package jp.sabakan.mirai.entity

import lombok.Data

@Data
class CabGabEntity {
    // CAB/GAB受検ID
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
}