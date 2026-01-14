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
}