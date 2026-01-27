package jp.sabakan.mirai.security

import jp.sabakan.mirai.service.UserService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.stereotype.Service

@Service
class LoginUserDetailsService : UserDetailsService {

    @Autowired
    private lateinit var service: UserService

    override fun loadUserByUsername(username: String): UserDetails {
        if (username.isNullOrBlank()) {
            throw UsernameNotFoundException("ユーザー名が入力されていません")
        }

        // ユーザ情報の取得 (UserServiceに追加したメソッドを使用)
        val userEntity = service.getUserByEmail(username)
            ?: throw UsernameNotFoundException("ユーザが見つかりません: $username")

        // UserDetailsの実装クラスを返す
        return LoginUserDetails(userEntity)
    }
}