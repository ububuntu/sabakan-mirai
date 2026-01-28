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

@Controller
class QuestionsController {

    @Autowired
    lateinit var spiService: SpiService
    @Autowired
    lateinit var cabGabService: CabGabService
    @Autowired
    lateinit var session: HttpSession
    // 問題数の定数化（必要に応じて調整してください）
    val SPI_TOTAL_COUNT = 70
    val CABGAB_TOTAL_COUNT = 90

    // --- 共通・メイン画面 ---

    @GetMapping("/questions")
    fun getQuestions(): String = "questions/questions-main"

    @PostMapping("/questions")
    fun postQuestions(): String = "questions/questions-main"

    // --- SPI 制御 (既存ロジック維持) ---

    @GetMapping("/spi")
    fun getQuestionSpiMain(
        model: Model,
        @AuthenticationPrincipal userDetails: LoginUserDetails
    ): String {
        // ユーザーID取得
        val userId = userDetails.getUserEntity().userId
        val request = SpiRequest().apply {
            this.userId = userId
        }

        // 進行中の試験があるか確認
        val inProgressId = spiService.getInProgressExamId(request)
        model.addAttribute("hasInProgress", inProgressId != null)
        return "questions/spi-main"
    }

    @PostMapping("/spi/start")
    fun startSpiExam(
        @RequestParam mode: String,
        @RequestParam(required = false) middleIndex: Int?,
        model: Model,
        @AuthenticationPrincipal userDetails: LoginUserDetails
    ): String {
        // ユーザーID取得
        val userId = userDetails.getUserEntity().userId
        val request = SpiRequest().apply {
            this.userId = userId
        }

        var examId: String? = null
        var startIndex = 1

        when (mode) {
            "new_start" -> {
                examId = spiService.startNewExam(request)
                startIndex = 1
            }
            "new_middle" -> {
                examId = spiService.startNewExam(request)
                startIndex = middleIndex ?: 1
            }
            "resume" -> {
                examId = spiService.getInProgressExamId(request)
                if (examId != null) {
                    startIndex = spiService.getCurrentQuestionIndex(examId)
                } else {
                    return "redirect:/spi"
                }
            }
        }
        session.setAttribute("currentSpiExamId", examId)
        return "redirect:/spi/study?index=$startIndex"
    }

    @GetMapping("/spi/study")
    fun getQuestionSpiStudy(@RequestParam(name = "index") index: Int, model: Model): String {
        val examId = session.getAttribute("currentSpiExamId") as? String ?: return "redirect:/spi"
        val targetCategory = if (index <= 40) "言語" else "非言語"

        val response = spiService.getSpi(SpiRequest().apply { spiCategory = targetCategory })
        val questionList = response.spis

        if (questionList.isNullOrEmpty()) return "redirect:/spi"

        model.addAttribute("question", questionList[0])
        model.addAttribute("currentIndex", index)
        model.addAttribute("totalCount", SPI_TOTAL_COUNT)
        model.addAttribute("progress", (index.toDouble() / SPI_TOTAL_COUNT * 100).toInt())

        return "questions/spi-study"
    }

    @PostMapping("/spi/study")
    fun postQuestionSpiStudy(
        @RequestParam currentIndex: Int,
        @RequestParam spiId: String,
        @RequestParam(required = false) answer: Int?,
        @RequestParam action: String
    ): String {
        val examId = session.getAttribute("currentSpiExamId") as? String ?: return "redirect:/spi"

        when (action) {
            "suspend" -> {
                session.removeAttribute("currentSpiExamId")
                return "redirect:/spi"
            }
            "finish" -> {
                spiService.finishExam(examId)
                session.removeAttribute("currentSpiExamId")
                return "redirect:/spi/result"
            }
            "next" -> {
                if (answer != null) spiService.saveOneAnswer(examId, spiId, answer)
                val nextIndex = currentIndex + 1
                if (nextIndex > SPI_TOTAL_COUNT) {
                    spiService.finishExam(examId)
                    session.removeAttribute("currentSpiExamId")
                    return "redirect:/spi/result"
                }
                return "redirect:/spi/study?index=$nextIndex"
            }
        }
        return "redirect:/spi"
    }

    // --- CAB/GAB 制御 (SpiServiceの仕様に合わせて修正) ---

    @GetMapping("/cabgab")
    fun getQuestionCabgabMain(
        model: Model,
        @AuthenticationPrincipal userDetails: LoginUserDetails
    ): String {
        // ユーザーID取得
        val userId = userDetails.getUserEntity().userId
        val request = CabGabRequest().apply {
            this.userId = userId
        }

        // 進行中の試験があるか確認
        val inProgressId = cabGabService.getInProgressCabGabId(request)
        model.addAttribute("hasInProgress", inProgressId != null)
        return "questions/cabgab-main"
    }

    @PostMapping("/cabgab/start")
    fun startCabGabExam(
        @RequestParam mode: String,
        @RequestParam(required = false) middleIndex: Int?,
        model: Model,
        @AuthenticationPrincipal userDetails: LoginUserDetails
    ): String {
        // ユーザーID取得
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
        // セッションにCabGab用の試験IDを保存
        session.setAttribute("currentCabGabExamId", examId)
        return "redirect:/cabgab/study?index=$startIndex"
    }

    @GetMapping("/cabgab/study")
    fun getQuestionCabgabStudy(
        @RequestParam(name = "index") index: Int,
        model: Model
    ): String {
        // セッションチェック
        val examId = session.getAttribute("currentCabGabExamId") as? String ?: return "redirect:/cabgab"

        // カテゴリ決定ロジック
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

    // --- 結果・履歴画面 ---

    @GetMapping("/spi/result")
    fun getQuestionSpiResult(): String = "questions/spi-result"

    @GetMapping("/spi/history")
    fun getSpiHistory(): String = "questions/spi-history"

    @GetMapping("/cabgab/result")
    fun getQuestionCabgabResult(): String = "questions/cabgab-result"

    @GetMapping("/cabgab/history")
    fun getCabgabHistory(): String = "questions/cabgab-history"

    @PostMapping("/spi/result")
    fun postQuestionSpiResult(): String = "questions/spi-result"

    @PostMapping("/spi/history")
    fun postSpiHistory(): String = "questions/spi-history"

    @PostMapping("/cabgab/result")
    fun postQuestionCabgabResult(): String = "questions/cabgab-result"

    @PostMapping("/cabgab/history")
    fun postCabgabHistory(): String = "questions/cabgab-history"
}