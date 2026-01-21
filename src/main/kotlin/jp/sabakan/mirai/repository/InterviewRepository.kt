package jp.sabakan.mirai.repository

import jp.sabakan.mirai.data.InterviewData
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.stereotype.Repository

@Repository
class InterviewRepository {
    @Autowired
    lateinit var jdbc: JdbcTemplate

    // 面接履歴を取得するSQLクエリ
    val getInterviews = """
        SELECT * FROM interview_t
        WHERE user_id = :userId
        ORDER BY interview_id
    """.trimIndent()

    // 面接履歴を保存するSQLクエリ
    val insertInterview = """
        INSERT INTO interview_t (interview_id, user_id, interview_expression, interview_eyes, interview_posture, interview_voice, interview_date, interview_score)
        VALUES (:interviewId, :userId, :interviewExpression, :interviewEyes, :interviewPosture, :interviewVoice, CURRENT_TIMESTAMP, :interviewScore)
    """.trimIndent()

    // 最大面接ID取得SQLクエリ
    val getMaxInterviewId = "SELECT MAX(interview_id) AS max_interview_id FROM interview_t"

    /**
     * 指定ユーザの面接履歴を取得する
     *
     * @param userId ユーザID
     * @return 面接履歴リスト
     */
    fun getInterviews(data: InterviewData): List<Map<String, Any?>> {
        // パラメータマップの作成
        val paramMap = mapOf<String, Any?>(
            "userId" to data.userId
        )

        // クエリの実行
        return jdbc.queryForList(getInterviews, paramMap)
    }

    /**
     * 面接履歴を新規登録する
     *
     * @param userId ユーザID
     * @return 面接履歴リスト
     */
    fun insertInterview(data: InterviewData): Int {
        // パラメータマップの作成
        val paramMap = mapOf<String, Any?>(
            "interviewId" to data.interviewId,
            "userId" to data.userId,
            "interviewExpression" to data.interviewExpression,
            "interviewEyes" to data.interviewEyes,
            "interviewPosture" to data.interviewPosture,
            "interviewVoice" to data.interviewVoice,
            "interviewScore" to data.interviewScore
        )

        // クエリの実行
        return jdbc.update(insertInterview, paramMap)
    }

    /**
     * 質問内容を取得する
     *
     * @return 質問内容リスト
     */
    // クラスレベルで質問リストとインデックスを保持
    private val questions: List<String> = listOf(
        "あなたの志望動機は何ですか？",
        "自己PRをお願いします",
        "長所と短所を教えてください"
    )
    private var index = 0

    /**
     * 現在の質問内容を取得する
     */
    fun getCurrentQuestion(): String {
        return questions.getOrNull(index) ?: "質問はありません"
    }

    /**
     * 次の質問に進む
     */
    fun nextQuestion(): String? {
        index++
        return questions.getOrNull(index)
    }

    /**
     * 質問をリセット
     */
    fun resetQuestions() {
        index = 0
    }

    /**
     * 現在の質問を取得する
     * 作成中
     */
//    fun getCurrentQuestion(): String {
//        return questions.getOrNull(index) ?: "質問はありません"
//    }

    /**
     * 次の質問に進んで取得する
     */
    fun getNextQuestion(): String? {
        index++
        return questions.getOrNull(index)
    }

    /**
     * 質問のインデックスをリセットする
     * 作成中
     */
//    fun resetQuestions() {
//        index = 0
//    }

    /**
     * 進捗率を取得する
     * @return 進捗率（0〜100）
     */
    fun getProgress(): Int {
        return if (questions.isEmpty()) 100
        else ((index + 1) * 100 / questions.size)
    }

    /**
     * 次の質問が存在するかチェック
     * @return true: まだ質問がある, false: 終了
     */
    fun hasNextQuestion(): Boolean {
        return index < questions.size - 1
    }

    /**
     * 全質問数を取得する
     * @return 質問の総数
     */
    fun getTotalQuestions(): Int {
        return questions.size
    }

    /**
     * 現在の質問番号を取得する（1始まり）
     * @return 現在の質問番号
     */
    fun getCurrentQuestionNumber(): Int {
        return index + 1
    }

    /**
     * 全ての質問リストを取得する
     * @return 質問リスト
     */
    fun getAllQuestions(): List<String> {
        return questions
    }
}