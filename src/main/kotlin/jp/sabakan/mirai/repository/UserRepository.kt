package jp.sabakan.mirai.repository

import jp.sabakan.mirai.data.GoalData
import jp.sabakan.mirai.data.UserData
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.stereotype.Repository
import java.util.Date

/**
 * ユーザデータへのデータアクセスを担当するRepository
 * データベースへの読み書きのみを行い、ビジネスロジックは含まない
 */
@Repository
class UserRepository {
    @Autowired
    lateinit var njdbc: NamedParameterJdbcTemplate

    @Autowired
    lateinit var jdbc: JdbcTemplate

    // ユーザ一覧取得
    val getUserList = """
        SELECT user_id, user_name, user_address, password, user_role, user_valid, created_at, updated_at, lasted_at
        FROM user_m
        ORDER BY created_at DESC
    """.trimIndent()

    // ユーザ情報取得（1件）
    val getOneUserList = """
        SELECT user_id, user_name, user_address, password, user_role, user_valid, created_at, updated_at, lasted_at
        FROM user_m
        WHERE user_id = :userId
    """.trimIndent()

    // ユーザ一覧取得（名前検索）
    val getUserListByName = """
        SELECT user_id, user_name, user_address, password, user_role, user_valid, created_at, updated_at, lasted_at
        FROM user_m
        WHERE user_name LIKE :keyword
        ORDER BY created_at DESC
    """.trimIndent()

    // 期限切れユーザID取得
    val getExpiredUserIdsSql = """
        SELECT user_id 
        FROM user_m 
        WHERE user_valid = false 
        AND updated_at < :thresholdDate
    """.trimIndent()

    // ユーザ情報登録
    val insertUser = """
        INSERT INTO user_m (
        user_id, user_name, user_address, password, user_role, user_valid, created_at, updated_at, lasted_at
        ) VALUES (
        :userId, :userName, :userAddress, :password, :userRole, :isValid, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
        )
    """.trimIndent()

    // ユーザ情報更新
    val updateUser = """
        UPDATE user_m
        SET user_name = :userName,
            user_address = :userAddress,
            password = :password,
            user_role = :userRole,
            user_valid = :isValid,
            updated_at = CURRENT_TIMESTAMP
        WHERE user_id = :userId
    """.trimIndent()

    // 目標情報取得
    val getGoal = """
        SELECT goal_id, user_id, goal_content, goal_date
        FROM goal_m
        WHERE user_id = :userId
    """.trimIndent()

    // 目標情報登録
    val insertGoal = """
        INSERT INTO goal_m (goal_id, user_id, goal_content, goal_date)
        VALUES (:goalId, :userId, :goalContent, :goalDate)
    """.trimIndent()

    // 目標情報更新
    val updateGoal = """
        UPDATE goal_m
        SET goal_content = :goalContent, goal_date = :goalDate
        WHERE goal_id = :goalId
    """.trimIndent()

    // ユーザ情報削除
    val deleteUser = """
        DELETE FROM user_m WHERE user_id = :userId
    """.trimIndent()

    // 目標情報削除
    val deleteGoal = """
        DELETE FROM goal_m WHERE user_id = :userId
    """.trimIndent()

    // エントリーシート情報削除
    val deleteEs = """
        DELEET FROM es_t WHERE user_id = :userId
    """.trimIndent()

    // 面接情報削除
    val deleteInterview = """
        DELETE FROM interview_t WHERE user_id = :userId
    """.trimIndent()

    // SPI履歴情報削除
    val deleteSpiHistory = """
        DELETE FROM spi_history_t WHERE user_id = :userId
    """.trimIndent()

    // SPI詳細情報削除
    val deleteSpiDetail = """
        DELETE FROM spi_detail_t 
        WHERE spi_hs_id IN (SELECT spi_hs_id FROM spi_history_t WHERE user_id = :userId)
    """.trimIndent()

    // メールアドレス（userAddress）でユーザを検索するSQL
    val getUserByAddress = """
        SELECT user_id, user_name, user_address, password, user_role, user_valid, created_at, updated_at, lasted_at
        FROM user_m
        WHERE user_address = :userAddress
    """.trimIndent()

    // ユーザ数カウントSQL
    val countUsers = """
        SELECT COUNT(*) FROM user_m
    """.trimIndent()

    /**
     * ユーザ一覧を取得する
     *
     * @return ユーザのリスト
     */
    fun getUserList(): List<Map<String, Any?>> {
        // パラメータマップの作成
        val params = mapOf<String, Any?>()

        // クエリの実行
        return njdbc.queryForList(getUserList, params)
    }

    /**
     * 期限切れユーザID一覧を取得する
     *
     * @param thresholdDate 期限日付
     * @return 期限切れユーザIDのリスト
     */
    fun getExpiredUserIds(thresholdDate: Date): List<String> {
        val params = mapOf("thresholdDate" to thresholdDate)
        return njdbc.queryForList(getExpiredUserIdsSql, params).map { it["user_id"] as String }
    }

