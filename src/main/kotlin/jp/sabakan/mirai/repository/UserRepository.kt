package jp.sabakan.mirai.repository

import jp.sabakan.mirai.data.UserData
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import org.springframework.stereotype.Repository

/**
 * ユーザ情報リポジトリクラス
 */
@Repository
class UserRepository {
    @Autowired
    lateinit var njdbc: NamedParameterJdbcTemplate
    @Autowired
    lateinit var jdbc: JdbcTemplate

    // ユーザー情報をメールアドレスで取得するSQLクエリ
    val getUserOne = "SELECT * FROM user_m WHERE user_address = :userAddress"

    // 指定ユーザの情報を取得するSQLクエリ
    val getUserDetail = "SELECT * FROM user_m WHERE user_id = :userId"

    // ユーザを新規登録するSQLクエリ
    val insertUser = """
        INSERT INTO user_m (user_id, user_name, user_address, password, user_role, user_valid, created_at, updated_at)
        VALUES (:userId, :userName, :userAddress, :password, :userRole, :userValid, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
    """.trimIndent()

    // 指定ユーザの情報を更新するSQLクエリ
    val updateUser = """
        UPDATE user_m
        SET user_name = :userName,
            user_address = :userAddress,
            password = :password,
            user_role = :userRole,
            user_valid = :userValid,
            updated_at = CURRENT_TIMESTAMP
        WHERE user_id = :userId
    """.trimIndent()

    // 指定ユーザを削除するSQLクエリ
    val deleteUser = """
        DELETE FROM user_m
        WHERE user_id = :userId
    """.trimIndent()

    // ログイン時更新SQLクエリ
    val loginTime = """
        UPDATE user_m
        SET lasted_at = CURRENT_TIMESTAMP
        WHERE user_id = :userId
    """.trimIndent()

    /**
     * ユーザログイン処理
     *
     * @param data ユーザデータ
     * @return ユーザ情報リスト
     */
    fun getUserLogin(data: UserData): List<Map<String, Any?>> {
        // パラメータマップの作成
        val paramMap = mapOf<String, Any?>(
            "userAddress" to data.userAddress
        )

        // ログイン時間更新
        jdbc.update(loginTime, mapOf("userId" to data.userId))

        // クエリの実行
        return njdbc.queryForList(getUserOne, paramMap)
    }

    /**
     * 指定ユーザの情報を取得する
     *
     * @param data ユーザデータ
     * @return ユーザ情報リスト
     */
    fun getUserDetail(data: UserData): List<Map<String, Any?>> {
        // パラメータマップの作成
        val paramMap = mapOf<String, Any?>(
            "userId" to data.userId
        )

        // クエリの実行
        return njdbc.queryForList(getUserDetail, paramMap)
    }

    /**
     * ユーザを新規登録する
     *
     * @param data ユーザデータ
     * @return 登録件数
     */
    fun insertUser(data: UserData): Int {
        // パラメータマップの作成
        val paramMap = mapOf<String, Any?>(
            "userId" to data.userId,
            "userName" to data.userName,
            "userAddress" to data.userAddress,
            "password" to data.password,
            "userRole" to data.userRole,
            "userValid" to data.isValid
        )

        // クエリの実行
        return jdbc.update(insertUser, paramMap)
    }

    /**
     * 指定ユーザの情報を更新する
     *
     * @param data ユーザデータ
     * @return 更新件数
     */
    fun updateUser(data: UserData): Int {
        // パラメータマップの作成
        val paramMap = mapOf<String, Any?>(
            "userId" to data.userId,
            "userName" to data.userName,
            "userAddress" to data.userAddress
        )

        // クエリの実行
        return jdbc.update(updateUser, paramMap)
    }



    /**
     * 指定ユーザを削除する
     *
     * @param data ユーザデータ
     * @return 削除件数
     */
    fun deleteUser(data: UserData): Int {
        // パラメータマップの作成
        val paramMap = mapOf<String, Any?>(
            "userId" to data.userId
        )

        // クエリの実行
        return jdbc.update(deleteUser, paramMap)
    }
}