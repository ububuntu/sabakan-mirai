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
        @RequestParam(required = false) mode: String?, // "new" or "resume"
        model: Model
    ): String {
        var examId: String? = null
        var startIndex = 1

        if (mode == "resume") {
            // 続きから：既存のIDを探す
            examId = spiService.getInProgressExamId(TEST_USER_ID)
            if (examId != null) {
                // 解いた問題数 + 1 からスタート
                startIndex = spiService.getCurrentQuestionIndex(examId)
            }
        }

        // 新規の場合、または続きが見つからなかった場合
        if (examId == null) {
            examId = spiService.startNewExam(TEST_USER_ID)
            startIndex = 1
        }

        // セッションに試験IDを保存 (これでページ遷移してもIDを忘れない)
        session.setAttribute("currentSpiExamId", examId)

        return "redirect:/spi/study?index=$startIndex"
    }

    // SPI問題画面
    @PostMapping("/spi/study")
    fun postQuestionSpiStudy(
        @RequestParam(name = "currentIndex") currentIndex: Int,
        @RequestParam(name = "spiId") spiId: String,
        @RequestParam(name = "answer") answer: Int,
        model: Model
    ): String {// セッションから試験ID取得
        val examId = session.getAttribute("currentSpiExamId") as? String
        if (examId == null) {
            return "redirect:/spi"
        }

        // 1. 次の問題番号を計算
        spiService.saveOneAnswer(examId, spiId, answer)// 2. 次の問題番号へ
        val nextIndex = currentIndex + 1

        // 3. 終了判定 (70問超えたら終了)
        if (nextIndex > 70) {
            // 集計＆完了処理
            spiService.finishExam(examId)

            // セッション情報をクリア
            session.removeAttribute("currentSpiExamId")

            return "redirect:/spi/result"
        }

        // 次の問題へリダイレクト
        return "redirect:/spi/study?index=$nextIndex"
    }

    // SPI結果画面
    @PostMapping("/spi/result")
    fun postQuestionSpiResult(): String{
        return "questions/spi-result"
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