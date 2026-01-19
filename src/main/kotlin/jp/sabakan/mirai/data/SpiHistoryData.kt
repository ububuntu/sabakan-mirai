package jp.sabakan.mirai.data

import lombok.Data
import java.math.BigDecimal
import java.time.LocalDateTime

@Data
class SpiHistoryData {
    // 履歴ID (PK)
    var spiHsId: String? = null

    // ユーザーID
    var userId: String? = null

    // 出題数 (例: 70)
    var totalQuestions: Int = 0

    // 正解数
    var correctCount: Int = 0

    // 正答率 (%)
    var accuracyRate: BigDecimal? = null

    // 実施日時
    var spiHsDate: LocalDateTime? = null
}