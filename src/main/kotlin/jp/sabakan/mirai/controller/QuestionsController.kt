package jp.sabakan.mirai.controller

import jakarta.servlet.http.HttpSession
import jp.sabakan.mirai.request.CabGabRequest
import jp.sabakan.mirai.request.SpiRequest
import jp.sabakan.mirai.security.LoginUserDetails
import jp.sabakan.mirai.service.CabGabService
import jp.sabakan.mirai.service.SpiService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.servlet.mvc.support.RedirectAttributes

@Controller
class QuestionsController {

    @Autowired
    lateinit var spiService: SpiService
    @Autowired
    lateinit var cabGabService: CabGabService
    @Autowired
    lateinit var session: HttpSession

    // 問題数の定数化
    val SPI_TOTAL_COUNT = 70
    val CABGAB_TOTAL_COUNT = 90

    // --- 1. 共通・メイン画面 ---

    @GetMapping("/questions")
    fun getQuestions(): String = "questions/questions-main"

    @PostMapping("/questions")
    fun postQuestions(): String = "questions/questions-main"

    // --- 2. SPI 制御ロジック ---
// --- 1. SPI メイン画面 (GET /spi) ---
    @GetMapping("/spi")
    fun getSpiMain(
        model: Model,
        @AuthenticationPrincipal userDetails: LoginUserDetails
    ): String {
        val userId = userDetails.getUserEntity().userId
        val request = SpiRequest().apply { this.userId = userId }

        // 続きからボタンの表示制御
        val inProgressId = spiService.getInProgressSpiId(request)
        model.addAttribute("hasInProgress", inProgressId != null)

        return "questions/spi-main"
    }

    // --- 2. SPI 試験開始 (POST /spi/start) ---
    @PostMapping("/spi/start")
    fun startSpiExam(
        @RequestParam mode: String,
        @RequestParam(required = false) middleIndex: Int?,
        @AuthenticationPrincipal userDetails: LoginUserDetails
    ): String {
        val userId = userDetails.getUserEntity().userId
        val request = SpiRequest().apply { this.userId = userId }

        var hsId: String? = null
        var startIndex = 1

        when (mode) {
            "new", "new_middle" -> {
                // 新規開始
                hsId = spiService.startExam(request)
                startIndex = if (mode == "new_middle") middleIndex ?: 1 else 1
            }
            "resume" -> {
                // 続きから
                hsId = spiService.getInProgressSpiId(request)
                // 本来はどこまで解いたか判定するロジックが必要だが、一旦1問目or任意実装
                startIndex = 1
            }
        }

        if (hsId == null) return "redirect:/spi"

        session.setAttribute("currentSpiExamId", hsId)
        return "redirect:/spi/study?index=$startIndex"
    }

    // --- 3. SPI 問題画面 (GET /spi/study) ---
    @GetMapping("/spi/study")
    fun getSpiStudy(@RequestParam("index") index: Int, model: Model): String {
        val hsId = session.getAttribute("currentSpiExamId") as? String ?: return "redirect:/spi"

        val request = SpiRequest().apply { spiHsId = hsId }
        val allQuestions = spiService.getExamResults(request)

        // index(question_number)に一致する問題を取得
        // カラム名はDB準拠(question_number)か、Map変換後の名前に注意
        val currentQuestion = allQuestions.find { it["question_number"] == index } ?: return "redirect:/spi/result?id=$hsId"

        model.addAttribute("question", currentQuestion)
        model.addAttribute("currentIndex", index)
        model.addAttribute("totalCount", 70)
        // 進捗率
        model.addAttribute("progress", (index.toDouble() / 70 * 100).toInt())

        return "questions/spi-study"
    }

    // --- 4. SPI 回答送信 (POST /spi/study) ---
    @PostMapping("/spi/study")
    fun postSpiStudy(
        @RequestParam currentIndex: Int,
        @RequestParam spiId: String,
        @RequestParam(required = false) answer: Int?, // HTML側valueが1~4ならInt
        @RequestParam action: String
    ): String {
        val hsId = session.getAttribute("currentSpiExamId") as? String ?: return "redirect:/spi"

        val request = SpiRequest().apply {
            this.spiHsId = hsId
            this.questionNumber = currentIndex
            this.spiId = spiId
            this.userAnswer = answer
        }

        when (action) {
            "suspend" -> {
                session.removeAttribute("currentSpiExamId")
                return "redirect:/spi"
            }
            "finish" -> {
                spiService.saveAnswer(request)
                spiService.finishExam(request)
                session.removeAttribute("currentSpiExamId")
                return "redirect:/spi/result?id=$hsId"
            }
            "next" -> {
                spiService.saveAnswer(request)
                val nextIndex = currentIndex + 1
                return if (nextIndex > 70) {
                    spiService.finishExam(request)
                    session.removeAttribute("currentSpiExamId")
                    "redirect:/spi/result?id=$hsId"
                } else {
                    "redirect:/spi/study?index=$nextIndex"
                }
            }
        }
        return "redirect:/spi"
    }

    // --- 5. SPI 結果画面 (GET /spi/result) ---
    @GetMapping("/spi/result")
    fun getSpiResult(@RequestParam("id") hsId: String, model: Model): String {
        val request = SpiRequest().apply { spiHsId = hsId }
        val results = spiService.getExamResults(request) // 全70問の明細を取得

        // 正答率計算 (回答したもののみを母数とする場合)
        val answeredList = results.filter { it["user_answer"] != null }
        val correctCount = answeredList.count { it["is_correct"] == true }

        val accuracyRate = if (answeredList.isNotEmpty()) {
            (correctCount.toDouble() / answeredList.size * 100).toInt()
        } else 0

        model.addAttribute("results", results)
        model.addAttribute("accuracyRate", accuracyRate)

        return "questions/spi-result"
    }

