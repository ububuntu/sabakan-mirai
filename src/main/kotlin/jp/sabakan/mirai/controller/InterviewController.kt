package jp.sabakan.mirai.controller

import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping

@Controller
class InterviewController {

    @GetMapping("/interview-main")
    fun getIndex(): String {
        return "/interview/interview-main"
    }

    @PostMapping("/interview-main")
    fun postIndex(): String {
        return "/interview/interview-main"
    }
}