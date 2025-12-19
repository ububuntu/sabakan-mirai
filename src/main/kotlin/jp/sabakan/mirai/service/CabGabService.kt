package jp.sabakan.mirai.service

import jp.sabakan.mirai.MessageConfig
import jp.sabakan.mirai.data.AnsweredCabGabData
import jp.sabakan.mirai.data.CabGabData
import jp.sabakan.mirai.entity.CabGabEntity
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
    lateinit var cabGabRepository: CabGabRepository

    /**
     * CabGab質問を登録する
     *
     * @param request CabGabリクエスト
     * @return CabGabレスポンス
     */
    fun insertCabGab(request: CabGabRequest): CabGabResponse {
        //リクエストからデータ変換
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

        //リポジトリへ登録処理
        val insertCount = cabGabRepository.insertCabGab(data)

        // 登録結果を確認
        if (insertCount == 0) {
            return CabGabResponse().apply {
                message = MessageConfig.CABGAB_INSERT_FAILED
            }
        }

        //結果を返す
        return CabGabResponse().apply {
            message = MessageConfig.CABGAB_INSERT_SUCCESS
        }
    }

    // #TODO rateとresultを分割する必要がある
    /**
     * CabGab回答を登録する
     *
     * @param request CabGabリクエスト
     * @return CabGabレスポンス
     */
    @Transactional
    fun insertCabGabAnswer(request: CabGabRequest): CabGabResponse {
        //リクエストからデータ変換
        val data = AnsweredCabGabData()
        data.cabgabResultId = toCreateCabGabResultId()
        data.cabGabId = request.cabGabId
        data.userId = request.userId
        data.userAnswer = request.userAnswer
        data.correctAnswer = request.correctAnswer

        //リポジトリへ登録処理
        val update = cabGabRepository.insertCabGabAnswer(data)

        // 登録結果を確認
        if (update == 0) {
            return CabGabResponse().apply {
                message = null
            }
        }

        // パラメータマップの作成
        val rateUpdate = cabGabRepository.updateCabGabAnswerRate(data)

        // 登録結果を確認
        if (rateUpdate == 0) {
            val rateInsertCount  = cabGabRepository.insertCabGabAnswerRate(data)

            if (rateInsertCount == 0) {
                throw RuntimeException("Failed to insert Cab/Gab answer.")
            }
        } else if (rateUpdate > 1) {
            // 多分ありえないエラー
            throw IllegalStateException("Failed to update Cab/Gab answer.")
        }

        //結果を返す
        return CabGabResponse().apply {
            message = null
        }
    }

    /**
     * 指定カテゴリーのCabGab質問を取得する
     *
     * @param request CabGabリクエスト
     * @return CabGabレスポンス
     */
    fun getCabGab(request: CabGabRequest): CabGabResponse {
        // リクエストからデータ変換
        val data = CabGabData()
        data.cabGabCategory = request.cabGabCategory

        // リポジトリへ問い合わせ
        val table: List<Map<String, Any?>> = cabGabRepository.getCabGab(data)
        val list: List<CabGabEntity> = tableToListEntity(table)

        // 結果を返す
        return CabGabResponse().apply {
            cabGabs = list
            message = null
        }
    }

    /**
     * CabGab質問リストを取得する
     *
     * @param request CabGabリクエスト
     * @return CabGabレスポンス
     */
    fun getCabGabList(request: CabGabRequest): CabGabResponse {
        //リクエストからデータ変換
        val data = AnsweredCabGabData()
        data.userId = request.userId

        //リポジトリへ問い合わせ
        val table: List<Map<String, Any?>> = cabGabRepository.getCabGabList(data)
        val list: List<CabGabEntity> = tableToListEntity(table)

        //結果を返す
        return CabGabResponse().apply {
            cabGabs = list
            message = null
        }
    }

    /**
     * CabGab質問を削除する
     *
     * @param request CabGabリクエスト
     * @return CabGabレスポンス
     */
    fun deleteCabGab(request: CabGabRequest): CabGabResponse {
        //リクエストからデータ変換
        val data = AnsweredCabGabData()
        data.cabGabId = request.cabGabId
        data.userId = request.userId

        //リポジトリへ削除処理
        val deleteCount = cabGabRepository.deleteCabGab(data)

        // 結果を返す
        return if (deleteCount == 0) {
            CabGabResponse().apply {
                message = MessageConfig.CABGAB_DELETE_FAILED
            }
        } else {
            CabGabResponse().apply {
                message = MessageConfig.CABGAB_DELETE_SUCCESS
            }
        }
    }

    /**
     * ユーザのCabGab回答を削除する
     *
     * @param request CabGabリクエスト
     * @return CabGabレスポンス
     */
    fun deleteCabGabByUser(request: CabGabRequest): CabGabResponse {
        //リクエストからデータ変換
        val data = AnsweredCabGabData()
        data.userId = request.userId

        //リポジトリへ削除処理
        val deleteCount = cabGabRepository.deleteCabGabByUser(data)

        // 結果を返す
        return if (deleteCount == 0) {
            CabGabResponse().apply {
                message = MessageConfig.CABGAB_DELETE_FAILED
            }
        } else {
            CabGabResponse().apply {
                message = MessageConfig.CABGAB_DELETE_SUCCESS
            }
        }
    }

    /**
     * 新しいCabGabIDを生成する
     *
     * @return 新しいCabGabID
     */
    private fun toCreateCabGabId(): String{
        // UUIDを生成
        val uuid = UUID.randomUUID().toString()

        // 新しいCabGabIDの生成
        return "C$uuid"
    }

    /**
     * 新しいCabGabResultIDを生成する
     *
     * @return 新しいCabGabResultID
     */
    private fun toCreateCabGabResultId(): String{
        // UUIDを生成
        val uuid = UUID.randomUUID().toString()

        // 新しいCabGabResultIDの生成
        return "CR$uuid"
    }

    /**
     * テーブルデータをCabGabEntityリストに変換する
     *
     * @param table テーブルデータ
     * @return CabGabEntityリスト
     */
    fun tableToListEntity(table: List<Map<String, Any?>>): List<CabGabEntity> {
        val list: MutableList<CabGabEntity> = mutableListOf()
        for (row in table) {
            val entity = CabGabEntity().apply {
                cabGabId = row["cabgab_id"] as String?
                cabGabContent = row["cabgab_content"] as String?
                cabGabAnswer1 = row["cabgab_answer1"] as String?
                cabGabAnswer2 = row["cabgab_answer2"] as String?
                cabGabAnswer3 = row["cabgab_answer3"] as String?
                cabGabAnswer4 = row["cabgab_answer4"] as String?
                cabGabCorrectAnswer = (row["cabgab_correct_answer"] as Number?)?.toInt()
                cabGabCategory = row["cabgab_category"] as String?
            }
            list.add(entity)
        }
        return list
    }
}