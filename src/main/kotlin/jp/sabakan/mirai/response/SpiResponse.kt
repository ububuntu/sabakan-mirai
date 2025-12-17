package jp.sabakan.mirai.response

import jp.sabakan.mirai.entity.AnsweredSpiEntity
import jp.sabakan.mirai.entity.SpiEntity
import lombok.Data

@Data
class SpiResponse {
    // SPI問題一覧
    var spis: List<SpiEntity>? = null

    // AnsweredSpi一覧
    var answeredSpis: List<AnsweredSpiEntity>? = null

    // 返却メッセージ
    var message: String? = null
}