    /**
     * ユーザ情報を1件取得する
     *
     * @param userId ユーザID
     * @return ユーザ情報のマップ
     */
    fun getOneUserList(data: UserData): Map<String, Any?>? {
        // パラメータマップの作成
        val params = mapOf<String, Any?>(
            "userId" to data.userId
        )

        // クエリの実行
        val result = njdbc.queryForList(getOneUserList, params)
        return if (result.isNotEmpty()) result[0] else null
    }

    /**
     * ユーザ一覧を名前で検索する
     *
     * @param keyword 検索キーワード
     * @return ユーザのリスト
     */
    fun getUserListByName(data: UserData): List<Map<String, Any?>> {
        // パラメータマップの作成
        val params = mapOf<String, Any?>(
            "keyword" to data.keyword
        )

        // クエリの実行
        return njdbc.queryForList(getUserListByName, params)
    }

    /**
     * メールアドレスでユーザ情報を取得する
     *
     * @param email ユーザメールアドレス
     * @return ユーザ情報のマップ
     */
    fun getUserByAddress(email: String): Map<String, Any?>? {
        val params = mapOf("userAddress" to email)
        val result = njdbc.queryForList(getUserByAddress, params)
        return if (result.isNotEmpty()) result[0] else null
    }

    /**
     * ユーザ情報を登録する
     *
     * @param userId ユーザID
     * @param userName ユーザ名
     * @param userAddress ユーザメールアドレス
     * @param password パスワード
     * @param userRole ユーザ権限
     * @param isValid 有効フラグ
     * @return 登録件数
     */
    fun insertUser(data: UserData): Int {
        // パラメータマップの作成
        val params = mapOf<String, Any?>(
            "userId" to data.userId,
            "userName" to data.userName,
            "userAddress" to data.userAddress,
            "password" to BCryptPasswordEncoder().encode(data.password),
            "userRole" to data.userRole,
            "isValid" to data.isValid
        )

        // クエリの実行
        return njdbc.update(insertUser, params)
    }

    /**
     * ユーザ情報を更新する
     *
     * @param userId ユーザID
     * @param userName ユーザ名
     * @param userAddress ユーザメールアドレス
     * @param password パスワード
     * @param userRole ユーザ権限
     * @param isValid 有効フラグ
     * @return 更新件数
     */
    fun updateUser(data: UserData): Int {
        // パラメータマップの作成
        val params = mapOf<String, Any?>(
            "userId" to data.userId,
            "userName" to data.userName,
            "userAddress" to data.userAddress,
            "password" to data.password,
            "userRole" to data.userRole,
            "isValid" to data.isValid
        )

        // クエリの実行
        return njdbc.update(updateUser, params)
    }

    /**
     * 目標情報を取得する
     *
     * @param userId ユーザID
     * @return 目標情報のリスト
     */
    fun getGoal(data: GoalData): List<Map<String, Any?>> {
        val params = mapOf<String, Any?>(
            "userId" to data.userId
        )
        return njdbc.queryForList(getGoal, params)
    }

    /**
     * 目標情報を登録する
     *
     * @param goalId 目標ID
     * @param userId ユーザID
     * @param goalContent 目標内容
     * @param goalDate 目標日付
     * @return 登録件数
     */
    fun insertGoal(data: GoalData): Int {
        val params = mapOf<String, Any?>(
            "goalId" to data.goalId,
            "userId" to data.userId,
            "goalContent" to data.goalContent,
            "goalDate" to data.goalDate
        )
        return njdbc.update(insertGoal, params)
    }

    /**
     * 目標情報を更新する
     *
     * @param goalId 目標ID
     * @param goalContent 目標内容
     * @param goalDate 目標日付
     * @return 更新件数
     */
    fun updateGoal(data: GoalData): Int {
        val params = mapOf<String, Any?>(
            "goalId" to data.goalId,
            "goalContent" to data.goalContent,
            "goalDate" to data.goalDate
        )
        return njdbc.update(updateGoal, params)
    }

    /**
     * ユーザ情報を削除する
     *
     * @param userId ユーザID
     */
    fun deleteUser(userId: String) {
        val params = mapOf<String, Any?>(
            "userId" to userId
        )
        njdbc.update(deleteSpiDetail, params)
        njdbc.update(deleteSpiHistory, params)
        njdbc.update(deleteInterview, params)
        njdbc.update(deleteEs, params)
        njdbc.update(deleteGoal, params)
        njdbc.update(deleteUser, params)
    }

    /**
     * ユーザ数をカウントする
     *
     * @return ユーザ数
     */
    fun countUsers(): Int {
        val params = mapOf<String, Any?>()
        return njdbc.queryForObject(countUsers, params, Int::class.java) ?: 0
    }
}