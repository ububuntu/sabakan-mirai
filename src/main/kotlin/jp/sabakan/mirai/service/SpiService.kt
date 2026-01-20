package jp.sabakan.mirai.service

import jp.sabakan.mirai.MessageConfig
import jp.sabakan.mirai.data.SpiData
import jp.sabakan.mirai.data.SpiDetailData
import jp.sabakan.mirai.data.SpiHistoryData
import jp.sabakan.mirai.entity.SpiEntity
import jp.sabakan.mirai.repository.SpiRepository
import jp.sabakan.mirai.request.SpiRequest
import jp.sabakan.mirai.response.SpiResponse
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.math.BigDecimal
import java.math.RoundingMode
import java.util.UUID

@Service
class SpiService {
    @Autowired
    lateinit var spiRepository: SpiRepository

    /**
     * 新しいSPI試験を開始する
     *
     * @param userId ユーザーID
     * @return 新しいSPI試験ID
     */
    @Transactional
    fun startNewExam(userId: String): String {
        // 新しいSPI試験IDを生成
        val historyId = UUID.randomUUID().toString()

        // SPI履歴データを作成
        val historyData = SpiHistoryData().apply {
            spiHsId = historyId
            this.userId = userId
            totalQuestions = 70
            correctCount = 0
            accuracyRate = BigDecimal.ZERO
            spiHsDate = java.time.LocalDateTime.now()
        }

        // Repositoryを使って履歴を保存
        spiRepository.insertHistory(historyData)

        // 新しいSPI試験IDを返す
        return historyId
    }

    /**
     * SPI質問を登録する
     *
     * @param request SPIリクエスト
     * @return SPIレスポンス
     */
    fun insertSpi(request: SpiRequest): SpiResponse {
        //リクエストからデータ変換
        val data = SpiData().apply {
            spiId = toCreateId()
            spiContent = request.spiContent
            spiAnswer1 = request.spiAnswer1
            spiAnswer2 = request.spiAnswer2
            spiAnswer3 = request.spiAnswer3
            spiAnswer4 = request.spiAnswer4
            spiCorrectAnswer = request.spiCorrectAnswer
            spiCategory = request.spiCategory
        }

        //リポジトリへ登録処理
        val insertCount = spiRepository.insertSpi(data)

        // 登録結果を確認
        if (insertCount == 0) {
            return SpiResponse().apply {
                message = MessageConfig.SPI_INSERT_FAILED
            }
        }

        //結果を返す
        return SpiResponse().apply {
            message = MessageConfig.SPI_INSERT_SUCCESS
        }
    }

    /**
     * 指定カテゴリーのSPI質問を取得する
     *
     * @param request SPIリクエスト
     * @return SPIレスポンス
     */
    fun getSpi(request: SpiRequest): SpiResponse {
        //リクエストからデータ変換
        val data = SpiData()
        data.spiCategory = request.spiCategory

        //リポジトリへ問い合わせ
        val table: List<Map<String, Any?>> = spiRepository.getSpi(data)
        val list: List<SpiEntity> = tableToListEntity(table)

        //結果を返す
        return SpiResponse().apply {
            spis = list
            message = null
        }
    }

    /**
     * 進行中のSPI試験IDを取得する
     *
     * @param userId ユーザーID
     * @return 進行中のSPI試験ID、存在しない場合はnull
     */
    fun getInProgressExamId(userId: String): String? {
        return spiRepository.getUnfinishedSpiHsId(userId)
    }

    /**
     * 現在の質問インデックスを取得する
     *
     * @param examId SPI試験ID
     * @return 現在の質問インデックス
     */
    fun getCurrentQuestionIndex(examId: String): Int {
        val answeredCount = spiRepository.countDetailsByHistoryId(examId)
        return answeredCount + 1 // (例: 5問解いていたら次は6問目)
    }

    /**
     * SPIの1つの回答を保存する
     *
     * @param examId SPI試験ID
     * @param spiId SPI質問ID
     * @param userAnswer ユーザーの回答
     */
    @Transactional
    fun saveOneAnswer(examId: String, spiId: String, userAnswer: Int) {
        // 正解を取得
        val correctAnswer = spiRepository.getCorrectAnswer(spiId)
        // 正誤判定
        val isCorrect = (correctAnswer != null && correctAnswer == userAnswer)

        // 明細データの作成
        val detailData = SpiDetailData().apply {
            spiDlId = UUID.randomUUID().toString()
            spiHsId = examId
            this.spiId = spiId
            this.userAnswer = userAnswer
            this.isCorrect = isCorrect
        }

        // DBへ保存
        spiRepository.insertDetail(detailData)
    }

