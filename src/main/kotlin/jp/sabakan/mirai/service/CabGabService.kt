package jp.sabakan.mirai.service

import jp.sabakan.mirai.MessageConfig
import jp.sabakan.mirai.data.CabGabData
import jp.sabakan.mirai.data.CabGabDetailData
import jp.sabakan.mirai.data.CabGabHistoryData
import jp.sabakan.mirai.repository.CabGabRepository
import jp.sabakan.mirai.request.CabGabRequest
import jp.sabakan.mirai.response.CabGabResponse
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Service
class CabGabService {

    @Autowired
    lateinit var cabgabRepository: CabGabRepository

    @Transactional
    fun startExam(request: CabGabRequest): String {
        val hsId = "C"+UUID.randomUUID().toString()

        // 1. 履歴ヘッダ作成
        val history = CabGabHistoryData().apply {
            cabgabHsId = hsId
            userId = request.userId
        }
        cabgabRepository.insertHistory(history)

        // 2. 問題ランダム抽出 (言語30問、計数30問、暗号30問)
        val verbal = cabgabRepository.getRandomQuestions(CabGabData().apply { cabgabCategory = "言語" }, 30)
        val math = cabgabRepository.getRandomQuestions(CabGabData().apply { cabgabCategory = "計数" }, 30)
        val crypto = cabgabRepository.getRandomQuestions(CabGabData().apply { cabgabCategory = "暗号" }, 30)

        // リストを結合 (計90問)
        val allQuestions = verbal + math + crypto

        // 3. 70問分の明細枠(detail)を先に作成
        allQuestions.forEachIndexed { index, q ->
            val detail = CabGabDetailData().apply {
                cabgabDlId = UUID.randomUUID().toString()
                cabgabHsId = hsId
                questionNumber = index + 1
                cabgabId = q["cabgab_id"] as String
            }
            cabgabRepository.insertDetail(detail)
        }

        // 生成した試験IDを返す
        return hsId
    }

    @Transactional
    fun saveAnswer(request: CabGabRequest) {
        // 現在の問題の正解を取得するために検索
        val searchData = CabGabHistoryData().apply { cabgabHsId = request.cabgabHsId }
        val results = cabgabRepository.findDetailsWithQuestion(searchData)

        // 対象の問題を探す
        val currentQ = results.find { it["question_number"] == request.questionNumber }
        val correctAnswer = currentQ?.get("cabgab_correct_answer") as? Int

        // ユーザーの回答をInt変換
        val userAnsInt = request.userAnswer

        // 更新用データの作成
        val detail = CabGabDetailData().apply {
            cabgabHsId = request.cabgabHsId
            questionNumber = request.questionNumber ?: 0
            userAnswer = userAnsInt
            // 正解と一致しているか判定
            isCorrect = (correctAnswer != null && userAnsInt == correctAnswer)
        }

        cabgabRepository.updateDetailAnswer(detail)
    }

    @Transactional
    fun finishExam(request: CabGabRequest) {
        val data = CabGabHistoryData().apply { cabgabHsId = request.cabgabHsId }
        cabgabRepository.updateHistoryFinished(data)
    }

    fun getExamResults(request: CabGabRequest): List<Map<String, Any?>> {
        val data = CabGabHistoryData().apply { cabgabHsId = request.cabgabHsId }
        return cabgabRepository.findDetailsWithQuestion(data)
    }

    fun getInProgressCabGabId(request: CabGabRequest): String? {
        val data = CabGabHistoryData().apply { userId = request.userId }
        return cabgabRepository.findInProgressId(data)
    }

    fun getHistoryList(request: CabGabRequest): List<Map<String, Any?>> {
        val data = CabGabHistoryData().apply { userId = request.userId }
        return cabgabRepository.findHistoryByUserId(data)
    }

    fun getAllCabGab(): List<CabGabRequest> {
        val list = cabgabRepository.findAllCabGab()
        // Map -> CabGabRequest 変換
        return list.map { row ->
            CabGabRequest().apply {
                cabgabId = row["cabgab_id"] as? String
                cabgabContent = row["cabgab_content"] as? String
                cabgabAnswer1 = row["cabgab_answer1"] as? String
                cabgabAnswer2 = row["cabgab_answer2"] as? String
                cabgabAnswer3 = row["cabgab_answer3"] as? String
                cabgabAnswer4 = row["cabgab_answer4"] as? String
                cabgabCorrectAnswer = (row["cabgab_correct_answer"] as? Number)?.toInt()
                cabgabCategory = row["cabgab_category"] as? String
            }
        }
    }

    fun getCabGabById(cabgabId: String): CabGabRequest? {
        val data = CabGabData().apply { this.cabgabId = cabgabId }
        val row = cabgabRepository.findCabGabById(data) ?: return null

        return CabGabRequest().apply {
            this.cabgabId = row["cabgab_id"] as? String
            cabgabContent = row["cabgab_content"] as? String
            cabgabAnswer1 = row["cabgab_answer1"] as? String
            cabgabAnswer2 = row["cabgab_answer2"] as? String
            cabgabAnswer3 = row["cabgab_answer3"] as? String
            cabgabAnswer4 = row["cabgab_answer4"] as? String
            cabgabCorrectAnswer = (row["cabgab_correct_answer"] as? Number)?.toInt()
            cabgabCategory = row["cabgab_category"] as? String
        }
    }

    @Transactional
    fun insertCabGab(request: CabGabRequest): CabGabResponse {
        val response = CabGabResponse()
        val data = CabGabData().apply {
            cabgabId = UUID.randomUUID().toString()
            cabgabContent = request.cabgabContent
            cabgabAnswer1 = request.cabgabAnswer1
            cabgabAnswer2 = request.cabgabAnswer2
            cabgabAnswer3 = request.cabgabAnswer3
            cabgabAnswer4 = request.cabgabAnswer4
            cabgabCorrectAnswer = request.cabgabCorrectAnswer
            cabgabCategory = request.cabgabCategory
        }
        try{
            cabgabRepository.insertCabGabMaster(data)
            response.message = MessageConfig.SPI_INSERT_SUCCESS
        } catch (e: Exception) {
            response.message = MessageConfig.SPI_INSERT_FAILED
        }
        return response
    }

    @Transactional
    fun updateCabGab(request: CabGabRequest): CabGabResponse {
        val response = CabGabResponse()
        val data = CabGabData().apply {
            cabgabId = request.cabgabId // IDは画面から渡ってきたものを使用
            cabgabContent = request.cabgabContent
            cabgabAnswer1 = request.cabgabAnswer1
            cabgabAnswer2 = request.cabgabAnswer2
            cabgabAnswer3 = request.cabgabAnswer3
            cabgabAnswer4 = request.cabgabAnswer4
            cabgabCorrectAnswer = request.cabgabCorrectAnswer
            cabgabCategory = request.cabgabCategory
        }
        try {
            cabgabRepository.updateCabGabMaster(data)
            response.message = MessageConfig.SPI_UPDATE_SUCCESS
        } catch (e: Exception) {
            response.message = MessageConfig.SPI_UPDATE_FAILED
        }
        return response
    }

    @Transactional
    fun deleteCabGab(cabgabId: String): CabGabResponse {
        val response = CabGabResponse()
        val data = CabGabData().apply { this.cabgabId = cabgabId }
        try {
            cabgabRepository.deleteCabGabMaster(data)
            response.message = MessageConfig.SPI_DELETE_SUCCESS
        } catch (e: Exception) {
            response.message = MessageConfig.SPI_DELETE_FAILED
        }
        return response
    }

    fun getCabGabCount(): Int {
        return cabgabRepository.countCabGab()
    }
}