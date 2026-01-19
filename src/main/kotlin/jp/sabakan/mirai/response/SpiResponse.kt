package jp.sabakan.mirai.response

import jp.sabakan.mirai.entity.SpiEntity
import lombok.Data

@Data
class SpiResponse {
    // SPI問題一覧
    var spis: List<SpiEntity>? = null

    // 返却メッセージ
    var message: String? = null
}