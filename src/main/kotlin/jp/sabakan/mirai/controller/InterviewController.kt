package jp.sabakan.mirai.controller

import jp.sabakan.mirai.request.InterviewRequest
import jp.sabakan.mirai.service.InterviewService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.ResponseBody
import java.security.Principal

@Controller
class InterviewController {

    @Autowired
    lateinit var interviewService: InterviewService

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

    // 面接メイン画面
    @PostMapping("/interview")
    fun postInterview(): String {
        return "/interview/interview-main"
    }

    // Web面接画面
    @PostMapping("/interview/do")
    fun postInterviewDo(interviewRequest: InterviewRequest, principal: Principal?): String {
        interviewRequest.userId = principal?.name ?: "test-user"
        interviewService.insertInterview(interviewRequest)
        return "/interview/interview-do"
    }

    // 面接ログ画面
    @PostMapping("/interview/log")
    fun postInterviewLog(): String {
        return "/interview/interview-log"
    }

    // 面接結果画面
    @PostMapping("/interview/result")
    fun postInterviewResult(): String {
        return "/interview/interview-result"
    }

    /**
     * 現在の質問を取得
     * GET /interview/api/current-question
     */
    @GetMapping("/interview/api/current-question")
    @ResponseBody
    fun getCurrentQuestion(): Map<String, Any> {
        val question = interviewService.getCurrentQuestion()
        val progress = interviewService.getProgress()
        val questionNumber = interviewService.getCurrentQuestionNumber()
        val totalQuestions = interviewService.getTotalQuestions()

        return mapOf(
            "question" to question,
            "progress" to progress,
            "questionNumber" to questionNumber,
            "totalQuestions" to totalQuestions
        )
    }

    /**
     * 次の質問に進む
     * POST /interview/api/next-question
     */
    @PostMapping("/interview/api/next-question")
    @ResponseBody
    fun getNextQuestion(): Map<String, Any?> {
        val nextQuestion = interviewService.getNextQuestion()
        val progress = interviewService.getProgress()
        val questionNumber = interviewService.getCurrentQuestionNumber()
        val totalQuestions = interviewService.getTotalQuestions()
        val hasNext = interviewService.hasNextQuestion()
        val isFinished = nextQuestion == null

        return mapOf(
            "question" to nextQuestion,
            "progress" to progress,
            "questionNumber" to questionNumber,
            "totalQuestions" to totalQuestions,
            "hasNext" to hasNext,
            "isFinished" to isFinished
        )
    }

    /**
     * 質問をリセット
     * POST /interview/api/reset-questions
     */
    @PostMapping("/interview/api/reset-questions")
    @ResponseBody
    fun resetQuestions(): Map<String, String> {
        interviewService.resetQuestions()
        return mapOf(
            "status" to "success",
            "message" to "質問をリセットしました"
        )
    }

    /**
     * 全質問を取得
     * GET /interview/api/all-questions
     */
    @GetMapping("/interview/api/all-questions")
    @ResponseBody
    fun getAllQuestions(): Map<String, Any> {
        val questions = interviewService.getAllQuestions()
        val total = interviewService.getTotalQuestions()

        return mapOf(
            "questions" to questions,
            "total" to total
        )
    }
}