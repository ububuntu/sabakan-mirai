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

/**
 * CABGAB試験関連のサービスクラス
 */
@Service
class CabGabService {

    @Autowired
    lateinit var cabgabRepository: CabGabRepository

    /**
     * CABGAB試験開始処理
     *
     * @param request CABGAB試験開始リクエスト
     * @return 生成されたCABGAB試験ID
     */
    @Transactional
    fun startExam(request: CabGabRequest): String {
        val hsId = "S"+UUID.randomUUID().toString()

        // 1. 履歴ヘッダ作成
        val history = CabGabHistoryData().apply {
            cabgabHsId = hsId
            userId = request.userId
        }
        cabgabRepository.insertHistory(history)

        // 2. 問題ランダム抽出 (言語40問、非言語30問)
        // 引数はDataクラスに詰めて渡す
        val verbal = cabgabRepository.getRandomQuestions(CabGabData().apply { cabgabCategory = "言語" }, 40)
        val nonVerbal = cabgabRepository.getRandomQuestions(CabGabData().apply { cabgabCategory = "非言語" }, 30)

        val allQuestions = verbal + nonVerbal

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

    /**
     * CABGAB試験の回答保存処理
     *
     * @param request CABGAB試験回答リクエスト
     */
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

    /**
     * CABGAB試験終了処理
     *
     * @param request CABGAB試験終了リクエスト
     */
    @Transactional
    fun finishExam(request: CabGabRequest) {
        val data = CabGabHistoryData().apply { cabgabHsId = request.cabgabHsId }
        cabgabRepository.updateHistoryFinished(data)
    }

    /**
     * CABGAB試験結果取得処理
     *
     * @param request CABGAB試験結果リクエスト
     * @return CABGAB試験結果リスト
     */
    fun getExamResults(request: CabGabRequest): List<Map<String, Any?>> {
        val data = CabGabHistoryData().apply { cabgabHsId = request.cabgabHsId }
        return cabgabRepository.findDetailsWithQuestion(data)
    }

    /**
     * 進行中のCABGAB試験ID取得処理
     *
     * @param request CABGAB試験リクエスト
     * @return 進行中のCABGAB試験ID
     */
    fun getInProgressCabGabId(request: CabGabRequest): String? {
        val data = CabGabHistoryData().apply { userId = request.userId }
        return cabgabRepository.findInProgressId(data)
    }

    /**
     * CABGAB試験履歴一覧取得処理
     *
     * @param request CABGAB試験リクエスト
     * @return CABGAB試験履歴リスト
     */
    fun getHistoryList(request: CabGabRequest): List<Map<String, Any?>> {
        val data = CabGabHistoryData().apply { userId = request.userId }
        return cabgabRepository.findHistoryByUserId(data)
    }

    /**
     * 全CABGAB問題取得処理（管理者用）
     *
     * @return CABGAB問題リスト
     */
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

    /**
     * CABGAB問題IDからCABGAB問題取得処理（管理者用）
     *
     * @param cabgabId CABGAB問題ID
     * @return CABGAB問題
     */
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

    /**
     * CABGAB問題登録処理（管理者用）
     *
     * @param request CABGAB問題登録リクエスト
     * @return CABGAB問題登録レスポンス
     */
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
            response.message = MessageConfig.CABGAB_INSERT_SUCCESS
        } catch (e: Exception) {
            response.message = MessageConfig.CABGAB_INSERT_FAILED
        }
        return response
    }

    /**
     * CABGAB問題更新処理（管理者用）
     *
     * @param request CABGAB問題更新リクエスト
     * @return CABGAB問題更新レスポンス
     */
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
            response.message = MessageConfig.CABGAB_UPDATE_SUCCESS
        } catch (e: Exception) {
            response.message = MessageConfig.CABGAB_UPDATE_FAILED
        }
        return response
    }

    /**
     * CABGAB問題削除処理（管理者用）
     *
     * @param cabgabId CABGAB問題ID
     * @return CABGAB問題削除レスポンス
     */
    @Transactional
    fun deleteCabGab(cabgabId: String): CabGabResponse {
        val response = CabGabResponse()
        val data = CabGabData().apply { this.cabgabId = cabgabId }
        try {
            cabgabRepository.deleteCabGabMaster(data)
            response.message = MessageConfig.CABGAB_DELETE_SUCCESS
        } catch (e: Exception) {
            response.message = MessageConfig.CABGAB_DELETE_FAILED
        }
        return response
    }

    /**
     * CABGAB問題数取得処理（管理者用）
     *
     * @return CABGAB問題数
     */
    fun getCabGabCount(): Int {
        return cabgabRepository.countCabGab()
    }
}