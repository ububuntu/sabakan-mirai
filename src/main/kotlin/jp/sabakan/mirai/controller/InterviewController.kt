package jp.sabakan.mirai.controller

import jp.sabakan.mirai.request.InterviewRequest
import jp.sabakan.mirai.security.LoginUserDetails
import jp.sabakan.mirai.service.InterviewService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.*
import java.security.Principal
import java.util.concurrent.CompletableFuture

/**
 * 面接画面表示用Controller
 */
@Controller
class InterviewViewController {

    @Autowired
    private lateinit var interviewService: InterviewService

    // 面接メイン画面
    @GetMapping("/interview")
    fun getInterview(): String {
        return "/interview/interview-main"
    }

    // Web面接画面
    @GetMapping("/interview/do")
    fun getInterviewDo(): String {
        return "/interview/interview-do"
    }

    //　面接ログ画面
// 面接ログ画面
    @GetMapping("/interview/log")
    fun getInterviewLog(
        @AuthenticationPrincipal userDetails: LoginUserDetails,
        model: Model
    ): String {
        val userId = userDetails.getUserEntity().userId

        val request = InterviewRequest().apply {
            this.userId = userId
        }

        // 直近3件の履歴を取得
        val recentInterviews = interviewService.getInterviews(request)

        // Thymeleafに渡す
        model.addAttribute("recentInterviews", recentInterviews)

        return "/interview/interview-log"
    }

    // 面接結果画面
    @GetMapping("/interview/result")
    fun getInterviewResult(): String {
        return "/interview/interview-result"
    }
}

/**
 * 面接API用Controller
 */
