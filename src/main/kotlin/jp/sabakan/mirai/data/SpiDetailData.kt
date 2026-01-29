package jp.sabakan.mirai.data

import lombok.Data

@Data
class SpiDetailData {
    // 明細ID (PK)
    var spiDlId: String? = null

    // 履歴ID (FK)
    var spiHsId: String? = null

    // 【重要】出題番号 (1~70) を管理するために追加
    var questionNumber: Int = 0

    // 問題ID (FK)
    var spiId: String? = null

    // ユーザーの回答 (1~4)
    var userAnswer: Int? = null

    // 正誤判定 (true/false)
    var isCorrect: Boolean = false
}