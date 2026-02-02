package jp.sabakan.mirai.controller

import jp.sabakan.mirai.MessageConfig
import jp.sabakan.mirai.request.UserRequest
import jp.sabakan.mirai.service.UserService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.validation.BindingResult
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.ModelAttribute
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.servlet.mvc.support.RedirectAttributes

@Controller
class LoginsController {

    @Autowired
    private lateinit var userService: UserService

    @Autowired
    private lateinit var passwordEncoder: PasswordEncoder

    /**
     * ログイン画面表示
     * SecurityConfigで .loginPage("/login") としているため、ここも /login にします。
     */
    @GetMapping("/login")
    fun login(
        @RequestParam(value = "error", required = false) error: String?,
        @RequestParam(value = "logout", required = false) logout: String?,
        @RequestParam(value = "passwordChanged", required = false) passwordChanged: String?,
        model: Model
    ): String {
        // ログイン失敗時 (SecurityConfigの .failureUrl("/login?error=true") と連動)
        if (error != null) {
            model.addAttribute("message", MessageConfig.LOGIN_FAILED)
        }

        // ログアウト成功時 (SecurityConfigの .logoutSuccessUrl("/login?logout=true") と連動)
        if (logout != null) {
            model.addAttribute("message", MessageConfig.LOGOUT_SUCCESS)
        }

        // パスワード変更成功時 (UserControllerのリダイレクトと連動)
        if (passwordChanged != null) {
            model.addAttribute("message", MessageConfig.PASSWORD_CHANGE_SUCCESS)
        }
        return "logins/sign-in"
    }

    /**
     * 新規登録画面表示
     */
    @GetMapping("/signup")
    fun signUp(model: Model): String {
        // フォームバインディング用の空オブジェクトを渡す
        model.addAttribute("userRequest", UserRequest())
        model.addAttribute("message", MessageConfig.USER_REGISTERED)
        return "logins/sign-up"
    }

    /**
     * 新規登録処理 (POST)
     */
    @PostMapping("/signup")
    fun signUpProcess(
        @Validated @ModelAttribute userRequest: UserRequest,
        bindingResult: BindingResult,
        model: Model,
        redirectAttributes: RedirectAttributes
    ): String {
        // 1. バリデーションエラーチェック
        if (bindingResult.hasErrors()) {
            return "logins/sign-up"
        }

        // 2. 既存ユーザチェック (同じメールアドレスが既にないか確認)
        // UserServiceに findUserByEmail がある前提
        val existingUser = userService.getUserByEmail(userRequest.userAddress ?: "")
        if (existingUser != null) {
            model.addAttribute("errorMessage", MessageConfig.USER_REGISTER_FAILED)
            return "logins/sign-up"
        }

        try {
            // 3. パスワードのハッシュ化 (必須)
            // 入力された生パスワードをBCrypt等で暗号化してセットし直す
            val rawPassword = userRequest.password
            userRequest.password = passwordEncoder.encode(rawPassword)

            // 権限などの初期値をセット（必要であれば）
            userRequest.userRole = "STUDENT"
            userRequest.isValid = true

            // 4. DB登録実行
            userService.insertUser(userRequest)

            // 5. 成功時のリダイレクト
            redirectAttributes.addFlashAttribute("message", MessageConfig.USER_REGISTERED)
            return "redirect:/login"

        } catch (e: Exception) {
            // エラーハンドリング
            e.printStackTrace()
            model.addAttribute("errorMessage", MessageConfig.USER_REGISTER_FAILED)
            return "logins/sign-up"
        }
    }
}