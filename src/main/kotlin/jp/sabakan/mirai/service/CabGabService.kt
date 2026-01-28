package jp.sabakan.mirai.service

import jp.sabakan.mirai.MessageConfig
import jp.sabakan.mirai.data.CabGabData
import jp.sabakan.mirai.data.CabGabDetailData
import jp.sabakan.mirai.data.CabGabHistoryData
import jp.sabakan.mirai.entity.CabGabEntity
import jp.sabakan.mirai.repository.CabGabRepository
import jp.sabakan.mirai.request.CabGabRequest
import jp.sabakan.mirai.request.SpiRequest
import jp.sabakan.mirai.response.CabGabResponse
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.math.BigDecimal
import java.math.RoundingMode
import java.util.UUID

@Service
class CabGabService {
    @Autowired
    lateinit var cabGabRepository: CabGabRepository

    /**
     * 新しいCabGab試験を開始する
     *
     * @param userId ユーザーID
     * @return 新しいCabGab試験ID
     */
    @Transactional
    fun startNewCabGab(request: CabGabRequest): String? {
        val userId = request.userId ?: return null
        // 新しいCabGab試験IDを生成
        val historyId = UUID.randomUUID().toString()

        // CabGab履歴データを作成
        val historyData = CabGabHistoryData().apply {
            cabGabHsId = historyId
            this.userId = userId
            totalQuestions = 50 // CabGabの標準問題数など、運用に合わせて調整
            correctCount = 0
            accuracyRate = BigDecimal.ZERO
            cabGabHsDate = java.time.LocalDateTime.now()
        }

        // Repositoryを使って履歴を保存
        cabGabRepository.insertHistory(historyData)

        return historyId
    }

    /**
     * すべてのCabGab質問を取得する
     */
    fun getAllCabGab(): List<CabGabEntity> {
        val table: List<Map<String, Any?>> = cabGabRepository.getAllCabGab()
        return tableToListEntity(table)
    }

    /**
     * 指定CabGab IDの質問を取得する
     */
    fun getCabGabById(cabGabId: String): CabGabRequest {
        val data = CabGabData().apply {
            this.cabGabId = cabGabId
        }

        val table: List<Map<String, Any?>> = cabGabRepository.getCabGabById(data)

        if (table.isEmpty()) {
            throw IllegalArgumentException("指定されたIDの問題が見つかりません: $cabGabId")
        }

        val row = table[0]

        return CabGabRequest().apply {
            this.cabGabId = row["cabgab_id"] as? String
            cabGabContent = row["cabgab_content"] as? String
            cabGabAnswer1 = row["cabgab_answer1"] as? String
            cabGabAnswer2 = row["cabgab_answer2"] as? String
            cabGabAnswer3 = row["cabgab_answer3"] as? String
            cabGabAnswer4 = row["cabgab_answer4"] as? String
            cabGabCorrectAnswer = row["cabgab_correct_answer"] as? Int
            cabGabCategory = row["cabgab_category"] as? String
        }
    }

    /**
     * CabGab質問を新規登録する
     */
    fun insertCabGab(request: CabGabRequest): CabGabResponse {
        val data = CabGabData().apply {
            cabGabId = toCreateId()
            cabGabContent = request.cabGabContent
            cabGabAnswer1 = request.cabGabAnswer1
            cabGabAnswer2 = request.cabGabAnswer2
            cabGabAnswer3 = request.cabGabAnswer3
            cabGabAnswer4 = request.cabGabAnswer4
            cabGabCorrectAnswer = request.cabGabCorrectAnswer
            cabGabCategory = request.cabGabCategory
        }

        val insertCount = cabGabRepository.insertCabGab(data)

        if (insertCount == 0) {
            return CabGabResponse().apply {
                message = MessageConfig.CABGAB_INSERT_FAILED
            }
        }

        return CabGabResponse().apply {
            message = MessageConfig.CABGAB_INSERT_SUCCESS
        }
    }

    /**
     * 指定カテゴリーのCabGab質問を取得する
     */
    fun getCabGab(request: CabGabRequest): CabGabResponse {
        val data = CabGabData().apply {
            cabGabCategory = request.cabGabCategory
        }

        val table: List<Map<String, Any?>> = cabGabRepository.getCabGab(data)
        val list: List<CabGabEntity> = tableToListEntity(table)

        return CabGabResponse().apply {
            cabGabs = list
            message = null
        }
    }

    /**
     * 進行中のCabGab試験IDを取得する
     */
    fun getInProgressCabGabId(request: CabGabRequest): String? {
        val userId = request.userId ?: return null
        return cabGabRepository.getUnfinishedCabGabHsId(userId)
    }

    /**
     * 現在の質問インデックスを取得する
     */
    fun getCurrentCabGabIndex(examId: String): Int {
        val answeredCount = cabGabRepository.countDetailsByHistoryId(examId)
        return answeredCount + 1
    }

