package jp.sabakan.mirai.service

import jp.sabakan.mirai.MessageConfig
import jp.sabakan.mirai.data.EsData
import jp.sabakan.mirai.entity.EsEntity
import jp.sabakan.mirai.repository.EsRepository
import jp.sabakan.mirai.request.EsRequest
import jp.sabakan.mirai.response.EsResponse
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
    fun getEsDetail(esRequest: EsRequest): List<EsEntity> {
        val data = EsData().apply {
            this.esId = esRequest.esId
            this.userId = esRequest.userId
        }

        val table = esRepository.getEsById(data)

        return tableToListEntity(table)
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
     */
    fun deleteEs(request: EsRequest): EsResponse {
        // リクエストからデータ変換
        val data = EsData().apply {
            this.esId = request.esId
            this.userId = request.userId
        }

        // リポジトリへ削除処理
        val deleteCount = esRepository.deleteEs(data)

        // 結果の返却
        return if (deleteCount == 0) {
            EsResponse().apply { message = MessageConfig.ES_DELETE_FAILED }
        } else {
            EsResponse().apply { message = MessageConfig.ES_DELETE_SUCCESS }
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
                // 左側はEntityのプロパティ（キャメル）、右側はDBのカラム名（スネーク）
                esId = row["es_id"] as String?
                userId = row["user_id"] as String?
                esContentReason = row["es_content_reason"] as String?
                esContentSelfpr = row["es_content_selfpr"] as String?
                esContentActivities = row["es_content_activities"] as String?
                esContentStwe = row["es_content_stwe"] as String?
                esOccupation = row["es_occupation"] as String?
                esDate = row["es_date"]?.toString() // DATE型をStringへ
            }
        }
    }
}