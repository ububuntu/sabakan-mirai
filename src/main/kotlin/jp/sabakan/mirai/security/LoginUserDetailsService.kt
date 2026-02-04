package jp.sabakan.mirai.security

import jp.sabakan.mirai.MessageConfig
import jp.sabakan.mirai.service.UserService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.stereotype.Service

/**
 * Spring SecurityのUserDetailsServiceインターフェースを実装したクラス。
 * ユーザ名（メールアドレス）からユーザ情報を取得し、UserDetailsを返す。
 */
@Service
class LoginUserDetailsService : UserDetailsService {

    @Autowired
    private lateinit var service: UserService

    override fun loadUserByUsername(username: String): UserDetails {
        if (username.isNullOrBlank()) {
            throw UsernameNotFoundException(MessageConfig.USERNAME_NOT_BLANK_ERROR)
        }

        // ユーザ情報の取得 (UserServiceに追加したメソッドを使用)
        val userEntity = service.getUserByEmail(username)
            ?: throw UsernameNotFoundException(MessageConfig.USER_NOT_FOUND)

        // UserDetailsの実装クラスを返す
        return LoginUserDetails(userEntity)
    }
}