package jp.sabakan.mirai.controller

import jp.sabakan.mirai.MessageConfig
import jp.sabakan.mirai.data.UserData
import jp.sabakan.mirai.request.UserRequest
import jp.sabakan.mirai.service.UserService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.validation.BindingResult
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.ModelAttribute
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.servlet.mvc.support.RedirectAttributes

@Controller
class ManageController {

    @Autowired
    lateinit var userService: UserService

    // 管理メイン画面
    @GetMapping("/manage")
    fun getManage(): String {
        return "/manage/manage-main"
    }

    // ユーザー管理画面
    @GetMapping("/manage/users")
    fun getManageUsers(model: Model): String {
        // ユーザ一覧を取得してモデルにセット
        val users = userService.getUserList()
        model.addAttribute("users", users)
        return "/manage/users/manage-users-main"
    }

    // ユーザー情報変更画面
    @GetMapping("/manage/users/edit/{user}")
    fun getManageUsersEdit(
        @PathVariable("user") userId: String,
        model: Model
    ): String {
        val request = UserRequest().apply {
            this.userId = userId
        }
        // IDからユーザ情報を取得
        val user = userService.getOneUserList(request)

        if (user == null) {
            // ユーザが見つからない場合は一覧へ戻すなどのハンドリング
            return "redirect:/manage/users"
        }

        // 画面の th:object="${user}" にセット
        model.addAttribute("user", user)
        return "/manage/users/manage-users-edit"
    }

    // ユーザー追加画面
    @GetMapping("/manage/users/add")
    fun getManageUsersAdd(model: Model): String {
        // 空のUserRequestをフォームにセット
        model.addAttribute("userRequest", UserRequest())
        return "/manage/users/manage-users-add"
    }

    // SPIメイン画面
    @GetMapping("/manage/spi")
    fun getManageSpi(): String {
        return "/manage/spi/manage-spi-main"
    }

    // SPI変更画面
    @GetMapping("/manage/spi/edit")
    fun getManageSpiEdit(): String {
        return "/manage/spi/manage-spi-edit"
    }

    // SPI追加画面
    @GetMapping("/manage/spi/add")
    fun getManageSpiAdd(): String {
        return "/manage/spi/manage-spi-add"
    }

    // CAB/GABメイン画面
    @GetMapping("/manage/cabgab")
    fun getManageCabgab(): String {
        return "/manage/cabgab/manage-cabgab-main"
    }

    // CAB/GAB変更画面
    @GetMapping("/manage/cabgab/edit")
    fun getManageCabgabEdit(): String {
        return "/manage/cabgab/manage-cabgab-edit"
    }

    // CAB/GAB追加画面
    @GetMapping("/manage/cabgab/add")
    fun getManageCabgabAdd(): String {
        return "/manage/cabgab/manage-cabgab-add"
    }

    // ログ管理画面
    @GetMapping("/manage/logs")
    fun getManageLogs(): String {
        return "/manage/manage-logs"
    }



    // 管理メイン画面
    @PostMapping("/manage")
    fun postManage(): String {
        return "/manage/manage-main"
    }

    // ユーザー管理画面
    @PostMapping("/manage/users")
    fun postManageUsers(): String {
        return "/manage/users/manage-users-main"
    }

    // ユーザー情報変更画面
    @PostMapping("/manage/users/edit")
    fun postManageUsersEdit(
        @ModelAttribute("user") request: UserRequest,
        bindingResult: BindingResult,
        model: Model,
        redirectAttributes: RedirectAttributes
    ): String {
        // フォームのバリデーションチェック
        if (bindingResult.hasErrors()) {
            return "/manage/users/manage-users-edit"
        }

        // バリデーションチェック
        if (!request.password.isNullOrEmpty() && request.password!!.length < 8) {
            model.addAttribute("message", MessageConfig.EMAIL_NOT_BLANK_ERROR)
            return "/manage/users/manage-users-edit"
        }
        // 更新処理を実行
        val isSuccess = userService.updateUser(request)

        if (isSuccess) {
            redirectAttributes.addFlashAttribute("message", MessageConfig.USER_UPDATE_SUCCESS)
            return "redirect:/manage/users"
        } else {
            redirectAttributes.addFlashAttribute("message", MessageConfig.USER_UPDATE_FAILED)
            return "/manage/users/manage-users-edit"
        }
    }

    // ユーザー追加画面
    @PostMapping("/manage/users/add")
    fun postManageUsersAdd(
        @ModelAttribute request: UserRequest,
        bindingResult: BindingResult,
        model: Model,
        redirectAttributes: RedirectAttributes
    ): String {
        // フォームのバリデーションチェック
        if (bindingResult.hasErrors()) {
            return "/manage/users/manage-users-add"
        }

        // バリデーションチェック
        if (!request.password.isNullOrEmpty() && request.password!!.length < 8) {
            model.addAttribute("message", MessageConfig.EMAIL_NOT_BLANK_ERROR)
            return "/manage/users/manage-users-edit"
        }

        try{
            // ユーザ登録処理を呼び出す
            userService.insertUser(request)

            // 登録成功メッセージをセットしてリダイレクト
            redirectAttributes.addFlashAttribute("message", MessageConfig.USER_REGISTERED)
            return "redirect:/manage/users"
        } catch (e: Exception){
            // 登録失敗メッセージをセットして元の画面に戻る
            model.addAttribute("message", MessageConfig.USER_REGISTER_FAILED)
            return "/manage/users/manage-users-add"
        }
    }

    // SPIメイン画面
    @PostMapping("/manage/spi")
    fun postManageSpi(): String {
        return "/manage/spi/manage-spi-main"
    }

    // SPI変更画面
    @PostMapping("/manage/spi/edit")
    fun postManageSpiEdit(): String {
        return "/manage/spi/manage-spi-edit"
    }

    // SPI追加画面
    @PostMapping("/manage/spi/add")
    fun postManageSpiAdd(): String {
        return "/manage/spi/manage-spi-add"
    }

    // CAB/GABメイン画面
    @PostMapping("/manage/cabgab")
    fun postManageCabgab(): String {
        return "/manage/cabgab/manage-cabgab-main"
    }

    // CAB/GAB変更画面
    @PostMapping("/manage/cabgab/edit")
    fun postManageCabgabEdit(): String {
        return "/manage/cabgab/manage-cabgab-edit"
    }

    // CAB/GAB追加画面
    @PostMapping("/manage/cabgab/add")
    fun postManageCabgabAdd(): String {
        return "/manage/cabgab/manage-cabgab-add"
    }

    // ログ管理画面
    @PostMapping("/manage/logs")
    fun postManageLogs(): String {
        return "/manage/manage-logs"
    }
}