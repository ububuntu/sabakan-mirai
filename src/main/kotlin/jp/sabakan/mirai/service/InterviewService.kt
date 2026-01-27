package jp.sabakan.mirai.service

import jp.sabakan.mirai.MessageConfig
import jp.sabakan.mirai.component.InterviewComponent
import jp.sabakan.mirai.data.InterviewData
import jp.sabakan.mirai.entity.InterviewEntity
import jp.sabakan.mirai.repository.InterviewRepository
import jp.sabakan.mirai.request.InterviewRequest
import jp.sabakan.mirai.response.InterviewResponse
import org.slf4j.LoggerFactory
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.stereotype.Service
import java.util.UUID
import java.util.concurrent.CompletableFuture
import java.util.concurrent.ConcurrentHashMap

@Service
class InterviewService(
    private val interviewRepository: InterviewRepository,
    private val interviewComponent: InterviewComponent
) {
    private val logger = LoggerFactory.getLogger(InterviewService::class.java)

    // 質問管理用の状態（インメモリ管理）
    private val questionLists = ConcurrentHashMap<String, MutableList<String>>()
    private val currentQuestionIndices = ConcurrentHashMap<String, Int>()
    private val sessionIdMap = ConcurrentHashMap<String, String>()

    companion object {
        private const val DEFAULT_SESSION = "default"
        private const val MAX_RETRY = 5

        // デフォルト質問リスト
        private val DEFAULT_QUESTIONS = listOf(
            "志望動機を教えてください",
            "自己PRをお願いします",
            "長所と短所を教えてください"
        )
    }

    init {
        // デフォルトセッションを初期化
        questionLists[DEFAULT_SESSION] = DEFAULT_QUESTIONS.toMutableList()
        currentQuestionIndices[DEFAULT_SESSION] = 0
    }

    // ==================== 面接履歴管理 ====================

    /**
     * 指定ユーザの面接履歴を取得する
     */
    fun getInterviews(request: InterviewRequest): InterviewResponse {
        logger.info("面接履歴取得: userId=${request.userId}")

        val data = InterviewData().apply {
            userId = request.userId
        }

        val table = interviewRepository.getInterviews(data)
        val list = tableToListEntity(table)

        if (list.isEmpty()) {
            logger.warn("面接履歴が見つかりません: userId=${request.userId}")
            throw Exception(MessageConfig.INTERVIEW_NOT_FOUND)
        }

        return InterviewResponse().apply {
            interviews = list
            message = null
        }
    }

    /**
     * 面接履歴を新規登録する
     */
    fun insertInterview(request: InterviewRequest): InterviewResponse {
        logger.info("面接履歴登録: userId=${request.userId}")

        var lastException: Exception? = null

        repeat(MAX_RETRY) { attempt ->
            try {
                val data = InterviewData().apply {
                    interviewId = createInterviewId()
                    userId = request.userId
                    interviewExpression = request.interviewExpression
                    interviewEyes = request.interviewEyes
                    interviewPosture = request.interviewPosture
                    interviewVoice = request.interviewVoice
                    interviewScore = request.interviewScore
                }

                interviewRepository.insertInterview(data)
                logger.info("面接履歴登録成功: interviewId=${data.interviewId}")

                return InterviewResponse().apply {
                    message = MessageConfig.INTERVIEW_INSERT_SUCCESS
                }
            } catch (e: DataIntegrityViolationException) {
                logger.warn("ID重複検出: リトライ ${attempt + 1}/$MAX_RETRY")
                lastException = e
            } catch (e: Exception) {
                logger.error("面接履歴登録エラー", e)
                throw Exception(MessageConfig.INTERVIEW_INSERT_FAILED, e)
            }
        }

        // 全リトライ失敗
        logger.error("面接履歴登録失敗: 最大リトライ回数超過", lastException)
        throw Exception(MessageConfig.INTERVIEW_INSERT_FAILED, lastException)
    }

    /**
     * 面接IDを生成する
     */
    private fun createInterviewId(): String {
        val uuid = UUID.randomUUID().toString()
        return "I$uuid"
    }

    /**
     * テーブルデータをエンティティリストに変換する
     */
    private fun tableToListEntity(table: List<Map<String, Any?>>): List<InterviewEntity> {
        return table.map { row ->
            InterviewEntity().apply {
                interviewId = row["interview_id"] as String?
                userId = row["user_id"] as String?
                interviewExpression = row["interview_expression"] as String?
                interviewEyes = row["interview_eyes"] as String?
                interviewPosture = row["interview_posture"] as String?
                interviewVoice = row["interview_voice"] as String?
                interviewDate = row["interview_date"] as String?
                interviewScore = row["interview_score"] as String?
            }
        }
    }

    // ==================== AI分析機能 ====================

    /**
     * AI分析システムへの接続確認
     */
    fun testConnection(): CompletableFuture<Boolean> {
        logger.info("AI分析システム接続確認")
        return interviewComponent.testConnection()
    }

    /**
     * 面接セッションを開始する
     */
    fun startInterviewSession(userId: String, request: Map<String, Any>): CompletableFuture<String> {
        logger.info("面接セッション開始: userId=$userId")

        val sessionId = UUID.randomUUID().toString()
        sessionIdMap[userId] = sessionId

        // 質問リストを初期化
        val questions = (request["questions"] as? List<*>)?.mapNotNull { it as? String }
            ?: DEFAULT_QUESTIONS

        questionLists[sessionId] = questions.toMutableList()
        currentQuestionIndices[sessionId] = 0

        logger.info("セッション初期化完了: sessionId=$sessionId, 質問数=${questions.size}")

        return interviewComponent.startAnalysis().thenApply { success ->
            if (success) {
                sessionId
            } else {
                throw RuntimeException("AI分析の開始に失敗しました")
            }
        }
    }

    /**
     * 面接セッションを停止する
     */
    fun stopInterviewSession(sessionId: String): CompletableFuture<String?> {
        logger.info("面接セッション停止: sessionId=$sessionId")

        return interviewComponent.stopAnalysis().thenApply { result ->
            // セッション情報をクリーンアップ
            questionLists.remove(sessionId)
            currentQuestionIndices.remove(sessionId)
            sessionIdMap.values.remove(sessionId)

            logger.info("セッション停止完了: sessionId=$sessionId")
            result
        }
    }

    /**
     * AI分析を開始する
     */
    fun startAnalysis(): CompletableFuture<Boolean> {
        logger.info("AI分析開始")
        return interviewComponent.startAnalysis()
    }

    /**
     * 音声データを分析する
     */
    fun analyzeAudio(base64Audio: String): CompletableFuture<Boolean> {
        logger.info("音声分析: データサイズ=${base64Audio.length}")
        return interviewComponent.analyzeAudio(base64Audio)
    }

    /**
     * フレーム画像を分析する
     */
    fun analyzeFrame(base64Image: String): CompletableFuture<Boolean> {
        logger.info("画像分析: データサイズ=${base64Image.length}")
        return interviewComponent.analyzeFrame(base64Image)
    }

    /**
     * AI分析をリセットする
     */
    fun resetAnalysis(): CompletableFuture<Boolean> {
        logger.info("AI分析リセット")
        return interviewComponent.reset()
    }

    /**
     * 音声結果を取得する
     */
    fun getAudioResult(): CompletableFuture<ByteArray> {
        logger.info("音声結果取得")
        return interviewComponent.getAudioResult().thenApply { it ?: ByteArray(0) }
    }

    // ==================== 質問管理（デフォルトセッション対応） ====================

    /**
     * 現在の質問を取得（引数なしでデフォルトセッション使用）
     */
    fun getCurrentQuestion(): String {
        return getCurrentQuestion(DEFAULT_SESSION)
    }

    /**
     * 現在の質問を取得（sessionId指定）
     */
    fun getCurrentQuestion(sessionId: String): String {
        val questions = questionLists[sessionId] ?: DEFAULT_QUESTIONS
        val index = currentQuestionIndices[sessionId] ?: 0

        return if (index < questions.size) {
            questions[index]
        } else {
            "面接は終了しました。"
        }
    }

    /**
     * 次の質問を取得（引数なしでデフォルトセッション使用）
     */
    fun getNextQuestion(): String? {
        return getNextQuestion(DEFAULT_SESSION)
    }

    /**
     * 次の質問を取得（sessionId指定）
     */
    fun getNextQuestion(sessionId: String): String? {
        val questions = questionLists[sessionId] ?: DEFAULT_QUESTIONS
        val currentIndex = currentQuestionIndices[sessionId] ?: 0
        val nextIndex = currentIndex + 1

        return if (nextIndex < questions.size) {
            currentQuestionIndices[sessionId] = nextIndex
            logger.info("次の質問に進む: sessionId=$sessionId, index=$nextIndex")
            questions[nextIndex]
        } else {
            logger.info("全質問完了: sessionId=$sessionId")
            null
        }
    }

    /**
     * 質問をリセット（引数なしでデフォルトセッション使用）
     */
    fun resetQuestions() {
        resetQuestions(DEFAULT_SESSION)
    }

    /**
     * 質問をリセット（sessionId指定）
     */
    fun resetQuestions(sessionId: String) {
        currentQuestionIndices[sessionId] = 0
        logger.info("質問をリセット: sessionId=$sessionId")
    }

    /**
     * 進捗率を取得（引数なしでデフォルトセッション使用）
     */
    fun getProgress(): Int {
        return getProgress(DEFAULT_SESSION)
    }

    /**
     * 進捗率を取得（sessionId指定）
     */
    fun getProgress(sessionId: String): Int {
        val questions = questionLists[sessionId] ?: DEFAULT_QUESTIONS
        val index = currentQuestionIndices[sessionId] ?: 0

        return if (questions.isEmpty()) 0
        else ((index + 1) * 100 / questions.size).coerceIn(0, 100)
    }

    /**
     * 次の質問があるかチェック（引数なしでデフォルトセッション使用）
     */
    fun hasNextQuestion(): Boolean {
        return hasNextQuestion(DEFAULT_SESSION)
    }

    /**
     * 次の質問があるかチェック（sessionId指定）
     */
    fun hasNextQuestion(sessionId: String): Boolean {
        val questions = questionLists[sessionId] ?: DEFAULT_QUESTIONS
        val index = currentQuestionIndices[sessionId] ?: 0

        return index + 1 < questions.size
    }

    /**
     * 質問の総数を取得（引数なしでデフォルトセッション使用）
     */
    fun getTotalQuestions(): Int {
        return getTotalQuestions(DEFAULT_SESSION)
    }

    /**
     * 質問の総数を取得（sessionId指定）
     */
    fun getTotalQuestions(sessionId: String): Int {
        return (questionLists[sessionId] ?: DEFAULT_QUESTIONS).size
    }

    /**
     * 現在の質問番号を取得（引数なしでデフォルトセッション使用）
     */
    fun getCurrentQuestionNumber(): Int {
        return getCurrentQuestionNumber(DEFAULT_SESSION)
    }

    /**
     * 現在の質問番号を取得（sessionId指定）
     */
    fun getCurrentQuestionNumber(sessionId: String): Int {
        return (currentQuestionIndices[sessionId] ?: 0) + 1
    }

    /**
     * 全質問リストを取得（引数なしでデフォルトセッション使用）
     */
    fun getAllQuestions(): List<String> {
        return getAllQuestions(DEFAULT_SESSION)
    }

    /**
     * 全質問リストを取得（sessionId指定）
     */
    fun getAllQuestions(sessionId: String): List<String> {
        return questionLists[sessionId] ?: DEFAULT_QUESTIONS
    }
}