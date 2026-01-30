package jp.sabakan.mirai.repository

import jp.sabakan.mirai.data.InterviewData
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import org.springframework.stereotype.Repository

/**
 * 面接データへのデータアクセスを担当するRepository
 * データベースへの読み書きのみを行い、ビジネスロジックは含まない
 */
@Repository
class InterviewRepository(
    private val jdbc: JdbcTemplate,
    private val namedJdbc: NamedParameterJdbcTemplate
) {
    companion object {
        // SQLクエリ定義
        private const val SELECT_INTERVIEWS = """
            SELECT 
                interview_id,
                user_id,
                interview_expression,
                interview_eyes,
                interview_posture,
                interview_voice,
                interview_date,
                interview_score,
                interview_comment
            FROM interview_t
            WHERE user_id = :userId
            ORDER BY interview_date DESC
        """

        private const val INSERT_INTERVIEW = """
            INSERT INTO interview_t (
                interview_id,
                user_id,
                interview_expression,
                interview_eyes,
                interview_posture,
                interview_voice,
                interview_date,
                interview_score,
                interview_comment
            ) VALUES (
                :interviewId,
                :userId,
                :interviewExpression,
                :interviewEyes,
                :interviewPosture,
                :interviewVoice,
                CURRENT_TIMESTAMP,
                :interviewScore,
                :interviewComment
            )
        """

        private const val SELECT_INTERVIEW_BY_ID = """
            SELECT 
                interview_id,
                user_id,
                interview_expression,
                interview_eyes,
                interview_posture,
                interview_voice,
                interview_date,
                interview_score,
                interview_comment
            FROM interview_t
            WHERE interview_id = :interviewId
        """

        private const val UPDATE_INTERVIEW = """
            UPDATE interview_t
            SET
                interview_expression = :interviewExpression,
                interview_eyes = :interviewEyes,
                interview_posture = :interviewPosture,
                interview_voice = :interviewVoice,
                interview_score = :interviewScore,
                interview_comment = :interviewComment
            WHERE interview_id = :interviewId
        """

        private const val DELETE_INTERVIEW = """
            DELETE FROM interview_t
            WHERE interview_id = :interviewId
        """

        private const val COUNT_INTERVIEWS_BY_USER = """
            SELECT COUNT(*) FROM interview_t
            WHERE user_id = :userId
        """

        private const val SELECT_RECENT_INTERVIEWS = """
            SELECT 
                interview_id,
                user_id,
                interview_expression,
                interview_eyes,
                interview_posture,
                interview_voice,
                interview_date,
                interview_score,
                interview_comment
            FROM interview_t
            WHERE user_id = :userId
            ORDER BY interview_date DESC
            LIMIT :limit
        """
    }

    // ==================== 基本CRUD操作 ====================

    /**
     * 指定ユーザの面接履歴を取得する
     *
     * @param data ユーザIDを含む検索条件
     * @return 面接履歴のリスト
     */
    fun getInterviews(data: InterviewData): List<Map<String, Any?>> {
        val paramMap = mapOf("userId" to data.userId)

        return try {
            namedJdbc.queryForList(SELECT_INTERVIEWS, paramMap)
        } catch (e: Exception) {
            throw e
        }
    }

    /**
     * 面接履歴を新規登録する
     *
     * @param data 登録する面接データ
     * @return 影響を受けた行数
     */
    fun insertInterview(data: InterviewData): Int {
        val paramMap = mapOf(
            "interviewId" to data.interviewId,
            "userId" to data.userId,
            "interviewExpression" to data.interviewExpression,
            "interviewEyes" to data.interviewEyes,
            "interviewPosture" to data.interviewPosture,
            "interviewVoice" to data.interviewVoice,
            "interviewScore" to data.interviewScore,
            "interviewComment" to data.interviewComment
        )

        return try {
            namedJdbc.update(INSERT_INTERVIEW, paramMap)
        } catch (e: Exception) {
            throw e
        }
    }

    /**
     * 面接IDで面接履歴を取得する
     *
     * @param interviewId 面接ID
     * @return 面接データ（存在しない場合はnull）
     */
    fun getInterviewById(interviewId: String): Map<String, Any?>? {
        val paramMap = mapOf("interviewId" to interviewId)

        return try {
            val results = namedJdbc.queryForList(SELECT_INTERVIEW_BY_ID, paramMap)
            results.firstOrNull()
        } catch (e: Exception) {
            null
        }
    }

    /**
     * 面接履歴を更新する
     *
     * @param data 更新する面接データ
     * @return 影響を受けた行数
     */
    fun updateInterview(data: InterviewData): Int {
        val paramMap = mapOf(
            "interviewId" to data.interviewId,
            "interviewExpression" to data.interviewExpression,
            "interviewEyes" to data.interviewEyes,
            "interviewPosture" to data.interviewPosture,
            "interviewVoice" to data.interviewVoice,
            "interviewScore" to data.interviewScore,
            "interviewComment" to data.interviewComment
        )

        return try {
            namedJdbc.update(UPDATE_INTERVIEW, paramMap)
        } catch (e: Exception) {
            throw e
        }
    }

    /**
     * 面接履歴を削除する
     *
     * @param interviewId 削除する面接ID
     * @return 影響を受けた行数
     */
    fun deleteInterview(interviewId: String): Int {
        val paramMap = mapOf("interviewId" to interviewId)

        return try {
            namedJdbc.update(DELETE_INTERVIEW, paramMap)
        } catch (e: Exception) {
            throw e
        }
    }

    // ==================== 集計・検索系 ====================

    /**
     * 指定ユーザの面接履歴件数を取得する
     *
     * @param userId ユーザID
     * @return 面接履歴件数
     */
    fun countInterviewsByUser(userId: String): Int {
        val paramMap = mapOf("userId" to userId)

        return try {
            namedJdbc.queryForObject(COUNT_INTERVIEWS_BY_USER, paramMap, Int::class.java) ?: 0
        } catch (e: Exception) {
            0
        }
    }

    /**
     * 指定ユーザの最近の面接履歴を取得する
     *
     * @param userId ユーザID
     * @param limit 取得件数
     * @return 面接履歴のリスト
     */
    fun getRecentInterviews(userId: String, limit: Int = 10): List<Map<String, Any?>> {
        val paramMap = mapOf(
            "userId" to userId,
            "limit" to limit
        )

        return try {
            namedJdbc.queryForList(SELECT_RECENT_INTERVIEWS, paramMap)
        } catch (e: Exception) {
            emptyList()
        }
    }

    /**
     * 面接IDの存在確認
     *
     * @param interviewId 面接ID
     * @return 存在する場合true
     */
    fun existsById(interviewId: String): Boolean {
        val sql = "SELECT COUNT(*) FROM interview_t WHERE interview_id = :interviewId"
        val paramMap = mapOf("interviewId" to interviewId)

        return try {
            val count = namedJdbc.queryForObject(sql, paramMap, Int::class.java) ?: 0
            count > 0
        } catch (e: Exception) {
            false
        }
    }
}