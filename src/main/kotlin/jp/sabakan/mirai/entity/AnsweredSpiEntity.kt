package jp.sabakan.mirai.entity

import lombok.Data

@Data
class AnsweredSpiEntity {
    // 回答結果ID
    var spiResultId: String? = null

    // Spi ID
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