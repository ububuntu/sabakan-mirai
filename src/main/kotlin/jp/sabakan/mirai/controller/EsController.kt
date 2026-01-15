package jp.sabakan.mirai.controller

import jp.sabakan.mirai.request.EsRequest
import jp.sabakan.mirai.service.EsService // Serviceをインポート
import org.springframework.beans.factory.annotation.Autowired // Autowiredをインポート
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.ModelAttribute // ModelAttributeをインポート
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestParam
import java.security.Principal

@Controller
class EsController {

    // Serviceを利用可能にする
    @Autowired
    lateinit var esService: EsService

    // ESメイン画面
    @GetMapping("/es")
    fun getEs(): String{
        return "entrysheet/es-main"
    }

    // ES一覧画面 (GET)
    @GetMapping("/es/list")
    fun getEsList(principal: Principal?, model: Model): String{ // principalはnullableにしておくのが安全
        // ログインユーザーIDの取得 (認証がない場合は仮のIDを設定)
        val userId = principal?.name ?: "test-user"

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
    fun getEsCreation(@RequestParam(required = false) esId: String?,
                      principal: Principal?,
                      model: Model): String {

        val userId = principal?.name ?: "test-user"
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

    // ES作成処理 (POST)
    @PostMapping("/es/creation")
    fun postEsCreation(esRequest: EsRequest, principal: Principal?): String {
        esRequest.userId = principal?.name ?: "test-user"
        esService.saveEs(esRequest)
        return "redirect:/es/list"
    }
}