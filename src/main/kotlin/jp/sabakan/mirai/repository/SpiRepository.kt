package jp.sabakan.mirai.repository

import jp.sabakan.mirai.data.SpiData
import jp.sabakan.mirai.data.SpiDetailData
import jp.sabakan.mirai.data.SpiHistoryData
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import org.springframework.stereotype.Repository

/**
 * SPIデータへのデータアクセスを担当するRepository
 * データベースへの読み書きのみを行い、ビジネスロジックは含まない
 */
@Repository
class SpiRepository {

    @Autowired
    lateinit var jdbc: NamedParameterJdbcTemplate

    // --- SQL定義エリア ---

    // カテゴリ別にランダムで問題を取得するSQL
    private val selectSpiByCategorySql = """
        SELECT * FROM spi_m 
        WHERE spi_category = :spiCategory
    """.trimIndent()

    // 結果を保存するSQL
    private val insertSpiHistorySql = """
        INSERT INTO spi_history_t (spi_hs_id, user_id, is_finished)
        VALUES (:spiHsId, :userId, FALSE)
    """.trimIndent()

    // 詳細結果を保存するSQL
    private val insertSpiDetailSql = """
        INSERT INTO spi_detail_t (spi_dl_id, spi_hs_id, question_number, spi_id, is_correct)
        VALUES (:spiDlId, :spiHsId, :questionNumber, :spiId, FALSE)
    """.trimIndent()

    // 詳細結果の回答を更新するSQL
    private val updateSpiDetailAnswerSql = """
        UPDATE spi_detail_t 
        SET user_answer = :userAnswer, 
            is_correct = :isCorrect
        WHERE spi_hs_id = :spiHsId AND question_number = :questionNumber
    """.trimIndent()

    // 詳細結果を履歴IDで取得するSQL（質問番号順）
    private val findDetailsOrderedSql = """
        SELECT d.*, m.* FROM spi_detail_t d
        JOIN spi_m m ON d.spi_id = m.spi_id
        WHERE d.spi_hs_id = :spiHsId
        ORDER BY d.question_number ASC
    """.trimIndent()

    // 履歴を完了に更新するSQL
    private val updateHistoryFinishedSql = """
        UPDATE spi_history_t SET is_finished = TRUE WHERE spi_hs_id = :spiHsId
    """.trimIndent()

    // ユーザ別に完了した履歴を取得するSQL
    private val selectHistoryByUserSql = """
        SELECT 
            spi_hs_id, 
            spi_hs_date, 
            (SELECT COUNT(*) FROM spi_detail_t WHERE spi_hs_id = h.spi_hs_id) as total_questions,
            (SELECT COUNT(*) FROM spi_detail_t WHERE spi_hs_id = h.spi_hs_id AND is_correct = TRUE) as correct_count
        FROM spi_history_t h
        WHERE user_id = :userId AND is_finished = TRUE
        ORDER BY spi_hs_date DESC
    """.trimIndent()

    // 進行中の履歴IDを取得するSQL
    private val selectInProgressSql = """
        SELECT spi_hs_id FROM spi_history_t
        WHERE user_id = :userId AND is_finished = FALSE
        ORDER BY spi_hs_date DESC
        LIMIT 1
    """.trimIndent()

    // 全SPI問題を取得するSQL
    private val selectAllSpiSql = """
        SELECT * FROM spi_m ORDER BY spi_category, spi_id
    """.trimIndent()

    // SPI問題をIDで取得するSQL
    private val selectSpiByIdSql = """
        SELECT * FROM spi_m WHERE spi_id = :spiId
    """.trimIndent()

    // SPIマスタの登録・更新・削除SQL
    private val insertSpiMasterSql = """
        INSERT INTO spi_m (
            spi_id, spi_content, spi_answer1, spi_answer2, spi_answer3, spi_answer4, 
            spi_correct_answer, spi_category
        ) VALUES (
            :spiId, :spiContent, :spiAnswer1, :spiAnswer2, :spiAnswer3, :spiAnswer4, 
            :spiCorrectAnswer, :spiCategory
        )
    """.trimIndent()

    // SPIマスタの更新SQL
    private val updateSpiMasterSql = """
        UPDATE spi_m SET 
            spi_content = :spiContent,
            spi_answer1 = :spiAnswer1,
            spi_answer2 = :spiAnswer2,
            spi_answer3 = :spiAnswer3,
            spi_answer4 = :spiAnswer4,
            spi_correct_answer = :spiCorrectAnswer,
            spi_category = :spiCategory
        WHERE spi_id = :spiId
    """.trimIndent()

    // SPIマスタの削除SQL
    private val deleteSpiMasterSql = """
        DELETE FROM spi_m WHERE spi_id = :spiId
    """.trimIndent()

    // SPIマスタの件数取得SQL
    private val getSpiCount = """
        SELECT COUNT(*) AS cnt FROM spi_m
    """.trimIndent()

    // --- メソッドエリア ---

    /**
     * 指定カテゴリのSPI問題からランダムに指定数取得する
     *
     * @param data SPIカテゴリ情報
     * @param limit 取得数
     * @return ランダムに選ばれたSPI問題リスト
     */
    fun getRandomQuestions(data: SpiData, limit: Int): List<Map<String, Any?>> {
        val params = mapOf("spiCategory" to data.spiCategory)

        // 1. 候補を全件取得
        val candidates = jdbc.queryForList(selectSpiByCategorySql, params)

        // データが0件の場合は空リストを返す
        if (candidates.isEmpty()) return emptyList()

        // 2. 指定数(limit)になるまで、候補からランダムに選び続ける（重複あり）
        val result = mutableListOf<Map<String, Any?>>()
        repeat(limit) {
            // candidates.random() はリストからランダムに1つ要素を返す
            result.add(candidates.random())
        }

        return result
    }

