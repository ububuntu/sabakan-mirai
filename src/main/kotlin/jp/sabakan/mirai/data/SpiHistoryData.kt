package jp.sabakan.mirai.data

import lombok.Data
import java.time.LocalDateTime

@Data
class SpiHistoryData {
    // 履歴ID (PK)
    var spiHsId: String? = null

    // ユーザーID
    var userId: String? = null

    // 実施日時
    var spiHsDate: LocalDateTime? = null

    // 試験完了フラグ
    var isFinished: Boolean = false
}