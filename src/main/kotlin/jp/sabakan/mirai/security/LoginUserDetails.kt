package jp.sabakan.mirai.security

import jp.sabakan.mirai.entity.UserEntity
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.userdetails.UserDetails

class LoginUserDetails(private val userEntity: UserEntity) : UserDetails {

    // 権限の取得
    override fun getAuthorities(): MutableCollection<out GrantedAuthority> {
        // userRoleがnullの場合はデフォルトで"USER"などを設定するか、空リストにする
        val role = userEntity.userRole ?: "USER"
        // Spring Securityは通常 "ROLE_" プレフィックスを期待します
        return mutableListOf(SimpleGrantedAuthority("ROLE_${role.uppercase()}"))
    }

    // パスワードの取得
    override fun getPassword(): String {
        return userEntity.password ?: ""
    }

    // ユーザ名（認証の識別子）の取得。ここではメールアドレス(userAddress)を使用
    override fun getUsername(): String {
        return userEntity.userAddress ?: ""
    }

    // アカウントの有効期限（今回は無期限）
    override fun isAccountNonExpired(): Boolean = true

    // アカウントのロック状態（今回はロックしない）
    override fun isAccountNonLocked(): Boolean = true

    // パスワードの有効期限（今回は無期限）
    override fun isCredentialsNonExpired(): Boolean = true

    // アカウントの有効状態 (isValidフラグを見る)
    override fun isEnabled(): Boolean {
        return userEntity.isValid == true
    }

    // アプリケーション独自でUserEntityを使いたい場合のためのゲッター
    fun getUserEntity(): UserEntity = userEntity
}