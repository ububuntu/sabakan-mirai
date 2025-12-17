package jp.sabakan.mirai.controller

import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping

// TODO: 後で消すこと
@Profile("dev")
@Controller()
class DevTestErrorController {

    @GetMapping("/dev/test/error")
    fun getTestError(): String {
        return "/error"
    }

    @GetMapping("/dev/test/400")
    fun getTest400(): String {
        return "/error/400-error"
    }

    @GetMapping("/dev/test/500")
    fun getTest500(): String {
        return "/error/500-error"
    }
}