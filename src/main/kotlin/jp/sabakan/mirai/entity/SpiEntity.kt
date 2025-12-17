package jp.sabakan.mirai.entity

import lombok.Data

@Data
class SpiEntity {
    // SPI受検ID
    var spiId: String? = null

    // 問題文
    var spiContent: String? = null

    // 選択肢1
    var spiAnswer1: String? = null

    // 選択肢2
    var spiAnswer2: String? = null

    // 選択肢3
    var spiAnswer3: String? = null

    // 選択肢4
    var spiAnswer4: String? = null

    // 正解番号
    var spiCorrectAnswer: Int? = null

    // 問題種別
    var spiCategory: String? = null
}