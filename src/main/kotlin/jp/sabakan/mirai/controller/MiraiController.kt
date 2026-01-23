package jp.sabakan.mirai.controller

import jp.sabakan.mirai.request.GoalRequest
import jp.sabakan.mirai.request.UserRequest
import jp.sabakan.mirai.service.UserService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping

@Controller
class MiraiController {

    @Autowired
    lateinit var userService: UserService

    // ホーム画面
    @GetMapping("/index")
    fun getIndex(model: Model): String {
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
        return "/index"
    }

    // ホーム画面
    @PostMapping("/index")
    fun postIndex(): String {
        return "/index"
    }
}