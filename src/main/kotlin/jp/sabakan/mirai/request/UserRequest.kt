package jp.sabakan.mirai.request

import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Pattern
import jakarta.validation.constraints.Size
import jp.sabakan.mirai.MessageConfig
import lombok.Data
import java.util.Date

@Data
class UserRequest {
    // ユーザID
    var userId: String? = null

    // ユーザネーム
    @field:NotBlank(message = MessageConfig.USERNAME_NOT_BLANK_ERROR)
    @field:Size(max = 50, message = MessageConfig.PASSWORD_LENGTH_ERROR)
    @field:Pattern(regexp = "^[^<>{}\\[\\]]*$", message = MessageConfig.INVALID_CHARACTER_ERROR)
    var userName: String? = null

    // ユーザアドレス
    @field:NotBlank(message = MessageConfig.EMAIL_NOT_BLANK_ERROR)
    @field:Email(message = MessageConfig.EMAIL_INVALID_FORMAT_ERROR)
    @field:Size(max = 100, message = MessageConfig.PASSWORD_LENGTH_ERROR)
    var userAddress: String? = null

    // パスワード
    @field:Size(min = 8, max = 100, message = MessageConfig.PASSWORD_LENGTH_ERROR)
    @field:Pattern(regexp = "^(?=.*[0-9])(?=.*[a-z]).*$", message = MessageConfig.PASSWORD_INPUT_ERROR)
    var password: String? = null

    // 旧パスワード
    @field:Size(max = 100, message = MessageConfig.PASSWORD_LENGTH_ERROR)
    var oldPassword: String? = null

    // パスワード確認用
    @field:Size(max = 100, message = MessageConfig.PASSWORD_LENGTH_ERROR)
    var passwordConfirm: String? = null

    // 権限
    var userRole: String? = null

    // 有効性
    var isValid: Boolean? = null

    // 登録日時
    var createdAt: Date? = null

    // 最終ログイン日時
    var lastedAt: Date? = null

    // 検索用キーワード
    var keyword: String? = null
}