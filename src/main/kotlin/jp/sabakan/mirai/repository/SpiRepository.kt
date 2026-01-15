package jp.sabakan.mirai.repository

import jp.sabakan.mirai.data.AnsweredSpiData
import jp.sabakan.mirai.data.SpiData
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import org.springframework.stereotype.Repository

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

    // 直近3回分の回答を得るSQLクエリ
    val getSpiAnswers = """
        SELECT spi_answer, spi_correct_answer FROM spi_result_t
        WHERE spi_id = :spiId AND user_id = :userId
        ORDER BY spi_answer_at DESC LIMIT 3
    """.trimIndent()

    // 問題と回答率を取得するSQLクエリ
    val getSpiList = """
        SELECT s.spi_id, sr.spi_answers, sr.spi_count
        FROM spi_m s
        LEFT JOIN spi_rate_t sr
        ON s.spi_id = sr.spi_id AND sr.user_id = :userId
        ORDER BY s.spi_id
    """.trimIndent()

    // 回答結果を保存するSQLクエリ
    val insertSpiAnswer = """
        INSERT INTO spi_result_t (spi_result_id, spi_id, user_id, spi_user_answer, spi_correct_answer, spi_answer_at)
        VALUES (:spiResultId, :spiId, :userId, :spiUserAnswer, :spiCorrectAnswer, CURRENT_TIMESTAMP)
    """.trimIndent()

    // 回答率テーブルに新規登録するSQLクエリ
    val insertSpiAnswerRate = """
        INSERT INTO spi_rate_t (spi_id, user_id, spi_answers, api_count)
        VALUES (:spiId, :userId, :spiAnswers, :spiCount)
    """.trimIndent()

    // 問題文を追加するSQLクエリ
    val insertSpi = """
        INSERT INTO spi_m (spi_id, spi_content, spi_answer1, spi_answer2, spi_answer3, spi_answer4, spi_correct_answer, spi_category)
        VALUES (:spiId, :spiContent, :spiAnswer1, :spiAnswer2, :spiAnswer3, :spiAnswer4, :spiCorrectAnswer, :spiCategory)
    """.trimIndent()

    // 回答内容に基づき回答率を更新するSQLクエリ
    val updateSpiAnswerRate = """
        UPDATE spi_rate_t
        SET spi_answers = :spiAnswers,
            spi_count = :spiCount
        WHERE spi_id = :spiId AND user_id = :userId
    """.trimIndent()

    //問題文を削除するSQLクエリ
    val deleteSpi = "DELETE FROM spi_t WHERE spi_id = :spiId"
    val deleteSpiRate = "DELETE FROM spi_rate_t WHERE spi_id = :spiId"
    val deleteSpiResult = "DELETE FROM spi_result_t WHERE spi_id = :spiId"

    //ユーザの回答履歴を削除するSQLクエリ
    val deleteSpiRateByUser = "DELETE FROM spi_rate_t WHERE user_id = :userId"
    val deleteSpiResultByUser = "DELETE FROM spi_result_t WHERE user_id = :userId"

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
     * SPIの回答結果を保存する
     *
     * @param data 回答データ
     * @return 更新件数
     */
    fun insertSpiAnswer(data: AnsweredSpiData): Int {
        // パラメータマップの作成
        val paramMap = mapOf<String, Any?>(
            "spiResultId" to data.spiResultId,
            "spiId" to data.spiId,
            "userId" to data.userId,
            "spiUserAnswer" to data.userAnswer,
            "spiCorrectAnswer" to data.correctAnswer
        )

        // クエリの実行
        return jdbc.update(insertSpiAnswer, paramMap)
    }

    /**
     * Spiの回答率を新規登録する
     *
     * @param data 回答データ
     * @return 更新件数
     */
    fun insertSpiAnswerRate(data: AnsweredSpiData): Int {
        // パラメータマップの作成
        val temp = createAnswer(data)
        val paramMap = mapOf<String, Any?>(
            "spiId" to data.spiId ,
            "userId" to data.userId ,
            "spiAnswers" to temp ,
            "spiCount" to temp.count{ it == 'T' }
        )

        // クエリの実行
        return jdbc.update(insertSpiAnswerRate, paramMap)
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
     * ユーザのSPI質問リストを取得する
     *
     * @param data 回答データ
     * @return SPI質問リスト
     */
    fun getSpiList(data: AnsweredSpiData): List<Map<String, Any?>>{
        // パラメータマップの作成
        val paramMap = mapOf<String, Any?>(
            "userId" to data.userId
        )

        // クエリの実行
        return jdbc.queryForList(getSpiList, paramMap)
    }

    /**
     * SPIの回答率を更新する
     *
     * @param data 回答データ
     * @return 更新件数
     */
    fun updateSpiAnswerRate(data: AnsweredSpiData): Int {
        // パラメータマップの作成
        val temp = createAnswer(data)
        val paramMap = mapOf<String, Any?>(
            "spiAnswers" to temp ,
            "spiCount" to temp.count{ it == 'T' } ,
            "spiId" to data.spiId ,
            "userId" to data.userId
        )

        // クエリの実行
        return jdbc.update(updateSpiAnswerRate, paramMap)
    }

    /**
     * SPIの問題文を削除する
     *
     * @param data SPIデータ
     * @return 更新件数
     */
    fun deleteSpi(data: AnsweredSpiData): Int {
        // パラメータマップの作成
        val paramMap = mapOf<String, Any?>(
            "spiId" to data.spiId
        )

        // クエリの実行
        jdbc.update(deleteSpiRate, paramMap)
        jdbc.update(deleteSpiResult, paramMap)
        return jdbc.update(deleteSpi, paramMap)
    }

    /**
     * ユーザのSPI回答履歴を削除する
     *
     * @param data 回答データ
     * @return 更新件数
     */
    fun deleteSpiByUser(data: AnsweredSpiData): Int {
        // パラメータマップの作成
        val paramMap = mapOf<String, Any?>(
            "userId" to data.userId
        )

        // クエリの実行
        jdbc.update(deleteSpiRateByUser, paramMap)
        return jdbc.update(deleteSpiResultByUser, paramMap)
    }

    /**
     * 回答履歴文字列を作成する
     *
     * @param data 回答データ
     * @return 回答履歴文字列
     */
    fun createAnswer(data: AnsweredSpiData): String {
        // パラメータマップの作成
        val paramMap = mapOf<String, Any?>(
            "spiId" to data.spiId,
            "userId" to data.userId
        )

        // クエリの実行
        val answers = jdbc.queryForList(getSpiAnswers, paramMap)

        // 回答の正誤を判定
        val correctResult = if (data.userAnswer == data.correctAnswer) "T" else "F"

        // 回答の正当性を評価
        val history = answers.map {
            val userAnswer = it["spi_answer"] as? String
            val correctAnswer = it["spi_correct_answer"] as? String
            when {
                userAnswer == null || correctAnswer == null -> "N"
                userAnswer == correctAnswer -> "T"
                else -> "F"
            }
        }

        // 新しい順に連結して返す
        val newHistory = listOf(correctResult) + history
        return newHistory.take(3).joinToString("")
    }
}