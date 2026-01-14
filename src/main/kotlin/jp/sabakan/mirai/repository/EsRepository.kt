package jp.sabakan.mirai.repository

import jp.sabakan.mirai.data.EsData
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.stereotype.Repository

@Repository
class EsRepository {
    @Autowired
    lateinit var jdbc: JdbcTemplate

    // ES一覧を取得するSQLクエリ
    val getEsList = """
        SELECT * FROM es_t
        WHERE user_id = :userId
        """.trimIndent()

    // ESを追加するSQLクエリ
    val insertEs = """
        INSERT INTO es_t (es_id, user_id, es_content_reason, es_content_selfpr, es_content_activities, es_content_stwe, es_occupation, es_date)
        VALUES (:esId, :userId, :esContentReason, :esContentSelfpr, :esContentActivities, :esContentStwe, :esOccupation, CURRENT_TIMESTAMP)
        """.trimIndent()

    // ESを更新するSQLクエリ
    val updateEs = """
        UPDATE es_t
        SET es_content_reason = :esContentReason,
            es_content_selfpr = :esContentSelfpr,
            es_content_activities = :esContentActivities,
            es_content_stwe = :esContentStwe,
            es_occupation = :esOccupation,
            es_date = CURRENT_TIMESTAMP
        WHERE es_id = :esId AND user_id = :userId
        """.trimIndent()

    // ESを削除するSQLクエリ
    val deleteEs = """
        DELETE FROM es_t
        WHERE es_id = :esId AND user_id = :userId
        """.trimIndent()

    // ESの最大IDを取得するSQLクエリ
    val getMaxEsId = """
        SELECT MAX(es_id) AS max_es_id FROM es_t
        WHERE es_id LIKE :prefix
        """.trimIndent()

    /**
     * 指定ユーザのES一覧を取得する
     *
     * @param userId ユーザID
     * @return ESリスト
     */
    fun getEsList(data: EsData): List<Map<String, Any?>> {
        // パラメータマップの作成
        val paramMap = mapOf<String, Any?>(
            "userId" to data.userId
        )

        // クエリの実行
        return jdbc.queryForList(getEsList, paramMap)
    }

    /**
     * ESを新規登録する
     *
     * @param data ESデータ
     * @return 登録件数
     */
    fun insertEs(data: EsData): Int {
        // パラメータマップの作成
        val paramMap = mapOf<String, Any?>(
            "esId" to data.esId,
            "userId" to data.userId,
            "esContentReason" to data.esContentReason,
            "esContentSelfpr" to data.esContentSelfpr,
            "esContentActivities" to data.esContentActivities,
            "esContentStwe" to data.esContentStwe,
            "esOccupation" to data.esOccupation
        )

        // クエリの実行
        return jdbc.update(insertEs, paramMap)
    }

    /**
     * ESを更新する
     *
     * @param data ESデータ
     * @return 更新件数
     */
    fun updateEs(data: EsData): Int {
        // パラメータマップの作成
        val paramMap = mapOf<String, Any?>(
            "esId" to data.esId,
            "userId" to data.userId,
            "esContentReason" to data.esContentReason,
            "esContentSelfpr" to data.esContentSelfpr,
            "esContentActivities" to data.esContentActivities,
            "esContentStwe" to data.esContentStwe,
            "esOccupation" to data.esOccupation
        )
        // クエリの実行
        return jdbc.update(updateEs, paramMap)
    }

    /**
     * ESを削除する
     *
     * @param data ESデータ
     * @return 削除件数
     */
    fun deleteEs(data: EsData): Int {
        // パラメータマップの作成
        val paramMap = mapOf<String, Any?>(
            "esId" to data.esId,
            "userId" to data.userId
        )

        // クエリの実行
        return jdbc.update(deleteEs, paramMap)
    }
}