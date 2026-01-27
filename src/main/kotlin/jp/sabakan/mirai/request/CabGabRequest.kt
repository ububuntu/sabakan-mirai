package jp.sabakan.mirai.request

import lombok.Data

@Data
class CabGabRequest {
    // Cab・Gab受検ID
    var cabGabId: String? = null

    // 問題文
    var cabGabContent: String? = null

    // 選択肢1
    var cabGabAnswer1: String? = null

    // 選択肢2
    var cabGabAnswer2: String? = null

    // 選択肢3
    var cabGabAnswer3: String? = null

    // 選択肢4
    var cabGabAnswer4: String? = null

    // 正解番号
    var cabGabCorrectAnswer: Int? = null

    // 問題種別
    var cabGabCategory: String? = null

    // 回答結果ID
    var cabGabResultId: String? = null

    // ユーザーID
    var userId: String? = null

    // ユーザーの回答
    var userAnswer: String? = null

    // 正解の回答
    var correctAnswer: String? = null

    // 回答日時
    var answerDate: String? = null

    // 90問分の回答を一括送信するためのリクエスト
    @Data
    class CabGabExamRequest {
        var userId: String? = null
        var answers: List<CabGabAnswerItem>? = null
    }

    // 1問ごとの回答内容
    @Data
    class CabGabAnswerItem {
        var cabGabId: String? = null
        var userAnswer: Int? = null
    }
}