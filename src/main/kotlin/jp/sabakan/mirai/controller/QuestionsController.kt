package jp.sabakan.mirai.controller

import jp.sabakan.mirai.request.SpiRequest
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

    // 適性試験メイン画面
    @GetMapping("/questions")
    fun getQuestions(): String{
        return "questions/questions-main"
    }

    //　SPIメイン画面
    @GetMapping("/spi")
    fun getQuestionSpiMain(): String{
        return "questions/spi-main"
    }

    // SPI問題画面
    @GetMapping("/spi/study")
    fun getQuestionSpiStudy(@RequestParam(name = "index", defaultValue = "1") index: Int, model: Model): String {
        // 1. 問題番号(index)によってカテゴリを決定するロジック
        // 1~40問目: 言語, 41~70問目: 非言語
        val targetCategory = if (index <= 40) {
            "言語"  // ※DBに入っている実際の値に合わせてください（例: "Japanese", "1" 等の可能性もあり）
        } else {
            "非言語"
        }

        // 2. 決定したカテゴリで検索条件を作成
        val request = SpiRequest().apply {
            spiCategory = targetCategory
        }

        // 3. Serviceを使ってDBから問題をランダムに1問取得
        // (SpiServiceに getSpi メソッドがある前提です。なければ作成が必要です)
        val response = spiService.getSpi(request)

        val questionList = response.spis
        if (questionList.isNullOrEmpty()) {
            // エラーハンドリング（データがない場合など）
            model.addAttribute("errorMessage", "問題データが見つかりませんでした。")
            return "questions/spi-study"
        }

        // 4. 画面に必要なデータを渡す
        model.addAttribute("question", questionList[0]) // 取得した問題
        model.addAttribute("currentIndex", index)       // 現在の問題番号 (例: 1)
        model.addAttribute("totalCount", 70)            // 全問題数

        // 進捗率の計算 (例: (1 / 70) * 100)
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
    fun getQuestionCabgabStudy(): String{
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

    // SPI問題画面
    @PostMapping("/spi/study")
    fun postQuestionSpiStudy(): String{
        return "questions/spi-study"
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