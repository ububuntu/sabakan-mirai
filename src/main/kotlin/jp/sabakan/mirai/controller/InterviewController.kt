package jp.sabakan.mirai.controller

import jp.sabakan.mirai.service.InterviewService
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.*
import java.security.Principal
import java.util.concurrent.CompletableFuture

/**
 * 面接画面表示用Controller
 */
@Controller
class InterviewViewController {

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
    @GetMapping("/interview/log")
    fun getInterviewLog(): String {
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
@Controller
@RequestMapping("/interview/api")
class InterviewApiController(
    private val interviewService: InterviewService
) {
    private val logger = LoggerFactory.getLogger(InterviewApiController::class.java)

    // ========================================
    // GET リクエスト
    // ========================================

    @GetMapping("/analysis/audio-result")
    @ResponseBody
    fun getAudioResult(): CompletableFuture<ResponseEntity<ByteArray>> {
        logger.info("音声結果取得リクエスト")

        return interviewService.getAudioResult()
            .thenApply<ResponseEntity<ByteArray>> { audioData ->
                if (audioData.isNotEmpty()) {
                    logger.info("音声結果取得成功: サイズ=${audioData.size}")
                    ResponseEntity.ok()
                        .header("Content-Type", "audio/wav")
                        .body(audioData)
                } else {
                    logger.warn("音声結果が見つかりません")
                    ResponseEntity.notFound().build()
                }
            }
            .exceptionally { e ->
                logger.error("音声結果取得エラー", e)
                ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build()
            }
    }

    @GetMapping("/current-question")
    @ResponseBody
    fun getCurrentQuestion(): ResponseEntity<Map<String, Any>> {
        return try {
            logger.info("現在の質問取得リクエスト")
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
            logger.error("現在の質問取得エラー", e)
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
            logger.info("全質問取得リクエスト")
            val questions = interviewService.getAllQuestions()
            val total = interviewService.getTotalQuestions()

            ResponseEntity.ok(
                mapOf(
                    "questions" to questions,
                    "total" to total
                )
            )
        } catch (e: Exception) {
            logger.error("全質問取得エラー", e)
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                mapOf(
                    "status" to "error",
                    "message" to "質問の取得に失敗しました: ${e.message}"
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
            logger.info("セッション結果取得リクエスト: sessionId=$sessionId")

            val result = interviewService.getSessionResult(sessionId)

            if (result != null) {
                logger.info("セッション結果取得成功: sessionId=$sessionId")
                ResponseEntity.ok(
                    mapOf(
                        "status" to "success",
                        "data" to result
                    )
                )
            } else {
                logger.warn("セッション結果が見つかりません: sessionId=$sessionId")
                ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                    mapOf(
                        "status" to "error",
                        "message" to "セッション結果が見つかりません"
                    )
                )
            }
        } catch (e: Exception) {
            logger.error("セッション結果取得エラー: sessionId=$sessionId", e)
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
        logger.info("面接セッション開始リクエスト: userId=$userId")

        return interviewService.startInterviewSession(userId, request ?: emptyMap())
            .thenApply<ResponseEntity<Map<String, Any>>> { sessionId ->
                logger.info("面接セッション開始成功: sessionId=$sessionId")
                ResponseEntity.ok(
                    mapOf(
                        "status" to "success",
                        "sessionId" to sessionId,
                        "message" to "面接セッションを開始しました"
                    )
                )
            }
            .exceptionally { e ->
                logger.error("面接セッション開始エラー", e)
                ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    mapOf(
                        "status" to "error",
                        "message" to "面接セッションの開始に失敗しました: ${e.message}"
                    )
                )
            }
    }

    @PostMapping("/sessions/{sessionId}/stop")
    @ResponseBody
    fun stopInterviewSession(
        @PathVariable sessionId: String
    ): CompletableFuture<ResponseEntity<Map<String, Any?>>> {
        logger.info("面接セッション停止リクエスト: sessionId=$sessionId")

        return interviewService.stopInterviewSession(sessionId)
            .thenApply<ResponseEntity<Map<String, Any?>>> { result ->
                logger.info("面接セッション停止成功: sessionId=$sessionId")

                // 点数とコメントを抽出
                @Suppress("UNCHECKED_CAST")
                val scores = result["scores"] as? Map<String, Int> ?: emptyMap()
                @Suppress("UNCHECKED_CAST")
                val comments = result["comments"] as? Map<String, String> ?: emptyMap()

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
                logger.error("面接セッション停止エラー: sessionId=$sessionId", e)
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
        logger.info("AI分析開始リクエスト")

        return interviewService.startAnalysis()
            .thenApply<ResponseEntity<Map<String, Any>>> { success ->
                if (success) {
                    logger.info("AI分析開始成功")
                    ResponseEntity.ok(
                        mapOf(
                            "status" to "success",
                            "message" to "AI分析を開始しました"
                        )
                    )
                } else {
                    logger.warn("AI分析開始失敗")
                    ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                        mapOf(
                            "status" to "error",
                            "message" to "AI分析の開始に失敗しました"
                        )
                    )
                }
            }
            .exceptionally { e ->
                logger.error("AI分析開始エラー", e)
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
            logger.warn("音声データが空です")
            return CompletableFuture.completedFuture(
                ResponseEntity.badRequest().body(
                    mapOf(
                        "status" to "error",
                        "message" to "音声データが指定されていません"
                    )
                )
            )
        }

        logger.info("音声分析リクエスト: データサイズ=${audio.length}")

        return interviewService.analyzeAudio(audio)
            .thenApply<ResponseEntity<Map<String, Any>>> { success ->
                if (success) {
                    logger.info("音声分析成功")
                    ResponseEntity.ok(
                        mapOf(
                            "status" to "success",
                            "message" to "音声分析が完了しました"
                        )
                    )
                } else {
                    logger.warn("音声分析失敗")
                    ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                        mapOf(
                            "status" to "error",
                            "message" to "音声分析に失敗しました"
                        )
                    )
                }
            }
            .exceptionally { e ->
                logger.error("音声分析エラー", e)
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
            logger.warn("画像データが空です")
            return CompletableFuture.completedFuture(
                ResponseEntity.badRequest().body(
                    mapOf(
                        "status" to "error",
                        "message" to "画像データが指定されていません"
                    )
                )
            )
        }

        logger.info("画像分析リクエスト: データサイズ=${image.length}")

        return interviewService.analyzeFrame(image)
            .thenApply<ResponseEntity<Map<String, Any>>> { success ->
                if (success) {
                    logger.info("画像分析成功")
                    ResponseEntity.ok(
                        mapOf(
                            "status" to "success",
                            "message" to "画像分析が完了しました"
                        )
                    )
                } else {
                    logger.warn("画像分析失敗")
                    ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                        mapOf(
                            "status" to "error",
                            "message" to "画像分析に失敗しました"
                        )
                    )
                }
            }
            .exceptionally { e ->
                logger.error("画像分析エラー", e)
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
        logger.info("AI分析リセットリクエスト")

        return interviewService.resetAnalysis()
            .thenApply<ResponseEntity<Map<String, Any>>> { success ->
                if (success) {
                    logger.info("AI分析リセット成功")
                    ResponseEntity.ok(
                        mapOf(
                            "status" to "success",
                            "message" to "AI分析をリセットしました"
                        )
                    )
                } else {
                    logger.warn("AI分析リセット失敗")
                    ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                        mapOf(
                            "status" to "error",
                            "message" to "AI分析のリセットに失敗しました"
                        )
                    )
                }
            }
            .exceptionally { e ->
                logger.error("AI分析リセットエラー", e)
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
            logger.info("次の質問リクエスト")
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
            logger.error("次の質問取得エラー", e)
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
            logger.info("質問リセットリクエスト")
            interviewService.resetQuestions()
            ResponseEntity.ok(
                mapOf(
                    "status" to "success",
                    "message" to "質問をリセットしました"
                )
            )
        } catch (e: Exception) {
            logger.error("質問リセットエラー", e)
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                mapOf(
                    "status" to "error",
                    "message" to "質問のリセットに失敗しました: ${e.message}"
                )
            )
        }
    }
}