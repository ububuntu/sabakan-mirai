package jp.sabakan.mirai.controller

import jp.sabakan.mirai.request.GoalRequest
import jp.sabakan.mirai.request.UserRequest
import jp.sabakan.mirai.security.LoginUserDetails
import jp.sabakan.mirai.service.UserService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping

@Controller
class MiraiController {

    @Autowired
    lateinit var userService: UserService

    /**
     * ホーム画面
     */
    @GetMapping("/index")
    fun getIndex(
        @AuthenticationPrincipal userDetails: LoginUserDetails,
        session: jakarta.servlet.http.HttpSession,
        model: Model
    ): String {
        val msg = session.getAttribute("message")
        if (msg != null) {
            model.addAttribute("message", msg)
            session.removeAttribute("message")
        }

        // ユーザーIDを取得
        val userId = userDetails.getUserEntity().userId
        val userRequest = UserRequest().apply {
            this.userId = userId
        }
        val user = userService.getOneUserList(userRequest)
        val goalRequest = GoalRequest().apply {
            this.userId = userId
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
        return "index"
    }

    /**
     * ホーム画面（POST）
     */
    @PostMapping("/index")
    fun postIndex(): String {
        return "index"
    }
}