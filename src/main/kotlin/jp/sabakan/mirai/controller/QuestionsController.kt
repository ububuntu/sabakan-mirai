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

        @GetMapping("/spi-main")
        fun getQuestion_spi_main(): String{
            return "questions/spi-main"
        }

        @GetMapping("/spi-do")
        fun getQuestion_spi_do(): String{
            return "questions/spi-do"
        }

        @GetMapping("/cabgab-main")
        fun getQuestion_cubgab_main(): String{
            return "questions/cabgab-main"
        }

        @PostMapping("/questions-main")
        fun postIndex(): String{
            return "questions/questions-main"
        }

        @PostMapping("/spi-main")
        fun postQuestion_spi_main(): String{
            return "questions/spi-main"
        }

        @PostMapping("/spi-do")
        fun postQuestion_spi_do(): String{
            return "questions/spi-do"
        }

        @PostMapping("/cabgab-main")
        fun postQuestion_cabgab_main(): String{
            return "questions/cabgab-main"
        }
    }