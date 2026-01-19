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
import java.util.UUID

@Service
class SpiService {
    @Autowired
    lateinit var spiRepository: SpiRepository

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

    @Transactional // データの整合性を保つためトランザクションをかける
    fun registerExamResult(request: SpiRequest.SpiExamRequest): SpiHistoryData {
        // 1. 履歴データの準備
        val historyId = UUID.randomUUID().toString() // 親ID生成
        val historyData = SpiHistoryData().apply {
            spiHsId = historyId
            userId = request.userId
            totalQuestions = request.answers?.size ?: 0
            correctCount = 0
            spiHsDate = java.time.LocalDateTime.now()
        }

        // 明細リスト（後でまとめてInsertするための保持用など、必要なら使う）
        // val detailsToSave = mutableListOf<SpiDetailData>()

        // 2. ループで採点処理
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

        // 3. 正答率の計算 (BigDecimalで計算)
        if (historyData.totalQuestions > 0) {
            val rate = historyData.correctCount.toBigDecimal()
                .divide(historyData.totalQuestions.toBigDecimal(), 2, java.math.RoundingMode.HALF_UP)
                .multiply(java.math.BigDecimal("100"))

            historyData.accuracyRate = rate
        } else {
            historyData.accuracyRate = java.math.BigDecimal.ZERO
        }

        // 4. 履歴（親）データの保存
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