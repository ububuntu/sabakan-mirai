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
     * 指定ESIDのES詳細を取得する
     *
     * @param esId ESID
     * @param userId ユーザID
     * @return ESリクエスト
     */
    fun getEsDetail(esId: String, userId: String): EsRequest {
        val row = esRepository.getEsById(esId, userId) ?: return EsRequest() // なければ空を返す

        // MapからEsRequestへ詰め替え
        return EsRequest().apply {
            this.esId = row["es_id"] as String? // カラム名はDB定義に合わせてください(スネークケースの場合あり)
            this.userId = row["user_id"] as String?
            this.esOccupation = row["es_occupation"] as String?
            this.esContentReason = row["es_content_reason"] as String?
            this.esContentSelfpr = row["es_content_selfpr"] as String?
            this.esContentActivities = row["es_content_activities"] as String?
            this.esContentStwe = row["es_content_stwe"] as String?
        }
    }

    /**
     * ESを保存する（新規作成または更新）
     *
     * @param request ESリクエスト
     */
    fun saveEs(request: EsRequest) {
        val data = EsData().apply {
            userId = request.userId
            esOccupation = request.esOccupation
            esContentReason = request.esContentReason
            esContentSelfpr = request.esContentSelfpr
            esContentActivities = request.esContentActivities
            esContentStwe = request.esContentStwe
        }

        if (request.esId.isNullOrEmpty()) {
            // 新規作成
            data.esId = toCreateEsId() // UUID生成など
            esRepository.insertEs(data)
        } else {
            // 更新
            data.esId = request.esId
            esRepository.updateEs(data)
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