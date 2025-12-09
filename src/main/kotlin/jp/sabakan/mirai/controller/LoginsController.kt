package jp.sabakan.mirai.controller

import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping

@Controller
class LoginsController {

    @GetMapping("/sign-in")
    fun signIn(model: Model): String {
        return "logins/sign-in"
    }

    @GetMapping("/sign-up")
    fun signUp(model: Model): String {
        return "logins/sign-up"
    }

}