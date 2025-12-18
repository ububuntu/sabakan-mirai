package jp.sabakan.mirai.component

import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.web.client.RestTemplate
import org.springframework.web.client.getForEntity
import java.lang.Exception
import java.util.concurrent.CompletableFuture

@Component
class InterviewComponent(
    private val restTemplate: RestTemplate,
    private val baseUrl: String = "http://192.168.1.100:5000"
) {

    /**
     * 非同期呼び出しを行うユーティリティメソッド
     *
     * @param default 例外発生時に返すデフォルト値
     * @param block 実行する処理
     * @return CompletableFuture<T>
     */
    private inline fun <T> asyncCall(default: T, crossinline block: () -> T): CompletableFuture<T> {
        return CompletableFuture.supplyAsync {
            try {
                block()
            } catch (e: Exception) {
                default
            }
        }
    }

    /**
     * 面接システムへの接続確認を行う
     *
     * @return CompletableFuture<Boolean>
     */
    fun testConnection(): CompletableFuture<Boolean> = asyncCall(false) {
        restTemplate.getForEntity<String>(baseUrl)
        true
    }

    /**
     * 面接分析を開始する
     *
     * @return CompletableFuture<Boolean>
     */
    fun startAnalysis(): CompletableFuture<Boolean> = post("/interview/start")

    /**
     * フレーム画像を分析する
     *
     * @param base64 画像のBase64エンコード文字列
     * @return CompletableFuture<Boolean>
     */
    fun analyzeFrame(base64: String): CompletableFuture<Boolean> {
        val body = mapOf("image" to base64)
        return post("/interview/analyze", body)
    }

    /**
     * 音声データを分析する
     *
     * @param base64Audio 音声データのBase64エンコード文字列
     * @return CompletableFuture<Boolean>
     */
    fun analyzeAudio(base64Audio: String): CompletableFuture<Boolean> {
        val body = mapOf("audio" to base64Audio)
        return post("/interview/analyze-audio", body)
    }

    /**
     * 面接分析の音声結果を取得する
     *
     * @return CompletableFuture<ByteArray?>
     */
    fun getAudioResult(): CompletableFuture<ByteArray?> = asyncCall(null) {
        restTemplate.getForEntity("$baseUrl/interview/audio", ByteArray::class.java).body
    }

    /**
     * 面接分析を停止する
     *
     * @return CompletableFuture<String?>
     */
    fun stopAnalysis(): CompletableFuture<String?> = asyncCall(null) {
        val headers = HttpHeaders().apply {
            contentType = MediaType.APPLICATION_JSON
        }
        val request = HttpEntity(emptyMap<String, String>(), headers)
        restTemplate.postForEntity("$baseUrl/interview/stop", request, String::class.java).body
    }

    /**
     * 面接分析データをリセットする
     *
     * @return CompletableFuture<Boolean>
     */
    fun reset(): CompletableFuture<Boolean> = post("/interview/reset")

    /**
     * POSTリクエストを送信するユーティリティメソッド
     *
     * @param path エンドポイントのパス
     * @param body リクエストボディ
     * @return CompletableFuture<Boolean>
     */
    private fun post(path: String, body: Map<String, String> = emptyMap()): CompletableFuture<Boolean> = asyncCall(false) {
        val headers = HttpHeaders().apply {
            contentType = MediaType.APPLICATION_JSON
        }
        val request = HttpEntity(body, headers)
        val response = restTemplate.postForEntity("$baseUrl$path", request, String::class.java)
        response.statusCode.is2xxSuccessful
    }
}