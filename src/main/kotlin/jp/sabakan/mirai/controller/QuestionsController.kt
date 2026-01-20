package jp.sabakan.mirai.controller

import jakarta.servlet.http.HttpSession
import jp.sabakan.mirai.request.CabGabRequest
import jp.sabakan.mirai.request.SpiRequest
import jp.sabakan.mirai.service.CabGabService
import jp.sabakan.mirai.service.SpiService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestParam

@Controller
class QuestionsController {

    // Serviceを使えるように注入します
    @Autowired
    lateinit var spiService: SpiService
    @Autowired
    lateinit var cabGabService: CabGabService
    @Autowired
    lateinit var session: HttpSession

    // テスト用
    val TEST_USER_ID = "test-user-id"

    // 適性試験メイン画面
    @GetMapping("/questions")
    fun getQuestions(): String{
        return "questions/questions-main"
    }

    //　SPIメイン画面
    @GetMapping("/spi")
    fun getQuestionSpiMain(model: Model): String{
        val inProgressId = spiService.getInProgressExamId(TEST_USER_ID)

        if (inProgressId != null) {
            model.addAttribute("hasInProgress", true)
        } else {
            model.addAttribute("hasInProgress", false)
        }

        return "questions/spi-main"
    }



    // SPI問題画面
    @GetMapping("/spi/study")
    fun getQuestionSpiStudy(
        @RequestParam(name = "index") index: Int, // URLの ?index=〇〇 を受け取る
        model: Model
    ): String {
        // セッションチェック (不正アクセス防止)
        val examId = session.getAttribute("currentSpiExamId") as? String
        if (examId == null) {
            return "redirect:/spi" // セッション切れならトップへ
        }

        // 1. カテゴリ決定 (1-40:言語, 41-70:非言語)
        val targetCategory = if (index <= 40) "言語" else "非言語"

        // 2. 問題を取得
        val request = SpiRequest().apply {
            spiCategory = targetCategory
        }
        val response = spiService.getSpi(request)
        val questionList = response.spis

        if (questionList.isNullOrEmpty()) {
            return "redirect:/spi" // エラー時はメインへ戻す
        }

        // 3. 画面に必要なデータを渡す
        model.addAttribute("question", questionList[0])
        model.addAttribute("currentIndex", index) // 受け取った番号をそのまま渡す
        model.addAttribute("totalCount", 70)      // 全70問

        // 進捗率計算
        val progress = (index.toDouble() / 70 * 100).toInt()
        model.addAttribute("progress", progress)

        return "questions/spi-study"
    }

    // SPI結果画面
    @GetMapping("/spi/result")
    fun getQuestionSpiResult(): String{
        return "questions/spi-result"
    }

    @GetMapping("/spi/history")
    fun getSpiHistory(): String {
        return "questions/spi-history"
    }

    // CAB/GABメイン画面
    @GetMapping("/cabgab")
    fun getQuestionCabgabMain(): String{
        return "questions/cabgab-main"
    }

    // CAB/GAB問題画面
    @GetMapping("/cabgab/study")
    fun getQuestionCabgabStudy(
        @RequestParam(name = "index") index: Int, // URLの　index受け取り
        model: Model
    ): String{

        // TODO ここのカテゴリに関してcabgabの使用が違うと思うので確認
        // 1.カテゴリ決定 ( 1-40:言語, 41-70:非言語)
        val targetCategory = if(index <= 40) "言語" else "非言語"

        // 2.問題の取得
        val request = CabGabRequest().apply {
            cabGabCategory = targetCategory
        }
        val response = cabGabService.getCabGab(request)
        val questionList = response.cabGabs

        if (questionList.isNullOrEmpty()) {
            return "redirect:/cabgab" // エラー時メイン画面へ戻す
        }

        // 3.画面に必要なデータを渡す
        model.addAttribute("question", questionList[0])
        model.addAttribute("currentIndex", index)
        model.addAttribute("totalCount", 70)

        // 進捗率計算
        val progress = (index.toDouble() / 70 * 100).toInt()
        model.addAttribute("progress", progress)

        return "questions/cabgab-study"
    }

