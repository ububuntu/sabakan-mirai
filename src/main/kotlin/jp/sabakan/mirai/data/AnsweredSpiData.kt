package jp.sabakan.mirai.data

import lombok.Data

@Data
class AnsweredSpiData {
    // 回答結果ID
    var spiResultId: String? = null

    // SPI ID
    var spiId: String? = null

    // ユーザーID
    var userId: String? = null

    // ユーザーの回答
    var userAnswer: String? = null

    // 正解の回答
    var correctAnswer: String? = null

    // 回答日時
    var answerDate: String? = null
}