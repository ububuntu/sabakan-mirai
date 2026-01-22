package jp.sabakan.mirai.controller

import jp.sabakan.mirai.MessageConfig
import jp.sabakan.mirai.request.GoalRequest
import jp.sabakan.mirai.request.UserRequest
import jp.sabakan.mirai.service.UserService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.ModelAttribute
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.servlet.mvc.support.RedirectAttributes

@Controller
class UserController {

    @Autowired
    lateinit var userService: UserService

    // ユーザーメイン画面
    @GetMapping("/user")
    fun getUser(model: Model): String {
        // ダミーユーザIDでユーザ情報と目標情報を取得
        val DUMMY_USER_ID = "test-user-id"
        val userRequest = UserRequest().apply {
            userId = DUMMY_USER_ID
        }
        val user = userService.getOneUserList(userRequest)
        val goalRequest = GoalRequest().apply {
            userId = DUMMY_USER_ID
        }

        // 目標情報を取得
        val goals = userService.getGoal(goalRequest)
        if (user != null) {
            model.addAttribute("userName", user.userName)
        }

        // 目標情報をモデルにセット
        if (goals.isNotEmpty()) {
            val currentGoal = goals[0]
            model.addAttribute("goalContent", currentGoal.goalContent)
            model.addAttribute("remainingDays", currentGoal.remainingDays)
        } else {
            model.addAttribute("goalContent", "目標は設定されていません")
            model.addAttribute("remainingDays", "-")
        }
        return "users/user-main"
    }

    // パスワード変更画面
    @GetMapping("/user/repassword")
    fun getRepassword(
        model: Model
    ): String {
        model.addAttribute("userRequest", UserRequest())
        return "users/user-repassword"
    }

    // ユーザー目標設定画面
    @GetMapping("/user/target")
    fun getTarget(
        model: Model
    ): String {
        model.addAttribute("goalRequest", GoalRequest())
        return "users/user-target"
    }

    // ユーザーメイン画面
    @PostMapping("/user")
    fun postUser(): String{
        return "users/user-main"
    }

    // パスワード変更画面
    @PostMapping("/user/repassword")
    fun postRepassword(
        @ModelAttribute userRequest: UserRequest,
        model: Model,
        redirectAttributes: RedirectAttributes
    ): String{
        // ダミーユーザIDをセット
        val DUMMY_USER_ID = "test-user-id"
        userRequest.userId = DUMMY_USER_ID
        val result = userService.updatePassword(userRequest)

        // 結果に応じてメッセージを設定しリダイレクト
        if (result) {
            redirectAttributes.addFlashAttribute("message", MessageConfig.PASSWORD_CHANGE_SUCCESS)
            return "redirect:/user"
        } else {
            model.addAttribute("message", MessageConfig.PASSWORD_CHANGE_FAILED)
            return "users/user-repassword"
        }
    }

    // ユーザー目標設定画面
    @PostMapping("/user/target")
    fun postTarget(
        @ModelAttribute goalRequest: GoalRequest
    ): String{
        // ダミーユーザIDをセット
        val DUMMY_USER_ID = "test-user-id"
        goalRequest.userId = DUMMY_USER_ID
        userService.saveGoal(goalRequest)
        return "redirect:/user/target"
    }
}