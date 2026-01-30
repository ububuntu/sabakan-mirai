package jp.sabakan.mirai.repository

import jp.sabakan.mirai.data.SpiData
import jp.sabakan.mirai.data.SpiDetailData
import jp.sabakan.mirai.data.SpiHistoryData
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import org.springframework.stereotype.Repository

@Repository
class SpiRepository {

    @Autowired
    lateinit var jdbc: NamedParameterJdbcTemplate

    // --- SQL定義エリア ---
    private val selectSpiByCategorySql = """
        SELECT * FROM spi_m 
        WHERE spi_category = :spiCategory
    """.trimIndent()

    private val insertSpiHistorySql = """
        INSERT INTO spi_history_t (spi_hs_id, user_id, is_finished)
        VALUES (:spiHsId, :userId, FALSE)
    """.trimIndent()

    private val insertSpiDetailSql = """
        INSERT INTO spi_detail_t (spi_dl_id, spi_hs_id, question_number, spi_id, is_correct)
        VALUES (:spiDlId, :spiHsId, :questionNumber, :spiId, FALSE)
    """.trimIndent()

    private val updateSpiDetailAnswerSql = """
        UPDATE spi_detail_t 
        SET user_answer = :userAnswer, 
            is_correct = :isCorrect
        WHERE spi_hs_id = :spiHsId AND question_number = :questionNumber
    """.trimIndent()

    private val findDetailsOrderedSql = """
        SELECT d.*, m.* FROM spi_detail_t d
        JOIN spi_m m ON d.spi_id = m.spi_id
        WHERE d.spi_hs_id = :spiHsId
        ORDER BY d.question_number ASC
    """.trimIndent()

    private val updateHistoryFinishedSql = """
        UPDATE spi_history_t SET is_finished = TRUE WHERE spi_hs_id = :spiHsId
    """.trimIndent()

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

    private val selectInProgressSql = """
        SELECT spi_hs_id FROM spi_history_t
        WHERE user_id = :userId AND is_finished = FALSE
        ORDER BY spi_hs_date DESC
        LIMIT 1
    """.trimIndent()

    private val selectAllSpiSql = """
        SELECT * FROM spi_m ORDER BY spi_category, spi_id
    """.trimIndent()

    private val selectSpiByIdSql = """
        SELECT * FROM spi_m WHERE spi_id = :spiId
    """.trimIndent()

    private val insertSpiMasterSql = """
        INSERT INTO spi_m (
            spi_id, spi_content, spi_answer1, spi_answer2, spi_answer3, spi_answer4, 
            spi_correct_answer, spi_category
        ) VALUES (
            :spiId, :spiContent, :spiAnswer1, :spiAnswer2, :spiAnswer3, :spiAnswer4, 
            :spiCorrectAnswer, :spiCategory
        )
    """.trimIndent()

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

    private val deleteSpiMasterSql = """
        DELETE FROM spi_m WHERE spi_id = :spiId
    """.trimIndent()

    private val getSpiCount = """
        SELECT COUNT(*) AS cnt FROM spi_m
    """.trimIndent()

    // --- メソッドエリア ---

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

    fun insertHistory(data: SpiHistoryData) {
        val params = mapOf<String, Any?>(
            "spiHsId" to data.spiHsId, "userId" to data.userId
        )
        jdbc.update(insertSpiHistorySql, params)
    }

    fun insertDetail(data: SpiDetailData) {
        val params = mapOf<String, Any?>(
            "spiDlId" to data.spiDlId,
            "spiHsId" to data.spiHsId,
            "questionNumber" to data.questionNumber,
            "spiId" to data.spiId
        )
        jdbc.update(insertSpiDetailSql, params)
    }

    fun updateDetailAnswer(data: SpiDetailData) {
        val params = mapOf<String, Any?>(
            "userAnswer" to data.userAnswer,
            "isCorrect" to data.isCorrect,
            "spiHsId" to data.spiHsId,
            "questionNumber" to data.questionNumber
        )
        jdbc.update(updateSpiDetailAnswerSql, params)
    }

    fun findDetailsWithQuestion(data: SpiHistoryData): List<Map<String, Any?>> {
        val param = mapOf<String, Any?>(
            "spiHsId" to data.spiHsId
        )
        return jdbc.queryForList(findDetailsOrderedSql, param)
    }

    fun updateHistoryFinished(data: SpiHistoryData) {
        val param = mapOf<String, Any?>(
            "spiHsId" to data.spiHsId
        )
        jdbc.update(updateHistoryFinishedSql, param)
    }

    fun findHistoryByUserId(data: SpiHistoryData): List<Map<String, Any?>> {
        val params = mapOf<String, Any?>(
            "userId" to data.userId
        )
        return jdbc.queryForList(selectHistoryByUserSql, params)
    }

    fun findInProgressId(data: SpiHistoryData): String? {
        val params = mapOf<String, Any?>(
            "userId" to data.userId
        )
        val list = jdbc.queryForList(selectInProgressSql, params)
        return if (list.isNotEmpty()) list[0]["spi_hs_id"] as String else null
    }

    fun findAllSpi(): List<Map<String, Any?>> {
        val params = emptyMap<String, Any>()
        return jdbc.queryForList(selectAllSpiSql, params)
    }

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

    fun deleteSpiMaster(data: SpiData): Int {
        val params = mapOf<String, Any?>(
            "spiId" to data.spiId
        )
        return jdbc.update(deleteSpiMasterSql, params)
    }

    fun countSpi(): Int {
        val params = emptyMap<String, Any>()
        return jdbc.queryForObject(getSpiCount, params, Int::class.java) ?: 0
    }
}