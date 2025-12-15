package jp.sabakan.mirai.controller

import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping

@Controller
    class EsController {

        @GetMapping("/es-main")
        fun getIndex(): String{
            return "entrysheet/es-main"
        }

        @GetMapping("/es-creation")
        fun getEsCreation(): String{
            return "entrysheet/es-creation"
        }

        @PostMapping("/es-main")
        fun postIndex(): String{
            return "entrysheet/es-main"
        }

        @PostMapping("/es-creation")
        fun postEsCreation(): String{
            return "entrysheet/es-creation"
        }
    }