    /**
     * CabGabの1つの回答を保存する
     */
    @Transactional
    fun saveOneCabGabAnswer(examId: String, cabGabId: String, userAnswer: Int) {
        val correctAnswer = cabGabRepository.getCorrectAnswer(cabGabId)
        val isCorrect = (correctAnswer != null && correctAnswer == userAnswer)

        val detailData = CabGabDetailData().apply {
            cabGabDlId = UUID.randomUUID().toString()
            cabGabHsId = examId
            this.cabGabId = cabGabId
            this.userAnswer = userAnswer
            this.isCorrect = isCorrect
        }

        cabGabRepository.insertDetail(detailData)
    }

    /**
     * CabGab試験を完了し、結果を更新する
     */
    @Transactional
    fun finishCabGab(cabGabHsId: String) {
        val results: List<Boolean> = cabGabRepository.findDetailsByHistoryId(cabGabHsId)
        val totalAnswered = results.size
        val correctCount = results.count { it }

        val accuracyRate = if (totalAnswered > 0) {
            BigDecimal(correctCount)
                .divide(BigDecimal(totalAnswered), 2, RoundingMode.HALF_UP)
                .multiply(BigDecimal("100"))
        } else {
            BigDecimal.ZERO
        }

        cabGabRepository.updateExamResult(cabGabHsId, correctCount, accuracyRate)
    }

    /**
     * まとめてCabGab試験結果を登録する
     */
    @Transactional
    fun registerCabGabResult(request: CabGabRequest.CabGabExamRequest): CabGabHistoryData {
        val historyId = UUID.randomUUID().toString()
        val historyData = CabGabHistoryData().apply {
            cabGabHsId = historyId
            userId = request.userId
            totalQuestions = request.answers?.size ?: 0
            correctCount = 0
            cabGabHsDate = java.time.LocalDateTime.now()
        }

        request.answers?.forEach { answerItem ->
            val currentCabGabId = answerItem.cabGabId ?: return@forEach
            val userAns = answerItem.userAnswer

            val correctAnswer = cabGabRepository.getCorrectAnswer(currentCabGabId)
            val isCorrect = (correctAnswer != null && correctAnswer == userAns)

            if (isCorrect) historyData.correctCount++

            val detailData = CabGabDetailData().apply {
                cabGabDlId = UUID.randomUUID().toString()
                cabGabHsId = historyId
                cabGabId = currentCabGabId
                userAnswer = userAns
                this.isCorrect = isCorrect
            }
            cabGabRepository.insertDetail(detailData)
        }

        historyData.accuracyRate = if (historyData.totalQuestions > 0) {
            historyData.correctCount.toBigDecimal()
                .divide(historyData.totalQuestions.toBigDecimal(), 2, RoundingMode.HALF_UP)
                .multiply(BigDecimal("100"))
        } else {
            BigDecimal.ZERO
        }

        cabGabRepository.insertHistory(historyData)
        return historyData
    }

    /**
     * CabGab質問を更新する
     */
    fun updateCabGab(request: CabGabRequest): CabGabResponse {
        val data = CabGabData().apply {
            cabGabId = request.cabGabId
            cabGabContent = request.cabGabContent
            cabGabAnswer1 = request.cabGabAnswer1
            cabGabAnswer2 = request.cabGabAnswer2
            cabGabAnswer3 = request.cabGabAnswer3
            cabGabAnswer4 = request.cabGabAnswer4
            cabGabCorrectAnswer = request.cabGabCorrectAnswer
            cabGabCategory = request.cabGabCategory
        }

        val updateCount = cabGabRepository.updateCabGab(data)

        if (updateCount == 0) {
            return CabGabResponse().apply { message = MessageConfig.CABGAB_UPDATE_FAILED }
        }

        return CabGabResponse().apply { message = MessageConfig.CABGAB_UPDATE_SUCCESS }
    }

    /**
     * CabGab質問を削除する
     */
    fun deleteCabGab(cabGabId: String): CabGabResponse {
        val deleteCount = cabGabRepository.deleteCabGab(cabGabId)

        if (deleteCount == 0) {
            return CabGabResponse().apply { message = MessageConfig.CABGAB_DELETE_FAILED }
        }

        return CabGabResponse().apply { message = MessageConfig.CABGAB_DELETE_SUCCESS }
    }

    /**
     * 新しいCabGab IDを生成する (接頭辞 'C')
     */
    private fun toCreateId(): String {
        return "C${UUID.randomUUID()}"
    }

    /**
     * テーブルデータをCabGabEntityリストに変換する
     */
    fun tableToListEntity(table: List<Map<String, Any?>>): List<CabGabEntity> {
        return table.map { row ->
            CabGabEntity().apply {
                cabGabId = row["cabgab_id"] as String?
                cabGabContent = row["cabgab_content"] as String?
                cabGabAnswer1 = row["cabgab_answer1"] as String?
                cabGabAnswer2 = row["cabgab_answer2"] as String?
                cabGabAnswer3 = row["cabgab_answer3"] as String?
                cabGabAnswer4 = row["cabgab_answer4"] as String?
                cabGabCorrectAnswer = (row["cabgab_correct_answer"] as? Number)?.toInt()
                cabGabCategory = row["cabgab_category"] as String?
            }
        }
    }
}