@RestController
@RequestMapping("/interview/api")
class InterviewApiController(
    private val interviewService: InterviewService
) {

    // ★デバッグ用エンドポイント★
    @GetMapping("/test")
    fun testEndpoint(): String {
        return "エンドポイントは動作しています"
    }

    // ========================================
    // GET リクエスト
    // ========================================

    @GetMapping("/analysis/audio-result")
    @ResponseBody
    fun getAudioResult(): CompletableFuture<ResponseEntity<ByteArray>> {
        return interviewService.getAudioResult()
            .thenApply<ResponseEntity<ByteArray>> { audioData ->
                if (audioData.isNotEmpty()) {
                    ResponseEntity.ok()
                        .header("Content-Type", "audio/wav")
                        .body(audioData)
                } else {
                    ResponseEntity.notFound().build()
                }
            }
            .exceptionally { e ->
                ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build()
            }
    }

    @GetMapping("/current-question")
    @ResponseBody
    fun getCurrentQuestion(): ResponseEntity<Map<String, Any>> {
        return try {
            val question = interviewService.getCurrentQuestion()
            val progress = interviewService.getProgress()
            val questionNumber = interviewService.getCurrentQuestionNumber()
            val totalQuestions = interviewService.getTotalQuestions()

            ResponseEntity.ok(
                mapOf(
                    "question" to question,
                    "progress" to progress,
                    "questionNumber" to questionNumber,
                    "totalQuestions" to totalQuestions
                )
            )
        } catch (e: Exception) {
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                mapOf(
                    "status" to "error",
                    "message" to "質問の取得に失敗しました: ${e.message}"
                )
            )
        }
    }

    @GetMapping("/questions")
    @ResponseBody
    fun getAllQuestions(): ResponseEntity<Map<String, Any>> {
        return try {
            val questions = interviewService.getAllQuestions()
            val total = interviewService.getTotalQuestions()

            ResponseEntity.ok(
                mapOf(
                    "questions" to questions,
                    "total" to total
                )
            )
        } catch (e: Exception) {
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                mapOf(
                    "status" to "error",
                    "message" to "質問の取得に失敗しました: ${e.message}"
                )
            )
        }
    }

    @GetMapping("/sessions/result")
    fun getSessionResultByUser(
        principal: Principal?
    ): ResponseEntity<Map<String, Any>> {
        val userId = principal?.name ?: "anonymous"

        return try {
            val result = interviewService.getLatestResultByUserId(userId)

            if (result != null) {
                ResponseEntity.ok(
                    mapOf(
                        "status" to "success",
                        "data" to result
                    )
                )
            } else {
                ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                    mapOf(
                        "status" to "error",
                        "message" to "面接結果が見つかりません。先に面接を完了してください。"
                    )
                )
            }
        } catch (e: Exception) {
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                mapOf(
                    "status" to "error",
                    "message" to "面接結果の取得に失敗しました: ${e.message}"
                )
            )
        }
    }

    @GetMapping("/sessions/{sessionId}/result")
    @ResponseBody
    fun getSessionResult(
        @PathVariable sessionId: String
    ): ResponseEntity<Map<String, Any>> {
        return try {
            val result = interviewService.getSessionResult(sessionId)

            if (result != null) {
                ResponseEntity.ok(
                    mapOf(
                        "status" to "success",
                        "data" to result
                    )
                )
            } else {
                ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                    mapOf(
                        "status" to "error",
                        "message" to "セッション結果が見つかりません"
                    )
                )
            }
        } catch (e: Exception) {
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                mapOf(
                    "status" to "error",
                    "message" to "セッション結果の取得に失敗しました: ${e.message}"
                )
            )
        }
    }

    // ========================================
    // POST リクエスト
    // ========================================

    @PostMapping("/sessions")
    @ResponseBody
    fun startInterviewSession(
        @RequestBody(required = false) request: Map<String, Any>?,
        principal: Principal?
    ): CompletableFuture<ResponseEntity<Map<String, Any>>> {
        val userId = principal?.name ?: "anonymous"

        return interviewService.startInterviewSession(userId, request ?: emptyMap())
            .thenApply<ResponseEntity<Map<String, Any>>> { sessionId ->
                ResponseEntity.ok(
                    mapOf(
                        "status" to "success",
                        "sessionId" to sessionId,
                        "message" to "面接セッションを開始しました"
                    )
                )
            }
            .exceptionally { e ->
                ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    mapOf(
                        "status" to "error",
                        "message" to "面接セッションの開始に失敗しました: ${e.message}"
                    )
                )
            }
    }

    @PostMapping("/sessions/stop")
    @ResponseBody
    fun stopInterviewSession(
        principal: Principal?
    ): CompletableFuture<ResponseEntity<Map<String, Any?>>> {
        val userId = principal?.name ?: "anonymous"

        return interviewService.stopInterviewSessionByUserId(userId)
            .thenApply<ResponseEntity<Map<String, Any?>>> { result ->
                @Suppress("UNCHECKED_CAST")
                val sessionId = result?.let { it["sessionId"] as? String }

                // 点数とコメントを抽出
                @Suppress("UNCHECKED_CAST")
                val scores = result?.let { it["scores"] as? Map<String, Int> } ?: emptyMap()
                @Suppress("UNCHECKED_CAST")
                val comments = result?.let { it["comments"] as? Map<String, String> } ?: emptyMap()

                // 点数をString型に変換
                val scoresStr = scores.mapValues { it.value.toString() }

                ResponseEntity.ok(
                    mapOf(
                        "status" to "success",
                        "sessionId" to sessionId,
                        "scores" to scoresStr,
                        "comments" to comments,
                        "message" to "面接セッションを停止しました"
                    )
                )
            }
            .exceptionally { e ->
                ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    mapOf(
                        "status" to "error",
                        "message" to "面接セッションの停止に失敗しました: ${e.message}"
                    )
                )
            }
    }

    // 既存のsessionId指定版も残しておく（互換性のため）
    @PostMapping("/sessions/{sessionId}/stop")
    @ResponseBody
    fun stopInterviewSessionById(
        @PathVariable sessionId: String
    ): CompletableFuture<ResponseEntity<Map<String, Any?>>> {
        return interviewService.stopInterviewSession(sessionId)
            .thenApply<ResponseEntity<Map<String, Any?>>> { result ->
                // 点数とコメントを抽出
                @Suppress("UNCHECKED_CAST")
                val scores = result?.let { it["scores"] as? Map<String, Int> } ?: emptyMap()
                @Suppress("UNCHECKED_CAST")
                val comments = result?.let { it["comments"] as? Map<String, String> } ?: emptyMap()

                // 点数をString型に変換
                val scoresStr = scores.mapValues { it.value.toString() }

                ResponseEntity.ok(
                    mapOf(
                        "status" to "success",
                        "sessionId" to sessionId,
                        "scores" to scoresStr,
                        "comments" to comments,
                        "message" to "面接セッションを停止しました"
                    )
                )
            }
            .exceptionally { e ->
                ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    mapOf(
                        "status" to "error",
                        "message" to "面接セッションの停止に失敗しました: ${e.message}"
                    )
                )
            }
    }

    @PostMapping("/analysis/start")
    @ResponseBody
    fun startAnalysis(): CompletableFuture<ResponseEntity<Map<String, Any>>> {
        return interviewService.startAnalysis()
            .thenApply<ResponseEntity<Map<String, Any>>> { success ->
                if (success) {
                    ResponseEntity.ok(
                        mapOf(
                            "status" to "success",
                            "message" to "AI分析を開始しました"
                        )
                    )
                } else {
                    ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                        mapOf(
                            "status" to "error",
                            "message" to "AI分析の開始に失敗しました"
                        )
                    )
                }
            }
            .exceptionally { e ->
                ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    mapOf(
                        "status" to "error",
                        "message" to "AI分析の開始に失敗しました: ${e.message}"
                    )
                )
            }
    }

    @PostMapping("/analysis/audio")
    @ResponseBody
    fun analyzeAudio(
        @RequestBody request: Map<String, String>
    ): CompletableFuture<ResponseEntity<Map<String, Any>>> {
        val audio = request["audio"]
        if (audio.isNullOrBlank()) {
            return CompletableFuture.completedFuture(
                ResponseEntity.badRequest().body(
                    mapOf(
                        "status" to "error",
                        "message" to "音声データが指定されていません"
                    )
                )
            )
        }

        return interviewService.analyzeAudio(audio)
            .thenApply<ResponseEntity<Map<String, Any>>> { success ->
                if (success) {
                    ResponseEntity.ok(
                        mapOf(
                            "status" to "success",
                            "message" to "音声分析が完了しました"
                        )
                    )
                } else {
                    ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                        mapOf(
                            "status" to "error",
                            "message" to "音声分析に失敗しました"
                        )
                    )
                }
            }
            .exceptionally { e ->
                ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    mapOf(
                        "status" to "error",
                        "message" to "音声分析に失敗しました: ${e.message}"
                    )
                )
            }
    }

    @PostMapping("/analysis/frame")
    @ResponseBody
    fun analyzeFrame(
        @RequestBody request: Map<String, String>
    ): CompletableFuture<ResponseEntity<Map<String, Any>>> {
        val image = request["image"]
        if (image.isNullOrBlank()) {
            return CompletableFuture.completedFuture(
                ResponseEntity.badRequest().body(
                    mapOf(
                        "status" to "error",
                        "message" to "画像データが指定されていません"
                    )
                )
            )
        }

        return interviewService.analyzeFrame(image)
            .thenApply<ResponseEntity<Map<String, Any>>> { success ->
                if (success) {
                    ResponseEntity.ok(
                        mapOf(
                            "status" to "success",
                            "message" to "画像分析が完了しました"
                        )
                    )
                } else {
                    ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                        mapOf(
                            "status" to "error",
                            "message" to "画像分析に失敗しました"
                        )
                    )
                }
            }
            .exceptionally { e ->
                ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    mapOf(
                        "status" to "error",
                        "message" to "画像分析に失敗しました: ${e.message}"
                    )
                )
            }
    }

    @PostMapping("/analysis/reset")
    @ResponseBody
    fun resetAnalysis(): CompletableFuture<ResponseEntity<Map<String, Any>>> {
        return interviewService.resetAnalysis()
            .thenApply<ResponseEntity<Map<String, Any>>> { success ->
                if (success) {
                    ResponseEntity.ok(
                        mapOf(
                            "status" to "success",
                            "message" to "AI分析をリセットしました"
                        )
                    )
                } else {
                    ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                        mapOf(
                            "status" to "error",
                            "message" to "AI分析のリセットに失敗しました"
                        )
                    )
                }
            }
            .exceptionally { e ->
                ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    mapOf(
                        "status" to "error",
                        "message" to "AI分析のリセットに失敗しました: ${e.message}"
                    )
                )
            }
    }

    @PostMapping("/next-question")
    @ResponseBody
    fun getNextQuestion(): ResponseEntity<Map<String, Any?>> {
        return try {
            val nextQuestion = interviewService.getNextQuestion()
            val progress = interviewService.getProgress()
            val questionNumber = interviewService.getCurrentQuestionNumber()
            val totalQuestions = interviewService.getTotalQuestions()
            val hasNext = interviewService.hasNextQuestion()
            val isFinished = nextQuestion == null

            ResponseEntity.ok(
                mapOf(
                    "question" to nextQuestion,
                    "progress" to progress,
                    "questionNumber" to questionNumber,
                    "totalQuestions" to totalQuestions,
                    "hasNext" to hasNext,
                    "isFinished" to isFinished
                )
            )
        } catch (e: Exception) {
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                mapOf(
                    "status" to "error",
                    "message" to "次の質問の取得に失敗しました: ${e.message}"
                )
            )
        }
    }

    @PostMapping("/questions/reset")
    @ResponseBody
    fun resetQuestions(): ResponseEntity<Map<String, String>> {
        return try {
            interviewService.resetQuestions()
            ResponseEntity.ok(
                mapOf(
                    "status" to "success",
                    "message" to "質問をリセットしました"
                )
            )
        } catch (e: Exception) {
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                mapOf(
                    "status" to "error",
                    "message" to "質問のリセットに失敗しました: ${e.message}"
                )
            )
        }
    }
}