    /**
     * SPI試験を完了する
     *
     * @param examId SPI試験ID
     */
    @Transactional
    fun finishExam(spiHsId: String){
        // 全明細から正解数を集計
        val results: List<Boolean> = spiRepository.findDetailsByHistoryId(spiHsId)

        val totalAnswered = results.size
        val correctCount = results.count { it }

        // 正答率の計算 (BigDecimalを使用)
        val accuracyRate = if (totalAnswered > 0) {
            BigDecimal(correctCount)
                .divide(BigDecimal(totalAnswered), 2, RoundingMode.HALF_UP)
                .multiply(BigDecimal("100"))
        } else {
            BigDecimal.ZERO
        }
        // SPI履歴データの更新
        spiRepository.updateExamResult(spiHsId, correctCount, accuracyRate)
    }

    /**
     * SPI試験結果を登録する
     *
     * @param request SPI試験リクエスト
     * @return SPI履歴データ
     */
    @Transactional
    fun registerExamResult(request: SpiRequest.SpiExamRequest): SpiHistoryData {
        // 履歴データの作成
        val historyId = UUID.randomUUID().toString() // 親ID生成
        val historyData = SpiHistoryData().apply {
            spiHsId = historyId
            userId = request.userId
            totalQuestions = request.answers?.size ?: 0
            correctCount = 0
            spiHsDate = java.time.LocalDateTime.now()
        }

        // 各回答の処理
        request.answers?.forEach { answerItem ->
            val currentSpiId = answerItem.spiId ?: return@forEach
            val userAns = answerItem.userAnswer

            // DBから正解を取得
            val correctAnswer = spiRepository.getCorrectAnswer(currentSpiId)

            // 正誤判定 (正解が存在し、かつユーザーの回答と一致すれば正解)
            val isCorrect = (correctAnswer != null && correctAnswer == userAns)

            // 正解数をカウントアップ
            if (isCorrect) {
                historyData.correctCount++
            }

            // 明細データの作成と保存
            val detailData = SpiDetailData().apply {
                spiDlId = UUID.randomUUID().toString()
                spiHsId = historyId
                spiId = currentSpiId
                userAnswer = userAns
                this.isCorrect = isCorrect
            }

            // Repositoryを使って明細を1件保存
            spiRepository.insertDetail(detailData)
        }

        // 正答率の計算
        if (historyData.totalQuestions > 0) {
            val rate = historyData.correctCount.toBigDecimal()
                .divide(historyData.totalQuestions.toBigDecimal(), 2, java.math.RoundingMode.HALF_UP)
                .multiply(java.math.BigDecimal("100"))

            historyData.accuracyRate = rate
        } else {
            historyData.accuracyRate = java.math.BigDecimal.ZERO
        }

        // Repositoryを使って履歴を保存
        spiRepository.insertHistory(historyData)

        // 必要に応じて結果を返す
        return historyData
    }

    /**
     * 新しいSPI IDを生成する
     *
     * @return 新しいSPI ID
     */
    private fun toCreateId(): String {
        // UUIDを生成
        val uuid = UUID.randomUUID().toString()

        // 新しいSPI IDを作成
        return "S$uuid"
    }

    /**
     * テーブルデータをSpiEntityリストに変換する
     *
     * @param table テーブルデータ
     * @return SpiEntityリスト
     */
    fun tableToListEntity(table: List<Map<String, Any?>>): List<SpiEntity> {
        val list: MutableList<SpiEntity> = mutableListOf()
        for (row in table) {
            val entity = SpiEntity().apply {
                spiId = row["spi_id"] as String?
                spiContent = row["spi_content"] as String?
                spiAnswer1 = row["spi_answer1"] as String?
                spiAnswer2 = row["spi_answer2"] as String?
                spiAnswer3 = row["spi_answer3"] as String?
                spiAnswer4 = row["spi_answer4"] as String?
                spiCorrectAnswer = row["spi_correct_answer"] as Int?
                spiCategory = row["spi_category"] as String?
            }
            list.add(entity)
        }
        return list
    }
}