    /**
     * SPI履歴を挿入する
     *
     * @param data SPI履歴データ
     */
    fun insertHistory(data: SpiHistoryData) {
        val params = mapOf<String, Any?>(
            "spiHsId" to data.spiHsId, "userId" to data.userId
        )
        jdbc.update(insertSpiHistorySql, params)
    }

    /**
     * SPI詳細結果を挿入する
     *
     * @param data SPI詳細データ
     */
    fun insertDetail(data: SpiDetailData) {
        val params = mapOf<String, Any?>(
            "spiDlId" to data.spiDlId,
            "spiHsId" to data.spiHsId,
            "questionNumber" to data.questionNumber,
            "spiId" to data.spiId
        )
        jdbc.update(insertSpiDetailSql, params)
    }

    /**
     * SPI詳細結果の回答を更新する
     *
     * @param data SPI詳細データ
     */
    fun updateDetailAnswer(data: SpiDetailData) {
        val params = mapOf<String, Any?>(
            "userAnswer" to data.userAnswer,
            "isCorrect" to data.isCorrect,
            "spiHsId" to data.spiHsId,
            "questionNumber" to data.questionNumber
        )
        jdbc.update(updateSpiDetailAnswerSql, params)
    }

    /**
     * SPI履歴IDに紐づく詳細結果を質問番号順で取得する
     *
     * @param data SPI履歴データ
     * @return 詳細結果リスト
     */
    fun findDetailsWithQuestion(data: SpiHistoryData): List<Map<String, Any?>> {
        val param = mapOf<String, Any?>(
            "spiHsId" to data.spiHsId
        )
        return jdbc.queryForList(findDetailsOrderedSql, param)
    }

    /**
     * SPI履歴を完了に更新する
     *
     * @param data SPI履歴データ
     */
    fun updateHistoryFinished(data: SpiHistoryData) {
        val param = mapOf<String, Any?>(
            "spiHsId" to data.spiHsId
        )
        jdbc.update(updateHistoryFinishedSql, param)
    }

    /**
     * ユーザIDに紐づく完了したSPI履歴を取得する
     *
     * @param data SPI履歴データ
     * @return SPI履歴リスト
     */
    fun findHistoryByUserId(data: SpiHistoryData): List<Map<String, Any?>> {
        val params = mapOf<String, Any?>(
            "userId" to data.userId
        )
        return jdbc.queryForList(selectHistoryByUserSql, params)
    }

    /**
     * ユーザIDに紐づく進行中のSPI履歴IDを取得する
     *
     * @param data SPI履歴データ
     * @return 進行中のSPI履歴ID (存在しない場合はnull)
     */
    fun findInProgressId(data: SpiHistoryData): String? {
        val params = mapOf<String, Any?>(
            "userId" to data.userId
        )
        val list = jdbc.queryForList(selectInProgressSql, params)
        return if (list.isNotEmpty()) list[0]["spi_hs_id"] as String else null
    }

    /**
     * 全SPI問題を取得する
     *
     * @return SPI問題リスト
     */
    fun findAllSpi(): List<Map<String, Any?>> {
        val params = emptyMap<String, Any>()
        return jdbc.queryForList(selectAllSpiSql, params)
    }

    /**
     * SPI問題をIDで取得する
     *
     * @param data SPIデータ
     * @return SPI問題データ (存在しない場合はnull)
     */
    fun findSpiById(data: SpiData): Map<String, Any?>? {
        val list = jdbc.queryForList(selectSpiByIdSql, mapOf("spiId" to data.spiId))
        return list.firstOrNull()
    }fun insertSpiMaster(data: SpiData): Int {
        val params = mapOf<String, Any?>(
            "spiId" to data.spiId,
            "spiContent" to data.spiContent,
            "spiAnswer1" to data.spiAnswer1,
            "spiAnswer2" to data.spiAnswer2,
            "spiAnswer3" to data.spiAnswer3,
            "spiAnswer4" to data.spiAnswer4,
            "spiCorrectAnswer" to data.spiCorrectAnswer,
            "spiCategory" to data.spiCategory
        )
        return jdbc.update(insertSpiMasterSql, params)
    }

    /**
     * SPIマスタを更新する
     *
     * @param data SPIデータ
     * @return 更新件数
     */
    fun updateSpiMaster(data: SpiData): Int {
        val params = mapOf<String, Any?>(
            "spiId" to data.spiId,
            "spiContent" to data.spiContent,
            "spiAnswer1" to data.spiAnswer1,
            "spiAnswer2" to data.spiAnswer2,
            "spiAnswer3" to data.spiAnswer3,
            "spiAnswer4" to data.spiAnswer4,
            "spiCorrectAnswer" to data.spiCorrectAnswer,
            "spiCategory" to data.spiCategory
        )
        return jdbc.update(updateSpiMasterSql, params)
    }

    /**
     * SPIマスタを削除する
     *
     * @param data SPIデータ
     * @return 削除件数
     */
    fun deleteSpiMaster(data: SpiData): Int {
        val params = mapOf<String, Any?>(
            "spiId" to data.spiId
        )
        return jdbc.update(deleteSpiMasterSql, params)
    }

    /**
     * SPIマスタの件数を取得する
     *
     * @return SPIマスタ件数
     */
    fun countSpi(): Int {
        val params = emptyMap<String, Any>()
        return jdbc.queryForObject(getSpiCount, params, Int::class.java) ?: 0
    }
}