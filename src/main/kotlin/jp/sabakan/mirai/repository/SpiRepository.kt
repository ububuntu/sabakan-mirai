package jp.sabakan.mirai.repository
import jp.sabakan.mirai.data.SpiData
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import org.springframework.stereotype.Repository

@Repository
class SpiRepository {
    @Autowired
    lateinit var njdbc: NamedParameterJdbcTemplate

    // 指定の問題を取得するSQLクエリ
    val getQuestion = "SELECT * FROM spi_t WHERE spi_id = :spiId"

    /**
     * 指定の問題を取得する
     *
     * @param data SPIデータ
     * @return spi問題リスト
     */
    fun getUserDetail(data: SpiData): List<Map<String, Any?>> {
        // パラメータマップの作成
        val paramMap = mapOf<String, Any?>(
            "spiId" to data.spiId
        )

        // クエリの実行
        return njdbc.queryForList(getQuestion, paramMap)
    }
}