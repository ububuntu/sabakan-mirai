
package jp.sabakan.mirai.controller

import jakarta.servlet.http.HttpServletRequest
import org.springframework.boot.webmvc.error.ErrorController
import org.springframework.http.HttpHeaders
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping

@Controller
@RequestMapping("/error")
class CustomErrorController: ErrorController {

    private val STATUS_CODE = "jakarta.servlet.error.status_code"

    /**
     * エラーページの表示
     */
    @RequestMapping()
    fun error(request: HttpServletRequest): String{
        // HTTPステータスコードを取得
        val status = request.getAttribute(STATUS_CODE)?.toString()?.toIntOrNull()

        // 各種エラーステータスコードに応じたビューを返す
        if (status != null) {
            when{
                // 400番台のエラー
                status in 400..499 -> {
                    return "/error/400-error"
            }
                // 500番台のエラー
                status in 500..599 -> {
                    return "/error/500-error"
                }
            }
        }
        // その他のエラー
        return "/error"
    }

    /**
     * 前のページに戻る
     */
    @GetMapping("/back")
    fun goBackToPreviousPage(request: HttpServletRequest): String {
        // Refererヘッダーから前のページのURLを取得
        val referer = request.getHeader(HttpHeaders.REFERER)

        // 前のページが存在する場合はそのURLにリダイレクト、存在しない場合はホームページにリダイレクト
        return if (referer != null) {
            // 参照元がある場合はそのページにリダイレクト
            "redirect:$referer"
        } else {
            // 参照元がない場合はホームにリダイレクト
            "redirect:/index"
        }
    }
}