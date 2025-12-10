package jp.sabakan.mirai.response

import jp.sabakan.mirai.entity.UserEntity
import lombok.Data

@Data
class UserResponse {
    // ユーザ一覧
    var users: List<UserEntity>? = null

    // 返却メッセージ
    var message: String? = null

    // ユーザ権限
    var userRole: String? = null
}