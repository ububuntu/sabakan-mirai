package jp.sabakan.mirai.data

import lombok.Data
import java.util.Date

@Data
class GoalData {
    // 目標ID
    var goalId: String? = null

    // ユーザーID
    var userId: String? = null

    // 目標内容
    var goalContent: String? = null

    // 目標日付
    var goalDate: Date? = null

    // 画面表示用：残り日数
    var remainingDays: Long = 0
}