package jp.sabakan.mirai.controller

import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping

@Controller
class InterviewController {

    @GetMapping("/interview-main")
    fun getInterview(): String {
        return "/interview/interview-main"
    }

    @GetMapping("/interview-do")
    fun getInterview_do(): String {
        return "/interview/interview-do"
    }

    @GetMapping("/interview-log")
    fun getInterview_log(): String {
        return "/interview/interview-log"
    }

    @GetMapping("/interview-result")
    fun getInterview_result(): String {
        return "/interview/interview-result"
    }

    @PostMapping("/interview-main")
    fun postInterview(): String {
        return "/interview/interview-main"
    }

    @PostMapping("/interview-do")
    fun postInterview_do(): String {
        return "/interview/interview-do"
    }

    @PostMapping("/interview-log")
    fun postInterview_log(): String {
        return "/interview/interview-log"
    }

    @PostMapping("/interview-result")
    fun postInterview_result(): String {
        return "/interview/interview-result"
    }
}