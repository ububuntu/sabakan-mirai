
package jp.sabakan.mirai.controller

import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping

@Controller
class ErrorController {

    // 共通のエラー画面
    @GetMapping("/errors")
    fun getError(): String {
        return "error/error"
    }

    // 404エラー画面
    @GetMapping("/404error")
    fun get404Error(): String {
        return "error/404-error"
    }

    // 500エラー画面
    @GetMapping("/500error")
    fun get500Error(): String {
        return "error/500-error"
    }

    // 共通のエラー画面
    @PostMapping("/errors")
    fun postError(): String {
        return "error/error"
    }

    // 404エラー画面
    @PostMapping("/404error")
    fun post404Error(): String {
        return "error/404-error"
    }

    // 500エラー画面
    @PostMapping("/500error")
    fun post500Error(): String {
        return "error/500-error"
    }
}