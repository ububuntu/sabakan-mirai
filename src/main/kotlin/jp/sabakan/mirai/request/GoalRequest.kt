package jp.sabakan.mirai.request

import jakarta.validation.constraints.FutureOrPresent
import jakarta.validation.constraints.NotBlank
import jp.sabakan.mirai.MessageConfig
import lombok.Data
import org.springframework.format.annotation.DateTimeFormat
import java.util.Date

@Data
class GoalRequest {
    // 目標ID
    var goalId: String? = null

    // ユーザーID
    var userId: String? = null

    // 目標内容
    @field:NotBlank(message = MessageConfig.NOT_BLANK_ERROR)
    var goalContent: String? = null

    // 目標日付
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    @field:FutureOrPresent(message = MessageConfig.NOT_BLANK_ERROR)
    var goalDate: Date? = null

    // 画面表示用：残り日数
    var remainingDays: Long = 0
}