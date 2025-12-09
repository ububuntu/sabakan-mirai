package jp.sabakan.mirai.controller

import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping

@Controller
class UserController {

    @GetMapping("/user/profile")
    fun userProfile(model: Model): String {
        //データ取得
        var hoge: String = "moge"

        // データ受け渡し
        model.addAttribute("null", null)

        // 画面表示
        return "users/user-main"
    }
}