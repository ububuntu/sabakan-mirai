package jp.sabakan.mirai.repository

import jp.sabakan.mirai.data.SpiData
import jp.sabakan.mirai.data.SpiDetailData
import jp.sabakan.mirai.data.SpiHistoryData
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import org.springframework.stereotype.Repository
import java.math.BigDecimal

@Repository
class SpiRepository {
    @Autowired
    lateinit var jdbc: NamedParameterJdbcTemplate

    // カテゴリーごとにSPI質問を取得するSQLクエリ
    val getSpiByCategory = """
    SELECT * FROM spi_m 
    WHERE spi_category = :spiCategory
    ORDER BY RAND() LIMIT 1
    """.trimIndent()

    // 正解を取得するSQLクエリ
    val getCorrectAnswerSql = """
        SELECT spi_correct_answer FROM spi_m WHERE spi_id = :spiId
    """.trimIndent()

    // 未完了のSPI履歴を取得するSQLクエリ
    val getUnfinishedSql = """
        SELECT spi_hs_id FROM spi_history_t 
        WHERE user_id = :userId AND is_finished = FALSE
        LIMIT 1
    """.trimIndent()

    // SPI明細の正誤情報を取得するSQLクエリ
    val getDetailsSql = """
        SELECT is_correct FROM spi_detail_t 
        WHERE spi_hs_id = :spiHsId
    """.trimIndent()

    // 問題文を追加するSQLクエリ
    val insertSpi = """
        INSERT INTO spi_m (spi_id, spi_content, spi_answer1, spi_answer2, spi_answer3, spi_answer4, spi_correct_answer, spi_category)
        VALUES (:spiId, :spiContent, :spiAnswer1, :spiAnswer2, :spiAnswer3, :spiAnswer4, :spiCorrectAnswer, :spiCategory)
    """.trimIndent()

    // SPI履歴を追加するSQLクエリ
    val insertSpiHistorySql = """
        INSERT INTO spi_history_t (spi_hs_id, user_id, total_questions, correct_count, accuracy_rate, spi_hs_date, is_finished)
        VALUES (:spiHsId, :userId, :totalQuestions, :correctCount, :accuracyRate, CURRENT_TIMESTAMP, FALSE)
    """.trimIndent()

    // SPI明細を追加するSQLクエリ
    val insertSpiDetailSql = """
        INSERT INTO spi_detail_t (spi_dl_id, spi_hs_id, spi_id, user_answer, is_correct)
        VALUES (:spiDlId, :spiHsId, :spiId, :userAnswer, :isCorrect)
    """.trimIndent()

    // SPI履歴を更新するSQLクエリ（試験終了時）
    val updateFinishSql = """
        UPDATE spi_history_t 
        SET correct_count = :correctCount, accuracy_rate = :accuracyRate, is_finished = TRUE 
        WHERE spi_hs_id = :spiHsId
    """.trimIndent()

    // SPI明細の件数を取得するSQLクエリ
    val countDetailsSql = """
        SELECT COUNT(*) FROM spi_detail_t 
        WHERE spi_hs_id = :spiHsId
    """.trimIndent()

    /**
     * SPIの問題文を追加する
     *
     * @param data SPIデータ
     * @return 更新件数
     */
    fun insertSpi(data: SpiData): Int {
        // パラメータマップの作成
        val paramMap = mapOf<String, Any?>(
            "spiId" to data.spiId,
            "spiContent" to data.spiContent,
            "spiAnswer1" to data.spiAnswer1,
            "spiAnswer2" to data.spiAnswer2,
            "spiAnswer3" to data.spiAnswer3,
            "spiAnswer4" to data.spiAnswer4,
            "spiCorrectAnswer" to data.spiCorrectAnswer,
            "spiCategory" to data.spiCategory
        )

        // クエリの実行
        return jdbc.update(insertSpi, paramMap)
    }

    /**
     * SPI履歴を追加する
     *
     * @param historyData SPI履歴データ
     * @return 更新件数
     */
    fun insertHistory(historyData: SpiHistoryData): Int {
        // パラメータマップの作成
        val paramMap = mapOf(
            "spiHsId" to historyData.spiHsId,
            "userId" to historyData.userId,
            "totalQuestions" to historyData.totalQuestions,
            "correctCount" to historyData.correctCount,
            "accuracyRate" to historyData.accuracyRate
        )

        // クエリの実行
        return jdbc.update(insertSpiHistorySql, paramMap)
    }

    /**
     * SPI明細を追加する
     *
     * @param detailData SPI明細データ
     * @return 更新件数
     */
    fun insertDetail(detailData: SpiDetailData): Int {
        // パラメータマップの作成
        val paramMap = mapOf(
            "spiDlId" to detailData.spiDlId,
            "spiHsId" to detailData.spiHsId, // 親のID
            "spiId" to detailData.spiId,
            "userAnswer" to detailData.userAnswer,
            "isCorrect" to detailData.isCorrect
        )

        // クエリの実行
        return jdbc.update(insertSpiDetailSql, paramMap)
    }

    fun updateExamResult(spiHsId: String, correctCount: Int, accuracyRate: BigDecimal): Int {
        // パラメータマップの作成
        val paramMap = mapOf(
            "spiHsId" to spiHsId,
            "correctCount" to correctCount,
            "accuracyRate" to accuracyRate
        )

        // クエリの実行
        return jdbc.update(updateFinishSql, paramMap)
    }

    /**
     * 指定カテゴリーのSPI質問を取得する
     *
     * @param spiCategory 問題種別
     * @return SPI質問リスト
     */
    fun getSpi(data: SpiData): List<Map<String, Any?>>{
        // パラメータマップの作成
        val paramMap = mapOf<String, Any?>(
            "spiCategory" to data.spiCategory
        )

        // クエリの実行
        return jdbc.queryForList(getSpiByCategory, paramMap)
    }

    /**
     * 未完了のSPI受検IDを取得する
     *
     * @param userId ユーザーID
     * @return SPI受検ID または null
     */
    fun getUnfinishedSpiHsId(userId: String): String? {
        // パラメータマップの作成
        val paramMap = mapOf("userId" to userId)
        return try {
            // クエリの実行
            jdbc.queryForObject(getUnfinishedSql, paramMap, String::class.java)
        } catch (e: Exception) {
            // 未完了のSPI履歴が存在しない場合はnullを返す
            null
        }
    }

    /**
     * 指定SPI履歴IDの正誤情報を取得する
     *
     * @param spiHsId SPI履歴ID
     * @return 正誤情報リスト
     */
    fun findDetailsByHistoryId(spiHsId: String): List<Boolean> {
        // パラメータマップの作成
        val params = mapOf("spiHsId" to spiHsId)
        return jdbc.query(getDetailsSql, params) { rs, _ ->
            rs.getBoolean("is_correct")
        }
    }

    /**
     * 指定SPI IDの正解を取得する
     *
     * @param spiId SPI受検ID
     * @return 正解番号 (1~4) または null
     */
    fun getCorrectAnswer(spiId: String): Int? {
        val paramMap = mapOf("spiId" to spiId)
        return try {
            // queryForObjectは結果が0件だと例外を投げるためtry-catchするか、queryを使用
            jdbc.queryForObject(getCorrectAnswerSql, paramMap, Int::class.java)
        } catch (e: Exception) {
            null
        }
    }

    fun countDetailsByHistoryId(spiHsId: String): Int {
        val paramMap = mapOf("spiHsId" to spiHsId)
        // クエリの実行
        return jdbc.queryForObject(countDetailsSql, paramMap, Int::class.java) ?: 0
    }
}