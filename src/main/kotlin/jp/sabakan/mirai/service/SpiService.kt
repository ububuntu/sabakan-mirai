package jp.sabakan.mirai.service

import jp.sabakan.mirai.MessageConfig
import jp.sabakan.mirai.data.SpiData
import jp.sabakan.mirai.data.SpiDetailData
import jp.sabakan.mirai.data.SpiHistoryData
import jp.sabakan.mirai.repository.SpiRepository
import jp.sabakan.mirai.request.SpiRequest
import jp.sabakan.mirai.response.SpiResponse
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

/**
 * SPI試験関連のサービスクラス
 */
@Service
class SpiService {

    @Autowired
    lateinit var spiRepository: SpiRepository

    /**
     * SPI試験開始処理
     *
     * @param request SPI試験開始リクエスト
     * @return 生成されたSPI試験ID
     */
    @Transactional
    fun startExam(request: SpiRequest): String {
        val hsId = "S"+UUID.randomUUID().toString()

        // 1. 履歴ヘッダ作成
        val history = SpiHistoryData().apply {
            spiHsId = hsId
            userId = request.userId
        }
        spiRepository.insertHistory(history)

        // 2. 問題ランダム抽出 (言語40問、非言語30問)
        // 引数はDataクラスに詰めて渡す
        val verbal = spiRepository.getRandomQuestions(SpiData().apply { spiCategory = "言語" }, 40)
        val nonVerbal = spiRepository.getRandomQuestions(SpiData().apply { spiCategory = "非言語" }, 30)

        val allQuestions = verbal + nonVerbal

        // 3. 70問分の明細枠(detail)を先に作成
        allQuestions.forEachIndexed { index, q ->
            val detail = SpiDetailData().apply {
                spiDlId = UUID.randomUUID().toString()
                spiHsId = hsId
                questionNumber = index + 1
                spiId = q["spi_id"] as String
            }
            spiRepository.insertDetail(detail)
        }

        // 生成した試験IDを返す
        return hsId
    }

    /**
     * SPI試験の回答保存処理
     *
     * @param request SPI試験回答リクエスト
     */
    @Transactional
    fun saveAnswer(request: SpiRequest) {
        // 現在の問題の正解を取得するために検索
        val searchData = SpiHistoryData().apply { spiHsId = request.spiHsId }
        val results = spiRepository.findDetailsWithQuestion(searchData)

        // 対象の問題を探す
        val currentQ = results.find { it["question_number"] == request.questionNumber }
        val correctAnswer = currentQ?.get("spi_correct_answer") as? Int

        // ユーザーの回答をInt変換
        val userAnsInt = request.userAnswer

        // 更新用データの作成
        val detail = SpiDetailData().apply {
            spiHsId = request.spiHsId
            questionNumber = request.questionNumber ?: 0
            userAnswer = userAnsInt
            // 正解と一致しているか判定
            isCorrect = (correctAnswer != null && userAnsInt == correctAnswer)
        }

        spiRepository.updateDetailAnswer(detail)
    }

    /**
     * SPI試験終了処理
     *
     * @param request SPI試験終了リクエスト
     */
    @Transactional
    fun finishExam(request: SpiRequest) {
        val data = SpiHistoryData().apply { spiHsId = request.spiHsId }
        spiRepository.updateHistoryFinished(data)
    }

    /**
     * SPI試験結果取得処理
     *
     * @param request SPI試験結果リクエスト
     * @return SPI試験結果リスト
     */
    fun getExamResults(request: SpiRequest): List<Map<String, Any?>> {
        val data = SpiHistoryData().apply { spiHsId = request.spiHsId }
        return spiRepository.findDetailsWithQuestion(data)
    }

    /**
     * 進行中のSPI試験ID取得処理
     *
     * @param request SPI試験リクエスト
     * @return 進行中のSPI試験ID
     */
    fun getInProgressSpiId(request: SpiRequest): String? {
        val data = SpiHistoryData().apply { userId = request.userId }
        return spiRepository.findInProgressId(data)
    }

    /**
     * SPI試験履歴一覧取得処理
     *
     * @param request SPI試験リクエスト
     * @return SPI試験履歴リスト
     */
    fun getHistoryList(request: SpiRequest): List<Map<String, Any?>> {
        val data = SpiHistoryData().apply { userId = request.userId }
        return spiRepository.findHistoryByUserId(data)
    }

