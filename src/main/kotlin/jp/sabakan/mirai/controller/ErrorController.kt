
package jp.sabakan.mirai.controller

import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping


@Controller
class ErrorController {

    // 共通のエラー画面
    @GetMapping("/errors")
    fun getError(): String {
        return "error/error"
    }

    // 400エラー画面
    @GetMapping("/error/400")
    fun get404Error(): String {
        return "error/400-error"
    }

    // 500エラー画面
    @GetMapping("/error/500")
    fun get500Error(): String {
        return "error/500-error"
    }
}