package jp.sabakan.mirai.controller

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
    fun getQuestionSpiStudy(
        @RequestParam(name = "index") index: Int, // URLの ?index=〇〇 を受け取る
        model: Model
    ): String {

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

    // SPI問題画面
    @PostMapping("/spi/study")
    fun postQuestionSpiStudy(
        @RequestParam(name = "currentIndex") currentIndex: Int, // HTMLの隠し項目から現在の番号を受け取る
        // @RequestParam(name = "answer") answer: String?, // ← 将来的に回答を受け取る場合はここに追加
        model: Model
    ): String {

        // 1. 次の問題番号を計算
        val nextIndex = currentIndex + 1

        // 2. 70問を超えていたら結果画面などへ移動（とりあえずトップに戻す例）
        if (nextIndex > 70) {
            return "redirect:/spi/result" // 結果画面ができたらそちらへ
        }

        // 3. 次の問題のカテゴリを決定
        val targetCategory = if (nextIndex <= 40) "言語" else "非言語"

        // 4. DBから問題を検索
        val request = SpiRequest().apply {
            spiCategory = targetCategory
        }
        val response = spiService.getSpi(request)

        // データ取得チェック
        val questionList = response.spis
        if (questionList.isNullOrEmpty()) {
            // エラー時は一旦トップなどに戻すか、エラー表示
            return "redirect:/spi"
        }

        // 5. 画面にデータを渡す
        model.addAttribute("question", questionList[0])
        model.addAttribute("currentIndex", nextIndex) // 次の番号を渡す
        model.addAttribute("totalCount", 70)

        // 進捗率
        val progress = (nextIndex.toDouble() / 70 * 100).toInt()
        model.addAttribute("progress", progress)

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