    // CAB/GAB結果画面
    @GetMapping("/cabgab/result")
    fun getQuestionCabgabResult(): String{
        return "questions/cabgab-result"
    }

    // 適性試験メイン画面
    @PostMapping("/questions")
    fun postQuestions(): String{
        return "questions/questions-main"
    }

    // SPIメイン画面
    @PostMapping("/spi")
    fun postQuestionSpiMain(): String{
        return "questions/spi-main"
    }

    @PostMapping("/spi/start")
    fun startSpiExam(
        @RequestParam mode: String,
        @RequestParam(required = false) middleIndex: Int?,
        model: Model
    ): String {
        var examId: String? = null
        var startIndex = 1

        when (mode) {
            //  新規最初から
            "new_start" -> {
                examId = spiService.startNewExam(TEST_USER_ID)
                startIndex = 1
            }

            // 新規途中から
            "new_middle" -> {
                examId = spiService.startNewExam(TEST_USER_ID)
                startIndex = middleIndex ?: 1
            }

            // 再開
            "resume" -> {
                examId = spiService.getInProgressExamId(TEST_USER_ID)
                if (examId != null) {
                    // 解いた問題数 + 1 からスタート
                    startIndex = spiService.getCurrentQuestionIndex(examId)
                } else {
                    return "redirect:/spi"
                }
            }
        }

        // セッションに試験IDを保存
        session.setAttribute("currentSpiExamId", examId)
        return "redirect:/spi/study?index=$startIndex"
    }

    // SPI問題画面
    @PostMapping("/spi/study")
    fun postQuestionSpiStudy(
        @RequestParam(name = "currentIndex") currentIndex: Int,
        @RequestParam(name = "spiId") spiId: String,
        @RequestParam(name = "answer", required = false) answer: Int?, // 中断時はnullの可能性があるので nullable に変更
        @RequestParam(name = "action") action: String, // 追加: どのボタンが押されたか
        model: Model
    ): String {

        // セッションから現在の試験IDを取得
        val examId = session.getAttribute("currentSpiExamId") as? String
            ?: return "redirect:/spi" // セッション切れ対策

        // 分岐処理
        when (action) {
            "suspend" -> {
                // 中断
                session.removeAttribute("currentSpiExamId")
                return "redirect:/spi"
            }

            "finish" -> {
                // 途中終了
                spiService.finishExam(examId)
                session.removeAttribute("currentSpiExamId")
                return "redirect:/spi/result"
            }

            "next" -> {
                // 次へ
                if (answer != null) {
                    spiService.saveOneAnswer(examId, spiId, answer)
                }

                // 次の問題へ
                val nextIndex = currentIndex + 1
                if (nextIndex > 70) {
                    // 70問終わったら自動的に終了処理
                    spiService.finishExam(examId)
                    session.removeAttribute("currentSpiExamId")
                    return "redirect:/spi/result"
                }

                return "redirect:/spi/study?index=$nextIndex"
            }
        }

        return "redirect:/spi"
    }

    // SPI結果画面
    @PostMapping("/spi/result")
    fun postQuestionSpiResult(): String{
        return "questions/spi-result"
    }

    @PostMapping("/spi/history")
    fun postSpiHistory(): String {
        return "questions/spi-history"
    }

    // CAB/GABメイン画面
    @PostMapping("/cabgab")
    fun postQuestionCabgabMain(): String{
        return "questions/cabgab-main"
    }

    // CAB/GAB問題画面
    @PostMapping("/cabgab/study")
    fun postQuestionCabgabStudy(): String{
        return "questions/cabgab-study"
    }

    //　CAB/GAB結果画面
    @PostMapping("/cabgab/result")
    fun postQuestionCabgabResult(): String{
        return "questions/cabgab-result"
    }

}