package jp.sabakan.mirai.repository

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

    /**
     * ユーザ情報をメールアドレスで取得するメソッド
     *
     * @param userAddress ユーザのメールアドレス
     * @return ユーザ情報のリスト
     */
    fun getUserOne(userAddress: String): List<Map<String, Any?>> {
        // SQLクエリのパラメータを設定
        val param = mapOf("userAddress" to userAddress)

        // クエリを実行してユーザ情報を取得
        return njdbc.queryForList(getUserOne, param)
    }

}