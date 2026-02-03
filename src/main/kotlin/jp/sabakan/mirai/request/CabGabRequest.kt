package jp.sabakan.mirai.request

import lombok.Data

@Data
class CabGabRequest {
    // SPI受検ID
    var cabgabId: String? = null

    // SPI履歴ID
    var cabgabHsId : String? = null

    // 問題番号
    var questionNumber: Int? = null

    // 問題文
    var cabgabContent: String? = null

    // 選択肢1
    var cabgabAnswer1: String? = null

    // 選択肢2
    var cabgabAnswer2: String? = null

    // 選択肢3
    var cabgabAnswer3: String? = null

    // 選択肢4
    var cabgabAnswer4: String? = null

    // 正解番号
    var cabgabCorrectAnswer: Int? = null

    // 問題種別
    var cabgabCategory: String? = null

    // 回答結果ID
    var cabgabResultId: String? = null

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
    class CabGabExamRequest {
        var userId: String? = null
        var answers: List<CabGabAnswerItem>? = null
    }

    // 1問ごとの回答内容
    @Data
    class CabGabAnswerItem {
        var cabgabId: String? = null
        var userAnswer: Int? = null
    }
}