package jp.sabakan.mirai.security

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.web.SecurityFilterChain

@Configuration
@EnableWebSecurity
class SecurityConfig {

    @Autowired
    private lateinit var userDetailsService: LoginUserDetailsService

    @Value("\${spring.profiles.active:default}")
    private val activeProfile: String = "default"

    @Bean
    @Throws(Exception::class)
    fun filterChain(http: HttpSecurity): SecurityFilterChain {
        http
            // 1. 認可（アクセス制御）設定
            .authorizeHttpRequests { auth ->
                auth.requestMatchers("/login", "/signup/**", "/css/**", "/js/**", "/img/**").permitAll()
                auth.requestMatchers("/interview/api/**").authenticated()
                // 管理者専用ページ (ADMIN)
                auth.requestMatchers("/manage/**").hasRole("ADMIN")
                // hasAnyRoleは可変長引数を受け取るためカンマ区切りでOK
                auth.requestMatchers("/notice").hasAnyRole("USER", "ADMIN")
                auth.requestMatchers("/user/**").authenticated()

                if ("dev" == activeProfile) {
                    auth.requestMatchers("/h2-console/**").permitAll()
                }

                auth.anyRequest().authenticated()
            }
            // 2. フォームログイン設定
            .formLogin { form ->
                form
                    .loginPage("/login")             // ログイン画面のURL
                    .loginProcessingUrl("/login")    // フォームのPOST先
                    .usernameParameter("userAddress") // HTMLフォームのname属性
                    .passwordParameter("password")    // HTMLフォームのname属性
                    .defaultSuccessUrl("/index", true) // 成功時の遷移先
                    .failureUrl("/login?error=true")
                    .permitAll()
            }
            // 3. ログアウト設定
            .logout { logout ->
                logout
                    .logoutUrl("/logout")
                    .logoutSuccessUrl("/login?logout=true")
                    .invalidateHttpSession(true)
                    .deleteCookies("JSESSIONID")
            }
            // 4. 例外ハンドリング
            .exceptionHandling { exceptions ->
                exceptions.accessDeniedHandler(CustomAccessDeniedHandler())
            }
            // 5. セッション管理
            .sessionManagement { session ->
                session
                    .maximumSessions(1)
                    .maxSessionsPreventsLogin(false)
            }
            // 6. セキュリティヘッダー設定
            .headers { headers ->
                headers
                    .httpStrictTransportSecurity { hsts ->
                        hsts.includeSubDomains(true).maxAgeInSeconds(2592000)
                    }
                    .contentSecurityPolicy { csp->
                        csp.policyDirectives("script-src 'self'; object-src 'none';")
                    }
            }

        // 開発環境用のH2コンソール対応
        if ("dev" == activeProfile) {
            http.csrf { it.disable() }
            http.headers { it.frameOptions { frame -> frame.disable() } }
        }

        return http.build()
    }

    /**
     * 認証マネージャの設定
     */
    @Bean
    @Throws(Exception::class)
    fun authenticationManager(http: HttpSecurity): AuthenticationManager {
        val authenticationManagerBuilder = http.getSharedObject(AuthenticationManagerBuilder::class.java)
        authenticationManagerBuilder
            .userDetailsService(userDetailsService)
            .passwordEncoder(PasswordEncoderConfig().passwordEncoder())
        return authenticationManagerBuilder.build()
    }
}