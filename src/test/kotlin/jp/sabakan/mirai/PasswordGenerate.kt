package jp.sabakan.mirai

import org.springframework.boot.test.context.SpringBootTest
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import kotlin.test.Test
import kotlin.test.fail


@SpringBootTest
internal class PasswordGenerate {
    @Test
    fun test() {
        val plain_password = "password123"
        val hash_password_ = BCryptPasswordEncoder().encode(plain_password)
        println("ハッシュ化されたパスワード:" + hash_password_)
    }
}