package jp.sabakan.mirai.data

import lombok.Data

@Data
class CabGabData {
    // CAB/GAB受検ID
    var cabgabId: String? = null

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
}