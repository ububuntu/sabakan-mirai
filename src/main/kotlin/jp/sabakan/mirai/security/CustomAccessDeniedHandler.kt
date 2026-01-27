package jp.sabakan.mirai.security

import jakarta.servlet.ServletException
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.security.access.AccessDeniedException
import org.springframework.security.web.access.AccessDeniedHandler
import java.io.IOException

class CustomAccessDeniedHandler : AccessDeniedHandler {
    @Throws(IOException::class, ServletException::class)
    override fun handle(
        request: HttpServletRequest,
        response: HttpServletResponse,
        accessDeniedException: AccessDeniedException
    ) {
        val referer = request.getHeader("Referer")

        // ログアウトページ以外から来た場合、元のページに戻すなどの処理
        if (!referer.isNullOrEmpty() && !referer.endsWith("/logout")) {
            response.sendRedirect(referer)
            return
        }

        // それ以外はトップページまたは専用エラーページへ
        response.sendRedirect("/index")
    }
}