package jp.sabakan.mirai.response

import jp.sabakan.mirai.entity.EsEntity
import lombok.Data

@Data
class EsResponse {
    // ES一覧
    var esList: List<EsEntity>? = null

    // 返却メッセージ
    var message: String? = null
}