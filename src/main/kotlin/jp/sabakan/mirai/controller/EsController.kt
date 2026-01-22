package jp.sabakan.mirai.controller

import jp.sabakan.mirai.request.EsRequest
import jp.sabakan.mirai.service.EsService
import jp.sabakan.mirai.component.EcComponent
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestParam
import java.security.Principal

@Controller
class EsController {

    // Serviceを利用可能にする
    @Autowired
    lateinit var esService: EsService

    @Autowired
    lateinit var ecComponent: EcComponent

    // ESメイン画面
    @GetMapping("/es")
    fun getEs(): String {
        return "entrysheet/es-main"
    }

    // ES一覧画面 (GET)
    @GetMapping("/es/list")
    fun getEsList(principal: Principal?, model: Model): String { // principalはnullableにしておくのが安全
        // ログインユーザーIDの取得 (認証がない場合は仮のIDを設定)
        val userId = principal?.name ?: "test-user-id"

        // 検索用リクエストオブジェクトの作成
        val request = EsRequest().apply {
            this.userId = userId
        }

        // サービスから一覧を取得
        val response = esService.getEsList(request)

        // 画面(Model)にリストを渡す
        model.addAttribute("esList", response.esList)

        return "/entrysheet/es-list"
    }

    // ES作成画面 (GET)
    @GetMapping("/es/creation")
    fun getEsCreation(
        @RequestParam(required = false) esId: String?,
        principal: Principal?,
        model: Model
    ): String {

        val userId = principal?.name ?: "test-user-id"
        val esRequest = if (esId != null) {
            // IDがあるならDBから取得
            esService.getEsDetail(esId, userId)
        } else {
            // 新規なら空のオブジェクト
            EsRequest()
        }

        // 画面に渡す
        model.addAttribute("esRequest", esRequest)
        return "entrysheet/es-creation"
    }

    @PostMapping("/es/creation")
    fun postEsCreation(
        esRequest: EsRequest,
        @RequestParam(value = "action", required = false) action: String?,
        // HTMLのhiddenから「前回の結果」を受け取る（最初はnullなのでOptionalにする）
        @RequestParam(required = false) reasonResult: String?,
        @RequestParam(required = false) selfprResult: String?,
        @RequestParam(required = false) activitiesResult: String?,
        @RequestParam(required = false) stweResult: String?,
        principal: Principal?,
        model: Model
    ): String {
        val userId = principal?.name ?: "test-user-id"
        esRequest.userId = userId

        // まず、受け取った「前回の結果」をすべて一旦モデルに戻す
        model.addAttribute("reasonResult", reasonResult)
        model.addAttribute("selfprResult", selfprResult)
        model.addAttribute("activitiesResult", activitiesResult)
        model.addAttribute("stweResult", stweResult)

        // その後、今回押されたボタンの処理で、新しい結果を上書きする
        when (action) {
            "checkReason" -> {
                val result = ecComponent.analyzeMessage(esRequest.esContentReason ?: "")
                model.addAttribute("reasonResult", result) // 新しい結果に更新
            }
            "checkSelfpr" -> {
                val result = ecComponent.analyzeMessage(esRequest.esContentSelfpr ?: "")
                model.addAttribute("selfprResult", result) // 新しい結果に更新
            }
            "checkActivities" -> {
                val result = ecComponent.analyzeMessage(esRequest.esContentActivities ?: "")
                model.addAttribute("activitiesResult", result)
            }
            "checkStwe" -> {
                val result = ecComponent.analyzeMessage(esRequest.esContentStwe ?: "")
                model.addAttribute("stweResult", result)
            }
            "save" -> {
                esService.saveEs(esRequest)
                return "redirect:/es/list"
            }
        }

        model.addAttribute("esRequest", esRequest)
        return "entrysheet/es-creation"
    }
}