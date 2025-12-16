package jp.sabakan.mirai.response

import jp.sabakan.mirai.entity.AnsweredCabGabEntity
import jp.sabakan.mirai.entity.CabGabEntity
import lombok.Data

@Data
class CabGabResponse {
    // CAB/GAB問題一覧
    var cabGabs: List<CabGabEntity>? = null

    // AnsweredCabGab一覧
    var answeredCabGabs: List<AnsweredCabGabEntity>? = null

    // 返却メッセージ
    var message: String? = null
}