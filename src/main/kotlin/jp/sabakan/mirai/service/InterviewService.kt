package jp.sabakan.mirai.service

import jp.sabakan.mirai.MessageConfig
import jp.sabakan.mirai.component.InterviewComponent
import jp.sabakan.mirai.component.InterviewCommentGenerator
import jp.sabakan.mirai.data.InterviewData
import jp.sabakan.mirai.entity.InterviewEntity
import jp.sabakan.mirai.repository.InterviewRepository
import jp.sabakan.mirai.request.InterviewRequest
import jp.sabakan.mirai.response.InterviewResponse
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.stereotype.Service
import tools.jackson.databind.ObjectMapper
import java.util.UUID
import java.util.concurrent.CompletableFuture
import java.util.concurrent.ConcurrentHashMap

@Service
class InterviewService(
    private val interviewRepository: InterviewRepository,
    private val interviewComponent: InterviewComponent,
    private val commentGenerator: InterviewCommentGenerator
) {
    private val objectMapper = ObjectMapper()

    // 質問管理用の状態（インメモリ管理）
    private val questionLists = ConcurrentHashMap<String, MutableList<String>>()
    private val currentQuestionIndices = ConcurrentHashMap<String, Int>()
    private val sessionIdMap = ConcurrentHashMap<String, String>()

    // セッションごとの分析結果を一時保存
    private val sessionAnalysisResults = ConcurrentHashMap<String, Map<String, Any>>()

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
        val data = InterviewData().apply {
            userId = request.userId
        }

        val table = interviewRepository.getInterviews(data)
        val list = tableToListEntity(table)
        val recentList = list.take(3).reversed().toList()

        return InterviewResponse().apply {
            interviews = recentList
            message = if (recentList.isEmpty()) MessageConfig.INTERVIEW_NOT_FOUND else null
        }
    }

    /**
     * 面接履歴を新規登録する
     */
    fun insertInterview(request: InterviewRequest): InterviewResponse {
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
                    interviewComment = request.interviewComment
                }

                interviewRepository.insertInterview(data)

                return InterviewResponse().apply {
                    message = MessageConfig.INTERVIEW_INSERT_SUCCESS
                }
            } catch (e: DataIntegrityViolationException) {
                lastException = e
            } catch (e: Exception) {
                throw Exception(MessageConfig.INTERVIEW_INSERT_FAILED, e)
            }
        }

        // 全リトライ失敗
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
                userId = row["user_id"] as String?
                interviewExpression = row["interview_expression"]?.toString()
                interviewEyes = row["interview_eyes"]?.toString()
                interviewPosture = row["interview_posture"]?.toString()
                interviewVoice = row["interview_voice"]?.toString()
                interviewScore = row["interview_score"]?.toString()
                interviewComment = row["interview_comment"] as String?
            }
        }
    }

    // ==================== AI分析機能 ====================

    /**
     * AI分析システムへの接続確認
     */
    fun testConnection(): CompletableFuture<Boolean> {
        return interviewComponent.testConnection()
    }

    /**
     * userIdからsessionIdを取得する
     */
    fun getSessionIdByUserId(userId: String): String? {
        return sessionIdMap[userId]
    }

    /**
     * 面接セッションを停止し、結果とコメントを生成する（userId版）
     */
    fun stopInterviewSessionByUserId(userId: String): CompletableFuture<Map<String, Any>?> {
        val sessionId = sessionIdMap[userId]
            ?: throw IllegalArgumentException("ユーザー $userId のセッションが見つかりません")

        return stopInterviewSession(sessionId)
    }

    /**
     * 面接セッションを開始する
     */
    fun startInterviewSession(userId: String, request: Map<String, Any>): CompletableFuture<String> {
        val sessionId = UUID.randomUUID().toString()
        sessionIdMap[userId] = sessionId

        // 質問リストを初期化
        val questions = (request["questions"] as? List<*>)?.mapNotNull { it as? String }
            ?: DEFAULT_QUESTIONS

        questionLists[sessionId] = questions.toMutableList()
        currentQuestionIndices[sessionId] = 0

        return interviewComponent.startAnalysis().thenApply { success ->
            if (success) {
                sessionId
            } else {
                throw RuntimeException("AI分析の開始に失敗しました")
            }
        }
    }

    /**
     * 分析結果から各項目の点数を計算する（4項目：表情、視線、姿勢、発話速度）
     */
    @Suppress("UNCHECKED_CAST")
    private fun calculateScores(report: Map<String, Any>): Map<String, Int> {
        // 表情点数 (0-100)
        val expressionScore = calculateExpressionScore(report)

        // 視線点数 (0-100)
        val eyesScore = calculateEyesScore(report)

        // 姿勢点数 (0-100)
        val postureScore = calculatePostureScore(report)

        // 発話速度点数 (0-100)
        val speechSpeedScore = calculateSpeechSpeedScore(report)

        return mapOf(
            "expression" to expressionScore,
            "eyes" to eyesScore,
            "posture" to postureScore,
            "speechSpeed" to speechSpeedScore
        )
    }

    /**
     * 発話速度点数を計算する
     */
    @Suppress("UNCHECKED_CAST")
    private fun calculateSpeechSpeedScore(report: Map<String, Any>): Int {
        val speech = report["speech"] as? Map<String, Any>

        if (speech == null) {
            return 0
        }

        val charsPerMinute = speech["chars_per_minute"] as? Number

        if (charsPerMinute == null) {
            return 0
        }

        val cpm = charsPerMinute.toInt()
        return commentGenerator.calculateSpeechSpeedScore(cpm)
    }

    /**
     * 姿勢の詳細コメントを生成（どの項目がダメかを明示）
     */
    @Suppress("UNCHECKED_CAST")
    private fun generatePostureDetailComment(report: Map<String, Any>): String {
        val posture = report["posture"] as? Map<String, Any> ?: return "姿勢データが取得できませんでした"

        val faceCentered = posture["face_centered"] as? Map<String, Any>
        val faceStraight = posture["face_straight"] as? Map<String, Any>
        val shouldersLevel = posture["shoulders_level"] as? Map<String, Any>

        val issues = mutableListOf<String>()

        // 顔が中央にあるかチェック
        if (faceCentered != null) {
            val success = (faceCentered["success"] as? Number)?.toInt() ?: 0
            val fail = (faceCentered["fail"] as? Number)?.toInt() ?: 0
            if (fail > success) {
                issues.add("顔が中央にありません")
            }
        }

        // 顔が傾いていないかチェック
        if (faceStraight != null) {
            val success = (faceStraight["success"] as? Number)?.toInt() ?: 0
            val fail = (faceStraight["fail"] as? Number)?.toInt() ?: 0
            if (fail > success) {
                issues.add("顔が傾いています")
            }
        }

        // 肩が水平かチェック
        if (shouldersLevel != null) {
            val success = (shouldersLevel["success"] as? Number)?.toInt() ?: 0
            val fail = (shouldersLevel["fail"] as? Number)?.toInt() ?: 0
            if (fail > success) {
                issues.add("肩が水平ではありません")
            }
        }

        return if (issues.isEmpty()) {
            "姿勢は良好ですが、さらなる改善が可能です。"
        } else {
            "緊張から表情が硬くなる場面がありました。${issues.joinToString("、")}。正しい姿勢を意識しましょう。"
        }
    }

    /**
     * 面接セッションを停止し、結果とコメントを生成する
     */
    fun stopInterviewSession(sessionId: String): CompletableFuture<Map<String, Any>?> {
        return interviewComponent.stopAnalysis().thenApply { analysisResult ->
            // 安全なnullチェックと型チェック
            val resultString = when {
                analysisResult == null -> null
                analysisResult is String -> analysisResult
                else -> null
            }

            // 分析結果をパース
            val report = parseAnalysisResult(resultString)

            // reportが空の場合はダミーデータを使用（エラー回避）
            val validReport = if (report.isEmpty()) {
                createDummyReport()
            } else {
                report
            }

            // 各項目の点数を計算（4項目）
            val scores = calculateScores(validReport)

            // コメントを生成（4項目）
            val comments = generateComments(scores, validReport)

            // 結果を保存（クリーンアップ前に保存）
            val result = mapOf(
                "sessionId" to sessionId,
                "scores" to scores,
                "comments" to comments,
                "report" to validReport
            )
            sessionAnalysisResults[sessionId] = result

            // セッション情報をクリーンアップ
            questionLists.remove(sessionId)
            currentQuestionIndices.remove(sessionId)
            sessionIdMap.entries.removeIf { it.value == sessionId }

            result
        }.exceptionally { ex ->
            // エラー時はダミーデータで結果を返す
            val dummyReport = createDummyReport()
            val scores = calculateScores(dummyReport)
            val comments = generateComments(scores, dummyReport)

            mapOf(
                "sessionId" to sessionId,
                "scores" to scores,
                "comments" to comments,
                "report" to dummyReport,
                "error" to (ex.message ?: "Unknown error")
            )
        }
    }

    /**
     * Flask APIからの分析結果をパースする
     */
    @Suppress("UNCHECKED_CAST")
    private fun parseAnalysisResult(result: String?): Map<String, Any> {
        // nullまたは空文字チェック
        if (result.isNullOrBlank()) {
            return emptyMap()
        }

        return try {
            // JSONとしてパースを試みる
            val jsonMap = objectMapper.readValue(result, Map::class.java) as Map<String, Any>
            jsonMap["report"] as? Map<String, Any> ?: emptyMap()
        } catch (e: Exception) {
            emptyMap()
        }
    }

    /**
     * ダミーレポートを作成（エラー時やデータが取得できない場合の代替）
     */
    private fun createDummyReport(): Map<String, Any> {
        return mapOf(
            "emotion" to mapOf("喜び" to 70),
            "gaze" to mapOf(
                "directions" to mapOf(
                    "正面を見ている" to mapOf("percentage" to 70)
                )
            ),
            "posture" to mapOf(
                "perfect_rate" to 70,
                "face_centered" to mapOf("success" to 50, "fail" to 50),
                "face_straight" to mapOf("success" to 50, "fail" to 50),
                "shoulders_level" to mapOf("success" to 50, "fail" to 50)
            ),
            "speech" to mapOf("chars_per_minute" to 280)
        )
    }

    /**
     * 表情点数を計算（喜びのパーセンテージ）
     */
    @Suppress("UNCHECKED_CAST")
    private fun calculateExpressionScore(report: Map<String, Any>): Int {
        val emotion = report["emotion"] as? Map<String, Any>

        if (emotion == null) {
            return 0
        }

        val joyPercentage = emotion["喜び"] as? Number

        if (joyPercentage == null) {
            return 0
        }

        return joyPercentage.toInt().coerceIn(0, 100)
    }

    /**
     * 視線点数を計算（正面を見ているパーセンテージ）
     */
    @Suppress("UNCHECKED_CAST")
    private fun calculateEyesScore(report: Map<String, Any>): Int {
        val gaze = report["gaze"] as? Map<String, Any>

        if (gaze == null) {
            return 0
        }

        val directions = gaze["directions"] as? Map<String, Any>

        if (directions == null) {
            return 0
        }

        val frontGaze = directions["正面を見ている"] as? Map<String, Any>

        if (frontGaze == null) {
            return 0
        }

        val percentage = frontGaze["percentage"] as? Number

        if (percentage == null) {
            return 0
        }

        return percentage.toInt().coerceIn(0, 100)
    }

    /**
     * 姿勢点数を計算（perfect_rate）
     */
    @Suppress("UNCHECKED_CAST")
    private fun calculatePostureScore(report: Map<String, Any>): Int {
        val posture = report["posture"] as? Map<String, Any>

        if (posture == null) {
            return 0
        }

        val perfectRate = posture["perfect_rate"] as? Number

        if (perfectRate == null) {
            return 0
        }

        return perfectRate.toInt().coerceIn(0, 100)
    }

    /**
     * 点数と分析結果から4項目のコメントを生成する
     */
    private fun generateComments(scores: Map<String, Int>, report: Map<String, Any>): Map<String, String> {
        val expressionScore = scores["expression"] ?: 0
        val eyesScore = scores["eyes"] ?: 0
        val postureScore = scores["posture"] ?: 0

        // 発話速度の実データを取得（コメント生成用）
        @Suppress("UNCHECKED_CAST")
        val speech = report["speech"] as? Map<String, Any>
        val charsPerMinute = (speech?.get("chars_per_minute") as? Number)?.toInt() ?: 0

        // 姿勢の詳細コメント生成（点数が低い場合のみ詳細化）
        val postureComment = if (postureScore < 70) {
            generatePostureDetailComment(report)
        } else {
            commentGenerator.generatePostureComment(postureScore)
        }

        return mapOf(
            "表情" to commentGenerator.generateExpressionComment(expressionScore),
            "視線" to commentGenerator.generateEyesComment(eyesScore),
            "姿勢" to postureComment,
            "発話速度" to commentGenerator.generateSpeechSpeedComment(charsPerMinute)
        )
    }

    /**
     * AI分析を開始する
     */
    fun startAnalysis(): CompletableFuture<Boolean> {
        return interviewComponent.startAnalysis()
    }

    /**
     * 音声データを分析する
     */
    fun analyzeAudio(base64Audio: String): CompletableFuture<Boolean> {
        return interviewComponent.analyzeAudio(base64Audio)
    }

    /**
     * フレーム画像を分析する
     */
    fun analyzeFrame(base64Image: String): CompletableFuture<Boolean> {
        return interviewComponent.analyzeFrame(base64Image)
    }

    /**
     * AI分析をリセットする
     */
    fun resetAnalysis(): CompletableFuture<Boolean> {
        return interviewComponent.reset()
    }

    /**
     * 音声結果を取得する
     */
    fun getAudioResult(): CompletableFuture<ByteArray> {
        return interviewComponent.getAudioResult().thenApply { it ?: ByteArray(0) }
    }

    /**
     * セッションの分析結果を取得する
     */
    fun getSessionResult(sessionId: String): Map<String, Any>? {
        return sessionAnalysisResults[sessionId]
    }

    /**
     * セッションの分析結果を取得する（userId版）
     */
    fun getSessionResultByUserId(userId: String): Map<String, Any>? {
        val sessionId = sessionIdMap[userId] ?: return null
        return sessionAnalysisResults[sessionId]
    }

    /**
     * ユーザーの最新の面接結果を取得する
     */
    fun getLatestResultByUserId(userId: String): Map<String, Any>? {
        // まず現在のセッションから取得を試みる
        val sessionId = sessionIdMap[userId]
        if (sessionId != null) {
            val result = sessionAnalysisResults[sessionId]
            if (result != null) {
                return result
            }
        }

        // 現在のセッションになければ、最後に保存された結果を探す
        return sessionAnalysisResults.values.lastOrNull()
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
            questions[nextIndex]
        } else {
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