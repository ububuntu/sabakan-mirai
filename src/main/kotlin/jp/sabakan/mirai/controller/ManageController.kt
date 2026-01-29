package jp.sabakan.mirai.controller

import jp.sabakan.mirai.MessageConfig
import jp.sabakan.mirai.data.UserData
import jp.sabakan.mirai.request.SpiRequest
import jp.sabakan.mirai.request.UserRequest
import jp.sabakan.mirai.response.SpiResponse
import jp.sabakan.mirai.service.SpiService
import jp.sabakan.mirai.service.UserService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.validation.BindingResult
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.ModelAttribute
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.servlet.mvc.support.RedirectAttributes

@Controller
class ManageController {

    @Autowired
    lateinit var userService: UserService

    @Autowired
    lateinit var spiService: SpiService

    //todo 管理

    // 管理メイン画面
    @GetMapping("/manage")
    fun getManage(): String {
        return "/manage/manage-main"
    }
    // 管理メイン画面
    @PostMapping("/manage")
    fun postManage(): String {
        return "/manage/manage-main"
    }
    // ログ管理画面
    @GetMapping("/manage/logs")
    fun getManageLogs(): String {
        return "/manage/manage-logs"
    }
    // ログ管理画面
    @PostMapping("/manage/logs")
    fun postManageLogs(): String {
        return "/manage/manage-logs"
    }

    //todo ユーザ

    // ユーザー管理画面
    @GetMapping("/manage/users")
    fun getManageUsers(
        @RequestParam(name = "keyword", required = false) keyword: String?,
        model: Model
    ): String {
        // ユーザ一覧を取得してモデルにセット
        val request = UserRequest().apply {
            this.keyword = keyword
        }
        val users = userService.getUserListByName(request)
        model.addAttribute("users", users)
        model.addAttribute("keyword", keyword)
        return "/manage/users/manage-users-main"
    }
    // ユーザー管理画面
    @PostMapping("/manage/users")
    fun postManageUsers(): String {
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
        val response = userService.updatePassword(request)

        // メッセージの内容で成功・失敗を判定
        if (response.message == MessageConfig.PASSWORD_CHANGE_SUCCESS) {
            // 成功時：一覧画面へリダイレクト
            redirectAttributes.addFlashAttribute("message", response.message)
            return "redirect:/manage/user"
        } else {
            // 失敗時：編集画面に戻りエラーメッセージを表示
            model.addAttribute("message", response.message)
            return "/manage/user/manage-user-edit"
        }
    }
    // ユーザー追加画面
    @GetMapping("/manage/users/add")
    fun getManageUsersAdd(model: Model): String {
        // 空のUserRequestをフォームにセット
        model.addAttribute("userRequest", UserRequest())
        return "/manage/users/manage-users-add"
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
            val response = userService.insertUser(request)

            // 登録成功メッセージをセットしてリダイレクト
            redirectAttributes.addFlashAttribute("message", response.message)
            return "redirect:/manage/users"
        } catch (e: Exception){
            // 登録失敗メッセージをセットして元の画面に戻る
            model.addAttribute("message", MessageConfig.USER_REGISTER_FAILED)
            return "/manage/users/manage-users-add"
        }

    }

    //todo SPI

    // SPIメイン画面
//    @GetMapping("/manage/spi")
//    fun getManageSpiMain(
//        @RequestParam(name = "status", defaultValue = "all") status: String,
//        model: Model
//    ): String {
//        // ステータスに応じてフィルタリング
//        val allList = spiService.getAllSpi()
//        val filteredList = when (status) {
//            "active" -> allList.filter { it.spiCategory == "言語" }
//            "stop"   -> allList.filter { it.spiCategory == "非言語" }
//            else     -> allList // "all" または想定外の値なら全件表示
//        }
//        // フィルタリング結果をモデルにセット
//        model.addAttribute("spiList", filteredList)
//        model.addAttribute("selectedStatus", status)
//
//        return "manage/spi/manage-spi-main"
//    }
//    // SPIメイン画面
//    @PostMapping("/manage/spi")
//    fun postManageSpi(
//
//    ): String {
//        return "/manage/spi/manage-spi-main"
//    }
//    // SPI変更画面
//    @GetMapping("/manage/spi/edit")
//    fun getManageSpiEdit(
//        @RequestParam("spiId") spiId: String,
//        model: Model
//    ): String {
//        val request = spiService.getSpiById(spiId)
//
//        model.addAttribute("spiRequest", request)
//        return "manage/spi/manage-spi-edit"
//    }
//    // SPI変更画面
//    @PostMapping("/manage/spi/edit")
//    fun postManageSpiEdit(
//        @ModelAttribute spiRequest: SpiRequest,
//        redirectAttributes: RedirectAttributes
//    ): String {
//        // SPI更新処理を呼び出す
//        val response = spiService.updateSpi(spiRequest)
//
//        // メッセージをFlashScopeに入れてリダイレクト
//        redirectAttributes.addFlashAttribute("message", response.message)
//        return "redirect:/manage/spi"
//    }
//    // SPI追加画面
//    @GetMapping("/manage/spi/add")
//    fun getManageSpiAdd(model: Model): String {
//        // 空のフォームオブジェクトを渡す
//        model.addAttribute("spiRequest", SpiRequest())
//        return "manage/spi/manage-spi-add"
//    }
//    // SPI追加画面
//    @PostMapping("/manage/spi/add")
//    fun postManageSpiAdd(
//        @ModelAttribute spiRequest: SpiRequest,
//        redirectAttributes: RedirectAttributes
//    ): String {
//        // SPI追加処理を呼び出す
//        val response = spiService.insertSpi(spiRequest)
//
//        // メッセージをFlashScopeに入れてリダイレクト
//        redirectAttributes.addFlashAttribute("message", response.message)
//        return "redirect:/manage/spi"
//    }
//    // SPI削除画面
//    @PostMapping("/manage/spi/delete")
//    fun postManageSpiDelete(
//        @RequestParam("spiId") spiId: String,
//        redirectAttributes: RedirectAttributes
//    ): String {
//        // SPI削除処理を呼び出す
//        val response = spiService.deleteSpi(spiId)
//
//        // メッセージをFlashScopeに入れてリダイレクト
//        redirectAttributes.addFlashAttribute("message", response.message)
//        return "redirect:/manage/spi"
//    }

    //todo CABGAB

    // CAB/GABメイン画面
    @GetMapping("/manage/cabgab")
    fun getManageCabgab(): String {
        return "/manage/cabgab/manage-cabgab-main"
    }
    // CAB/GABメイン画面
    @PostMapping("/manage/cabgab")
    fun postManageCabgab(): String {
        return "/manage/cabgab/manage-cabgab-main"
    }
    // CAB/GAB変更画面
    @GetMapping("/manage/cabgab/edit")
    fun getManageCabgabEdit(): String {
        return "/manage/cabgab/manage-cabgab-edit"
    }
    // CAB/GAB変更画面
    @PostMapping("/manage/cabgab/edit")
    fun postManageCabgabEdit(): String {
        return "/manage/cabgab/manage-cabgab-edit"
    }
    // CAB/GAB追加画面
    @GetMapping("/manage/cabgab/add")
    fun getManageCabgabAdd(): String {
        return "/manage/cabgab/manage-cabgab-add"
    }
    // CAB/GAB追加画面
    @PostMapping("/manage/cabgab/add")
    fun postManageCabgabAdd(): String {
        return "/manage/cabgab/manage-cabgab-add"
    }
}