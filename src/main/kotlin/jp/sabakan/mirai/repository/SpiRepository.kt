package jp.sabakan.mirai.repository

import jp.sabakan.mirai.data.AnsweredSpiData
import jp.sabakan.mirai.data.SpiData
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.stereotype.Repository

@Repository
class SpiRepository {
    @Autowired
    lateinit var jdbc: JdbcTemplate

    // カテゴリーごとにSPI質問を取得するSQLクエリ
    val getSpiByCategory = """
        SELECT * FROM spi_t
        WHERE spi_category = :spiCategory
        ORDER BY RANDOM() LIMIT 1
    """.trimIndent()

    // 回答結果を保存するSQLクエリ
    val insertSpiAnswer = """
        INSERT INTO spi_result_t (spi_result_id, spi_id, user_id, spi_user_answer, spi_correct_answer, spi_answer_at)
        VALUES (:spiResultId, :spiId, :userId, :spiUserAnswer, :spiCorrectAnswer, CURRENT_TIMESTAMP)
    """.trimIndent()

    // 回答内容に基づき回答率を更新するSQLクエリ
    val updateSpiAnswerRate = """
        UPDATE spi_rate_t
        SET spi_answers = :spiAnswers,
            spi_count = :spiCount
        WHERE spi_id = :spiId AND user_id = :userId
    """.trimIndent()

    // 直近3回分の回答を得るSQLクエリ
    val getSpiAnswers = """
        SELECT spi_answer, spi_correct_answer FROM spi_result_t
        WHERE spi_id = :spiId AND user_id = :userId
        ORDER BY spi_answer_at DESC LIMIT 3
    """.trimIndent()

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
     * 回答履歴文字列を作成する
     *
     * @param data 回答データ
     * @return 回答履歴文字列
     */
    private fun createAnswer(data: AnsweredSpiData): String {
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