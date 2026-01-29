package jp.sabakan.mirai.request

import lombok.Data

@Data
class SpiRequest {
    // SPI受検ID
    var spiId: String? = null

    // SPI履歴ID
    var spiHsId : String? = null

    // 問題番号
    var questionNumber: Int? = null

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

    // 回答結果ID
    var spiResultId: String? = null

    // ユーザーID
    var userId: String? = null

    // ユーザーの回答
    var userAnswer: Int? = null

    // 正解の回答
    var correctAnswer: String? = null

    // 回答日時
    var answerDate: String? = null

    // 70問分の回答を一括送信するためのリクエスト
    @Data
    class SpiExamRequest {
        var userId: String? = null
        var answers: List<SpiAnswerItem>? = null
    }

    // 1問ごとの回答内容
    @Data
    class SpiAnswerItem {
        var spiId: String? = null
        var userAnswer: Int? = null
    }
}