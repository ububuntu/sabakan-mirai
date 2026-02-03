package jp.sabakan.mirai.data

import lombok.Data
import java.time.LocalDateTime

@Data
class CabGabHistoryData {
    // 履歴ID (PK)
    var cabgabHsId: String? = null

    // ユーザーID
    var userId: String? = null

    // 実施日時
    var cabgabHsDate: LocalDateTime? = null

    // 試験完了フラグ
    var isFinished: Boolean = false
}