    // POSTは不要、もしくはGETへリダイレクト
    @PostMapping("/spi/result")
    fun postSpiResult(): String = "redirect:/spi"

    // --- 6. SPI 履歴画面 (GET /spi/history) ---
    @GetMapping("/spi/history")
    fun getSpiHistory(
        model: Model,
        @AuthenticationPrincipal userDetails: LoginUserDetails
    ): String {
        val userId = userDetails.getUserEntity().userId
        val request = SpiRequest().apply { this.userId = userId }

        val historyList = spiService.getHistoryList(request)
        model.addAttribute("historyList", historyList)

        return "questions/spi-history"
    }

    // --- 3. CAB/GAB 制御ロジック ---

    /**
     * CAB/GAB メイン画面表示
     */
    @GetMapping("/cabgab")
    fun getQuestionCabgabMain(
        model: Model,
        @AuthenticationPrincipal userDetails: LoginUserDetails
    ): String {
        val userId = userDetails.getUserEntity().userId
        val request = CabGabRequest().apply {
            this.userId = userId
        }

        val inProgressId = cabGabService.getInProgressCabGabId(request)
        model.addAttribute("hasInProgress", inProgressId != null)
        return "questions/cabgab-main"
    }

    /**
     * CAB/GAB 試験開始処理
     */
    @PostMapping("/cabgab/start")
    fun startCabGabExam(
        @RequestParam mode: String,
        @RequestParam(required = false) middleIndex: Int?,
        model: Model,
        @AuthenticationPrincipal userDetails: LoginUserDetails
    ): String {
        var userId = userDetails.getUserEntity().userId
        val request = CabGabRequest().apply {
            this.userId = userId
        }

        var examId: String? = null
        var startIndex = 1

        when (mode) {
            "new_start" -> {
                examId = cabGabService.startNewCabGab(request)
                startIndex = 1
            }
            "new_middle" -> {
                examId = cabGabService.startNewCabGab(request)
                startIndex = middleIndex ?: 1
            }
            "resume" -> {
                examId = cabGabService.getInProgressCabGabId(request)
                if (examId != null) {
                    startIndex = cabGabService.getCurrentCabGabIndex(examId)
                } else {
                    return "redirect:/cabgab"
                }
            }
        }
        session.setAttribute("currentCabGabExamId", examId)
        return "redirect:/cabgab/study?index=$startIndex"
    }

    /**
     * CAB/GAB 学習（問題回答）画面表示
     */
    @GetMapping("/cabgab/study")
    fun getQuestionCabgabStudy(
        @RequestParam(name = "index") index: Int,
        model: Model
    ): String {
        val examId = session.getAttribute("currentCabGabExamId") as? String ?: return "redirect:/cabgab"
        val targetCategory = if (index <= 40) "言語" else "非言語"

        val request = CabGabRequest().apply { cabGabCategory = targetCategory }
        val response = cabGabService.getCabGab(request)
        val questionList = response.cabGabs

        if (questionList.isNullOrEmpty()) return "redirect:/cabgab"

        model.addAttribute("question", questionList[0])
        model.addAttribute("currentIndex", index)
        model.addAttribute("totalCount", CABGAB_TOTAL_COUNT)
        model.addAttribute("progress", (index.toDouble() / CABGAB_TOTAL_COUNT * 100).toInt())

        return "questions/cabgab-study"
    }

    /**
     * CAB/GAB 回答送信・中断・終了処理
     */
    @PostMapping("/cabgab/study")
    fun postQuestionCabgabStudy(
        @RequestParam currentIndex: Int,
        @RequestParam cabGabId: String,
        @RequestParam(required = false) answer: Int?,
        @RequestParam action: String
    ): String {
        val examId = session.getAttribute("currentCabGabExamId") as? String ?: return "redirect:/cabgab"

        when (action) {
            "suspend" -> {
                session.removeAttribute("currentCabGabExamId")
                return "redirect:/cabgab"
            }
            "finish" -> {
                cabGabService.finishCabGab(examId)
                session.removeAttribute("currentCabGabExamId")
                return "redirect:/cabgab/result"
            }
            "next" -> {
                if (answer != null) {
                    cabGabService.saveOneCabGabAnswer(examId, cabGabId, answer)
                }

                val nextIndex = currentIndex + 1
                if (nextIndex > CABGAB_TOTAL_COUNT) {
                    cabGabService.finishCabGab(examId)
                    session.removeAttribute("currentCabGabExamId")
                    return "redirect:/cabgab/result"
                }
                return "redirect:/cabgab/study?index=$nextIndex"
            }
        }
        return "redirect:/cabgab"
    }

    // --- 4. 結果・履歴表示 ---
    // -- CAB/GAB 結果・履歴 --
    @GetMapping("/cabgab/result")
    fun getQuestionCabgabResult(): String = "questions/cabgab-result"

    @PostMapping("/cabgab/result")
    fun postQuestionCabgabResult(): String = "questions/cabgab-result"

    @GetMapping("/cabgab/history")
    fun getCabgabHistory(): String = "questions/cabgab-history"

    @PostMapping("/cabgab/history")
    fun postCabgabHistory(): String = "questions/cabgab-history"
}