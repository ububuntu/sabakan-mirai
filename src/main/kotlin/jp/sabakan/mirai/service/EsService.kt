package jp.sabakan.mirai.service

import jp.sabakan.mirai.MessageConfig
import jp.sabakan.mirai.data.EsData
import jp.sabakan.mirai.entity.EsEntity
import jp.sabakan.mirai.repository.EsRepository
import jp.sabakan.mirai.request.EsRequest
import jp.sabakan.mirai.response.EsResponse
import org.apache.logging.log4j.message.Message
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import java.util.UUID

@Service
class EsService {
    @Autowired
    lateinit var esRepository: EsRepository

    /**
     * 指定ユーザのES一覧を取得する
     *
     * @param userId ユーザID
     * @return ESリスト
     */
    fun getEsList(request: EsRequest): EsResponse {
        // リクエストからデータ変換
        val data = EsData()
        data.userId = request.userId

        // リポジトリへ問い合わせ
        val table: List<Map<String, Any?>> = esRepository.getEsList(data)
        val list: List<EsEntity> = tableToListEntity(table)

        // 結果を返す
        return EsResponse().apply {
            esList = list
        }
    }

    /**
     * ESを新規登録する
     *
     * @param request ESリクエスト
     * @return ESレスポンス
     */
    fun insertEs(request: EsRequest): EsResponse {
        // リクエストからデータ変換
        val data = EsData().apply {
            esId = toCreateEsId()
            userId = request.userId
            esContentReason = request.esContentReason
            esContentSelfpr = request.esContentSelfpr
            esContentActivities = request.esContentActivities
            esContentStwe = request.esContentStwe
            esOccupation = request.esOccupation
        }

        // リポジトリへ登録処理
        val insertCount = esRepository.insertEs(data)

        // 登録結果を確認してレスポンスを返す
        return if (insertCount == 0) {
            EsResponse().apply {
                message = MessageConfig.ES_INSERT_FAILED
            }
        } else {
            EsResponse().apply {
                message = MessageConfig.ES_INSERT_SUCCESS
            }
        }
    }

    /**
     * ESを更新する
     *
     * @param request ESリクエスト
     * @return ESレスポンス
     */
    fun updateEs(request: EsRequest): EsResponse {
        // リクエストからデータ変換
        val data = EsData().apply {
            esId = request.esId
            userId = request.userId
            esContentReason = request.esContentReason
            esContentSelfpr = request.esContentSelfpr
            esContentActivities = request.esContentActivities
            esContentStwe = request.esContentStwe
            esOccupation = request.esOccupation
        }

        // リポジトリへ更新処理
        val updateCount = esRepository.updateEs(data)

        // 更新結果を確認してレスポンスを返す
        return if (updateCount == 0) {
            EsResponse().apply {
                message = MessageConfig.ES_UPDATE_FAILED
            }
        } else {
            EsResponse().apply {
                message = MessageConfig.ES_UPDATE_SUCCESS
            }
        }
    }

    /**
     * ESを削除する
     *
     * @param request ESリクエスト
     * @return ESレスポンス
     */
    fun deleteEs(request: EsRequest): EsResponse {
        // リクエストからデータ変換
        val data = EsData().apply {
            esId = toCreateEsId()
            userId = request.userId
        }

        // リポジトリへ削除処理
        val deleteCount = esRepository.deleteEs(data)

        // 削除結果を確認してレスポンスを返す
        return if (deleteCount == 0) {
            EsResponse().apply {
                message = MessageConfig.ES_DELETE_FAILED
            }
        } else {
            EsResponse().apply {
                message = MessageConfig.ES_DELETE_SUCCESS
            }
        }
    }

    /**
     * 新しいESIDを生成する
     *
     * @return 新しいESID
     */
    private fun toCreateEsId(): String {
        // UUIDを生成
        val uuid = UUID.randomUUID().toString()

        // 新しいESIDの生成
        return "E$uuid"
    }


    /**
     * テーブルデータをEsEntityリストに変換する
     *
     * @param table テーブルデータ
     * @return EsEntityリスト
     */
    fun tableToListEntity(table: List<Map<String, Any?>>): List<EsEntity> {
        return table.map { row ->
            EsEntity().apply {
                esId = row["esId"] as String?
                userId = row["userId"] as String?
                esContentReason = row["esContentReason"] as String?
                esContentSelfpr = row["esContentSelfpr"] as String?
                esContentActivities = row["esContentActivities"] as String?
                esContentStwe = row["esContentStwe"] as String?
                esOccupation = row["esOccupation"] as String?
                esDate = row["esDate"] as String?
            }
        }
    }
}