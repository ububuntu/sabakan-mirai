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
import org.springframework.validation.BindingResult
import jakarta.validation.Valid
import jp.sabakan.mirai.entity.EsEntity

@Controller
class EsController {

    @Autowired
    lateinit var esService: EsService

    @Autowired
    lateinit var ecComponent: EcComponent

    // ESメイン画面 (TOP)
    @GetMapping("/es")
    fun getEs(@AuthenticationPrincipal userDetails: LoginUserDetails, model: Model): String {
        val userId = userDetails.getUserEntity().userId
        val request = EsRequest().apply { this.userId = userId }

        // サービスから取得（response.esList は List<EsEntity>? 型）
        val response = esService.getEsList(request)

        // 【修正ポイント】
        // ?. を使い、型を明確にしたまま take(3) を実行する
        val top3List: List<EsEntity> = response.esList?.take(3) ?: emptyList()

        // 画面にリストを渡す
        model.addAttribute("esList", top3List)

        return "entrysheet/es-main"
    }

    // ES一覧画面
    @GetMapping("/es/list")
    fun getEsList(@AuthenticationPrincipal userDetails: LoginUserDetails, model: Model): String {
        val userId = userDetails.getUserEntity().userId
        val request = EsRequest().apply { this.userId = userId }
        val response = esService.getEsList(request)

        // emptyListに <EsEntity> を指定して型を確定させる
        model.addAttribute("esList", response.esList ?: emptyList<EsEntity>())

        return "entrysheet/es-list"
    }

    // ES新規作成画面 (GET)
    @GetMapping("/es/creation")
    fun getEsCreation(model: Model): String {
        model.addAttribute("esRequest", EsRequest())
        return "entrysheet/es-creation"
    }

    // ES新規作成 (POST)
    @PostMapping("/es/creation")
    fun postEsCreation(
        @Valid esRequest: EsRequest,
        bindingResult: BindingResult,
        @RequestParam(value = "action", required = false) action: String?,
        @RequestParam(required = false) reasonResult: String?,
        @RequestParam(required = false) selfprResult: String?,
        @RequestParam(required = false) activitiesResult: String?,
        @RequestParam(required = false) stweResult: String?,
        @AuthenticationPrincipal userDetails: LoginUserDetails,
        model: Model
    ): String {
        esRequest.userId = userDetails.getUserEntity().userId

        // 保存時のみバリデーションチェックを適用
        if (action == "save" && bindingResult.hasErrors()) {
            return "entrysheet/es-creation"
        }

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

        // 1. Serviceから取得
        val result = esService.getEsDetail(request)

        // 2. 【修正ポイント】型を具体的に指定してチェックする
        if (result is List<*> && result.isNotEmpty()) {
            // result[0] を EsRequest 型として明示的に扱う
            val esDetail = result[0] as? EsRequest
            if (esDetail != null) {
                model.addAttribute("esRequest", esDetail)
            } else {
                return "redirect:/es/list"
            }
        } else {
            return "redirect:/es/list"
        }

        return "entrysheet/es-edit"
    }

    // ES編集画面 (POST)
    @PostMapping("/es/edit")
    fun postEsEdit(
        @Valid esRequest: EsRequest, // @Validを追加
        bindingResult: BindingResult, // BindingResultを追加
        @RequestParam(value = "action", required = false) action: String?,
        @RequestParam(required = false) reasonResult: String?,
        @RequestParam(required = false) selfprResult: String?,
        @RequestParam(required = false) activitiesResult: String?,
        @RequestParam(required = false) stweResult: String?,
        @AuthenticationPrincipal userDetails: LoginUserDetails,
        model: Model
    ): String {
        esRequest.userId = userDetails.getUserEntity().userId

        // 保存時のみバリデーションチェックを適用
        if (action == "save" && bindingResult.hasErrors()) {
            return "entrysheet/es-edit"
        }

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
        return "entrysheet/es-edit"
    }
}