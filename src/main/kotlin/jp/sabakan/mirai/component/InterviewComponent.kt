package jp.sabakan.mirai.component

import org.springframework.beans.factory.annotation.Value
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
    @Value("\${interview.api.url:http://ai-1:8000}")
    private val baseUrl: String
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
     * 面接分析の点数結果を取得する
     *
     * @return CompletableFuture<InterviewScoreResult?>
     */
    fun getScoreResult(): CompletableFuture<InterviewScoreResult?> = asyncCall(null) {
        val response = restTemplate.getForEntity(
            "$baseUrl/interview/score",
            InterviewScoreResult::class.java
        )
        response.body
    }

    /**
     * 面接分析の点数結果データクラス
     */
    data class InterviewScoreResult(
        val expressionScore: Int,
        val eyesScore: Int,
        val postureScore: Int,
        val speechSpeedScore: Int,
        val totalScore: Int
    )

    /**
     * 面接分析を停止して点数を取得する
     *
     * @return CompletableFuture<InterviewScoreResult?>
     */
    fun stopAnalysis(): CompletableFuture<InterviewScoreResult?> {
        return CompletableFuture.supplyAsync {
            try {
                val headers = HttpHeaders().apply {
                    contentType = MediaType.APPLICATION_JSON
                    accept = listOf(MediaType.APPLICATION_JSON)
                }
                val request = HttpEntity(emptyMap<String, String>(), headers)

                val response = restTemplate.postForEntity(
                    "$baseUrl/interview/stop",
                    request,
                    InterviewScoreResult::class.java
                )

                response.body
            } catch (e: Exception) {
                throw e
            }
        }
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

/**
 * 面接評価に基づいてコメントを生成するユーティリティクラス
 */
@Component
class InterviewCommentGenerator {

    companion object {
        const val OPTIMAL_MIN = 251
        const val OPTIMAL_MAX = 350
        const val OPTIMAL_CENTER = (OPTIMAL_MIN + OPTIMAL_MAX) / 2
    }

    /**
     * 1分間あたりの文字数から発話速度の点数を計算
     *
     * @param charsPerMinute 1分間あたりの文字数
     * @return 発話速度の点数（0-100）
     */
    fun calculateSpeechSpeedScore(charsPerMinute: Int): Int {
        return when {
            charsPerMinute in OPTIMAL_MIN..OPTIMAL_MAX -> 100
            charsPerMinute in (OPTIMAL_MIN - 50)..(OPTIMAL_MAX + 50) -> 80
            charsPerMinute in (OPTIMAL_MIN - 75)..(OPTIMAL_MAX + 75) -> 60
            charsPerMinute in (OPTIMAL_MIN - 100)..(OPTIMAL_MAX + 100) -> 40
            else -> 20
        }
    }

    /**
     * 表情評価に基づいてコメントを生成
     *
     * @param score 表情の点数
     * @return 表情に関するコメント
     */
    fun generateExpressionComment(score: Int): String {
        return when {
            score >= 90 -> "表情が非常に良好です。その自然な表情を維持してください。"
            score >= 80 -> "表情は良好です。話し始める前に一呼吸置いてみましょう。"
            score >= 70 -> "表情は平均的です。内容を伝えることに集中して大丈夫です。"
            score >= 60 -> "表情が硬い場面がありました。口角を少し意識するだけで印象が変わります。"
            else -> "緊張から表情が硬くなる場面がありました。"
        }
    }

    /**
     * 視線評価に基づいてコメントを生成
     *
     * @param score 視線の点数
     * @return 視線に関するコメント
     */
    fun generateEyesComment(score: Int): String {
        return when {
            score >= 90 -> "アイコンタクトが非常に良好です。聞くときと話すときの切り替えが上手です。"
            score >= 80 -> "アイコンタクトは良好です。圧迫感のない、ちょうどよい視線です。"
            score >= 70 -> "アイコンタクトは平均的です。話すときは、画面ではなくカメラ付近を見る意識をしてみましょう。"
            score >= 60 -> "視線が不安定な場面がありました。画面との距離と姿勢を整えると、視線が安定しやすくなります。"
            else -> "視線が不安定な場面がありました。"
        }
    }

    /**
     * 姿勢評価に基づいてコメントを生成
     *
     * @param score 姿勢の点数
     * @return 姿勢に関するコメント
     */
    fun generatePostureComment(score: Int): String {
        return when {
            score >= 90 -> "姿勢が非常に良好です。姿勢が安定していて、落ち着いた印象があります。"
            score >= 80 -> "姿勢は良好です。長時間でも崩れにくい姿勢が取れています。"
            score >= 70 -> "姿勢は平均的です。背もたれに頼りすぎず、軽く背筋を伸ばす意識を持ってみてください。"
            score >= 60 -> "姿勢が崩れる場面がありました。画面の高さを調整すると、無理のない姿勢になります。"
            else -> "姿勢が崩れる場面がありました。"
        }
    }

    /**
     * 発話速度評価に基づいてコメントを生成
     *
     * @param charsPerMinute 1分間あたりの文字数
     * @return 発話速度に関するコメント
     */
    fun generateSpeechSpeedComment(charsPerMinute: Int): String {
        return when {
            charsPerMinute in 251..350 -> "間の取り方が上手で、内容が理解しやすいです。"
            charsPerMinute >= 351 -> "話すスピードが少し速いです。要点を短く区切って話すと、速度が安定します。"
            charsPerMinute in 201..250 -> "発話速度は良好ですが、一文を短くして、テンポよく話してみましょう。"
            else -> "話すスピードがゆっくりすぎます。もう少しテンポを上げましょう。"
        }
    }

    /**
     * 4項目の評価結果を生成
     *
     * @param expressionScore 表情の点数
     * @param eyesScore 視線の点数
     * @param postureScore 姿勢の点数
     * @param speechSpeedScore 発話速度の点数
     * @return 4項目の評価結果マップ
     */
    fun generateEvaluationResults(
        expressionScore: Int,
        eyesScore: Int,
        postureScore: Int,
        speechSpeedScore: Int
    ): Map<String, String> {
        return mapOf(
            "表情" to generateExpressionComment(expressionScore),
            "視線" to generateEyesComment(eyesScore),
            "姿勢" to generatePostureComment(postureScore),
            "発話速度" to generateSpeechSpeedComment(speechSpeedScore)
        )
    }

    /**
     * 改善ポイントを抽出
     *
     * @param expressionScore 表情の点数
     * @param eyesScore 視線の点数
     * @param postureScore 姿勢の点数
     * @param speechSpeedScore 発話速度の点数
     * @return 改善が必要な項目のリスト
     */
    fun extractImprovementPoints(
        expressionScore: Int,
        eyesScore: Int,
        postureScore: Int,
        speechSpeedScore: Int
    ): List<String> {
        val improvements = mutableListOf<String>()

        if (expressionScore < 70) improvements.add("表情の改善が必要です")
        if (eyesScore < 70) improvements.add("アイコンタクトの改善が必要です")
        if (postureScore < 70) improvements.add("姿勢の改善が必要です")
        if (speechSpeedScore < 70) improvements.add("発話速度の改善が必要です")

        return improvements
    }

    /**
     * 強みを抽出
     *
     * @param expressionScore 表情の点数
     * @param eyesScore 視線の点数
     * @param postureScore 姿勢の点数
     * @param speechSpeedScore 発話速度の点数
     * @return 良好な項目のリスト
     */
    fun extractStrengths(
        expressionScore: Int,
        eyesScore: Int,
        postureScore: Int,
        speechSpeedScore: Int
    ): List<String> {
        val strengths = mutableListOf<String>()

        if (expressionScore >= 80) strengths.add("表情が良好です")
        if (eyesScore >= 80) strengths.add("アイコンタクトが良好です")
        if (postureScore >= 80) strengths.add("姿勢が良好です")
        if (speechSpeedScore >= 80) strengths.add("発話速度が良好です")

        return strengths
    }
}