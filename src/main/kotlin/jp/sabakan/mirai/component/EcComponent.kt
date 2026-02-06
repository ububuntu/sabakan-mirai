package jp.sabakan.mirai.component

import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import org.springframework.web.client.RestTemplate

// 1. JSONを受け取るための入れ物（データクラス）を定義
data class EcApiResponse(
    val result: String = ""
)

@Component
class EcComponent(
    private val restTemplate: RestTemplate,
    @Value("\${ec.api.url:http://ai-2:8000}")
    private val baseUrl: String
) {
    fun analyzeMessage(message: String): String? {
        val url = "$baseUrl/check"
        val requestBody = mapOf("text_to_check" to message)

        return try {
            // 受け取る型を指定
            val response = restTemplate.postForObject(url, requestBody, EcApiResponse::class.java)
            // 文章のみの出力になるようにする
            response?.result?.trim()
        } catch (e: Exception) {
            "通信エラー: ${e.message}"
        }
    }
}