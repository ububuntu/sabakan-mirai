package jp.sabakan.mirai.controller

import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping

@Controller
    class QuestionsController {

        @GetMapping("/questions-main")
        fun getIndex(): String{
            return "questions/questions-main"
        }

        @PostMapping("/questions-main")
        fun postIndex(): String{
            return "questions/questions-main"
        }
    }