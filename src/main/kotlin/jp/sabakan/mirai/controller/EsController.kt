package jp.sabakan.mirai.controller

import jp.sabakan.mirai.request.EsRequest
import jp.sabakan.mirai.service.EsService
import jp.sabakan.mirai.component.EcComponent
import jp.sabakan.mirai.security.LoginUserDetails
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestParam

@Controller
class EsController {

    // Serviceを利用可能にする
    @Autowired
    lateinit var esService: EsService

    @Autowired
    lateinit var ecComponent: EcComponent

    // ESメイン画面
    @GetMapping("/es")
    fun getEs(@AuthenticationPrincipal userDetails: LoginUserDetails, model: Model): String {
        // ユーザーIDの取得
        val userId = userDetails.getUserEntity().userId

        // 検索リクエストの作成
        val request = EsRequest().apply {
            this.userId = userId
        }

        // サービスから一覧を取得
        val response = esService.getEsList(request)

        // 画面にリストを渡す
        model.addAttribute("esList", response.esList)

        return "entrysheet/es-main"
    }

    // ES一覧画面 (GET)
    @GetMapping("/es/list")
    fun getEsList(@AuthenticationPrincipal userDetails: LoginUserDetails, model: Model): String { // principalはnullableにしておくのが安全
        // ユーザーIDの取得
        val userId = userDetails.getUserEntity().userId

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

    // ES新規作成画面 (POST)
    @GetMapping("/es/creation")
    fun getEsCreation(model: Model): String {
        // IDを指定せず、常に空のEsRequestを渡す
        model.addAttribute("esRequest", EsRequest())
        return "entrysheet/es-creation"
    }

    // ES新規作成画面 (GET)
    @PostMapping("/es/creation")
    fun postEsCreation(
        esRequest: EsRequest,
        @RequestParam(value = "action", required = false) action: String?,
        @RequestParam(required = false) reasonResult: String?,
        @RequestParam(required = false) selfprResult: String?,
        @RequestParam(required = false) activitiesResult: String?,
        @RequestParam(required = false) stweResult: String?,
        @AuthenticationPrincipal userDetails: LoginUserDetails,
        model: Model
    ): String {
        val userId = userDetails.getUserEntity().userId
        esRequest.userId = userId

        // 前回の結果を保持（添削を繰り返すため）
        model.addAttribute("reasonResult", reasonResult)
        model.addAttribute("selfprResult", selfprResult)
        model.addAttribute("activitiesResult", activitiesResult)
        model.addAttribute("stweResult", stweResult)

        when (action) {
            "checkReason" -> model.addAttribute("reasonResult", ecComponent.analyzeMessage(esRequest.esContentReason ?: ""))
            "checkSelfpr" -> model.addAttribute("selfprResult", ecComponent.analyzeMessage(esRequest.esContentSelfpr ?: ""))
            "checkActivities" -> model.addAttribute("activitiesResult", ecComponent.analyzeMessage(esRequest.esContentActivities ?: ""))
            "checkStwe" -> model.addAttribute("stweResult", ecComponent.analyzeMessage(esRequest.esContentStwe ?: ""))
            "save" -> {
                esService.saveEs(esRequest)
                return "redirect:/es/list"
            }
        }

        model.addAttribute("esRequest", esRequest)
        return "entrysheet/es-creation"
    }

    // ES編集画面 (GET)

    @GetMapping("/es/edit")
    fun getEsEdit(
        @RequestParam esId: String,
        @AuthenticationPrincipal userDetails: LoginUserDetails,
        model: Model
    ): String {
        val userId = userDetails.getUserEntity().userId

        val request = EsRequest().apply {
            this.esId = esId
            this.userId = userId
        }

        // DBから取得（結果がListで返ってくる場合）
        val esDetailList = esService.getEsDetail(request)

        // リストが空でないか確認し、最初の1件をモデルに渡す
        if (esDetailList is List<*> && esDetailList.isNotEmpty()) {
            model.addAttribute("esRequest", esDetailList[0])
        } else if (esDetailList is EsRequest) {
            // もし既に単一オブジェクトで返ってきているならそのまま渡す
            model.addAttribute("esRequest", esDetailList)
        } else {
            // データが見つからない場合のハンドリング
            return "redirect:/es/list"
        }

        return "entrysheet/es-edit"
    }

    // ES編集画面(POST)
    @PostMapping("/es/edit")
    fun postEsEdit(
        esRequest: EsRequest,
        @RequestParam(value = "action", required = false) action: String?,
        @RequestParam(required = false) reasonResult: String?,
        @RequestParam(required = false) selfprResult: String?,
        @RequestParam(required = false) activitiesResult: String?,
        @RequestParam(required = false) stweResult: String?,
        @AuthenticationPrincipal userDetails: LoginUserDetails,
        model: Model
    ): String {
        val userId = userDetails.getUserEntity().userId
        esRequest.userId = userId

        // 結果の保持
        model.addAttribute("reasonResult", reasonResult)
        model.addAttribute("selfprResult", selfprResult)
        model.addAttribute("activitiesResult", activitiesResult)
        model.addAttribute("stweResult", stweResult)

        when (action) {
            "checkReason" -> model.addAttribute("reasonResult", ecComponent.analyzeMessage(esRequest.esContentReason ?: ""))
            "checkSelfpr" -> model.addAttribute("selfprResult", ecComponent.analyzeMessage(esRequest.esContentSelfpr ?: ""))
            "checkActivities" -> model.addAttribute("activitiesResult", ecComponent.analyzeMessage(esRequest.esContentActivities ?: ""))
            "checkStwe" -> model.addAttribute("stweResult", ecComponent.analyzeMessage(esRequest.esContentStwe ?: ""))
            "save" -> {
                esService.saveEs(esRequest)
                return "redirect:/es/list"
            }
            "delete" -> {
                esService.deleteEs(esRequest)
                return "redirect:/es/list"
            }
        }

        model.addAttribute("esRequest", esRequest)
        return "entrysheet/es-edit" // edit用のHTMLを返す
    }

}