    /**
     * 全SPI問題取得処理（管理者用）
     *
     * @return SPI問題リスト
     */
    fun getAllSpi(): List<SpiRequest> {
        val list = spiRepository.findAllSpi()
        // Map -> SpiRequest 変換
        return list.map { row ->
            SpiRequest().apply {
                spiId = row["spi_id"] as? String
                spiContent = row["spi_content"] as? String
                spiAnswer1 = row["spi_answer1"] as? String
                spiAnswer2 = row["spi_answer2"] as? String
                spiAnswer3 = row["spi_answer3"] as? String
                spiAnswer4 = row["spi_answer4"] as? String
                spiCorrectAnswer = (row["spi_correct_answer"] as? Number)?.toInt()
                spiCategory = row["spi_category"] as? String
            }
        }
    }

    /**
     * SPI問題IDからSPI問題取得処理（管理者用）
     *
     * @param spiId SPI問題ID
     * @return SPI問題
     */
    fun getSpiById(spiId: String): SpiRequest? {
        val data = SpiData().apply { this.spiId = spiId }
        val row = spiRepository.findSpiById(data) ?: return null

        return SpiRequest().apply {
            this.spiId = row["spi_id"] as? String
            spiContent = row["spi_content"] as? String
            spiAnswer1 = row["spi_answer1"] as? String
            spiAnswer2 = row["spi_answer2"] as? String
            spiAnswer3 = row["spi_answer3"] as? String
            spiAnswer4 = row["spi_answer4"] as? String
            spiCorrectAnswer = (row["spi_correct_answer"] as? Number)?.toInt()
            spiCategory = row["spi_category"] as? String
        }
    }

    /**
     * SPI問題登録処理（管理者用）
     *
     * @param request SPI問題登録リクエスト
     * @return SPI問題登録レスポンス
     */
    @Transactional
    fun insertSpi(request: SpiRequest): SpiResponse {
        val response = SpiResponse()
        val data = SpiData().apply {
            spiId = UUID.randomUUID().toString()
            spiContent = request.spiContent
            spiAnswer1 = request.spiAnswer1
            spiAnswer2 = request.spiAnswer2
            spiAnswer3 = request.spiAnswer3
            spiAnswer4 = request.spiAnswer4
            spiCorrectAnswer = request.spiCorrectAnswer
            spiCategory = request.spiCategory
        }
        try{
            spiRepository.insertSpiMaster(data)
            response.message = MessageConfig.SPI_INSERT_SUCCESS
        } catch (e: Exception) {
            response.message = MessageConfig.SPI_INSERT_FAILED
        }
        return response
    }

    /**
     * SPI問題更新処理（管理者用）
     *
     * @param request SPI問題更新リクエスト
     * @return SPI問題更新レスポンス
     */
    @Transactional
    fun updateSpi(request: SpiRequest): SpiResponse {
        val response = SpiResponse()
        val data = SpiData().apply {
            spiId = request.spiId // IDは画面から渡ってきたものを使用
            spiContent = request.spiContent
            spiAnswer1 = request.spiAnswer1
            spiAnswer2 = request.spiAnswer2
            spiAnswer3 = request.spiAnswer3
            spiAnswer4 = request.spiAnswer4
            spiCorrectAnswer = request.spiCorrectAnswer
            spiCategory = request.spiCategory
        }
        try {
            spiRepository.updateSpiMaster(data)
            response.message = MessageConfig.SPI_UPDATE_SUCCESS
        } catch (e: Exception) {
            response.message = MessageConfig.SPI_UPDATE_FAILED
        }
        return response
    }

    /**
     * SPI問題削除処理（管理者用）
     *
     * @param spiId SPI問題ID
     * @return SPI問題削除レスポンス
     */
    @Transactional
    fun deleteSpi(spiId: String): SpiResponse {
        val response = SpiResponse()
        val data = SpiData().apply { this.spiId = spiId }
        try {
            spiRepository.deleteSpiMaster(data)
            response.message = MessageConfig.SPI_DELETE_SUCCESS
        } catch (e: Exception) {
            response.message = MessageConfig.SPI_DELETE_FAILED
        }
        return response
    }

    /**
     * SPI問題数取得処理（管理者用）
     *
     * @return SPI問題数
     */
    fun getSpiCount(): Int {
        return spiRepository.countSpi()
    }
}