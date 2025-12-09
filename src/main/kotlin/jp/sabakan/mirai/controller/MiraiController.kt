package jp.sabakan.mirai.controller

import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping

@Controller
class MiraiController {

    @GetMapping("/index")
    fun getIndex(): String {
        return "/index"
    }

    @PostMapping("/index")
    fun postIndex(): String {
        return "/index"
    }
}