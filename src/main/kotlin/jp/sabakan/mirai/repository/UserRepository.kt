package jp.sabakan.mirai.repository

import jp.sabakan.mirai.data.UserData
import jp.sabakan.mirai.request.UserRequest
import org.h2.command.dml.Insert
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import org.springframework.stereotype.Repository
import java.util.Date

/**
 * ユーザ情報リポジトリクラス
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
            "password" to data.password,
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
}