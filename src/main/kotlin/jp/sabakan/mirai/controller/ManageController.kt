package jp.sabakan.mirai.controller

import jp.sabakan.mirai.MessageConfig
import jp.sabakan.mirai.request.CabGabRequest
import jp.sabakan.mirai.request.SpiRequest
import jp.sabakan.mirai.request.UserRequest
import jp.sabakan.mirai.service.CabGabService
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

    @Autowired
    lateinit var cabGabService: CabGabService

    // 管理メイン画面
    @GetMapping("/manage")
    fun getManage(
        model: Model
    ): String {
        model.addAttribute("userCount", userService.getUserCount())
        model.addAttribute("spiCount", spiService.getSpiCount())
        model.addAttribute("cabgabCount", cabGabService.getCabGabCount())
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

    // --- 一覧画面表示 ---
    @GetMapping("/manage/spi")
    fun getManageSpiMain(
        @RequestParam(name = "status", defaultValue = "all") status: String,
        model: Model
    ): String {
        // 全件取得
        val allList = spiService.getAllSpi()

        val filteredList = when (status) {
            "active" -> allList.filter { it.spiCategory == "言語" }
            "stop"   -> allList.filter { it.spiCategory == "非言語" }
            else     -> allList
        }

        model.addAttribute("spiList", filteredList)
        model.addAttribute("selectedStatus", status)

        return "manage/spi/manage-spi-main"
    }

    // --- 検索・フィルタ用POST（GETへ流すだけでもOK） ---
    @PostMapping("/manage/spi")
    fun postManageSpi(
        @RequestParam(name = "status", defaultValue = "all") status: String
    ): String {
        // 検索ボタン押下時などは、statusパラメータを付けてGETメソッドへリダイレクトするのが一般的です
        return "redirect:/manage/spi?status=$status"
    }

    // --- 追加画面表示 ---
    @GetMapping("/manage/spi/add")
    fun getManageSpiAdd(model: Model): String {
        // 空のフォームオブジェクトを渡す
        model.addAttribute("spiRequest", SpiRequest())
        return "manage/spi/manage-spi-add"
    }

    // --- 追加処理 ---
    @PostMapping("/manage/spi/add")
    fun postManageSpiAdd(
        @ModelAttribute spiRequest: SpiRequest,
        redirectAttributes: RedirectAttributes
    ): String {
        // Serviceの追加処理呼び出し
        val response = spiService.insertSpi(spiRequest)

        // 完了メッセージをセットして一覧へリダイレクト
        redirectAttributes.addFlashAttribute("message", response.message)
        return "redirect:/manage/spi"
    }

    // --- 編集画面表示 ---
    @GetMapping("/manage/spi/edit")
    fun getManageSpiEdit(
        @RequestParam("spiId") spiId: String,
        model: Model
    ): String {
        // Serviceから1件取得
        val request = spiService.getSpiById(spiId)

        // 存在しないID等の場合は一覧へ戻す（簡易ハンドリング）
        if (request == null) {
            return "redirect:/manage/spi"
        }

        model.addAttribute("spiRequest", request)
        return "manage/spi/manage-spi-edit"
    }

    // --- 更新処理 ---
    @PostMapping("/manage/spi/edit")
    fun postManageSpiEdit(
        @ModelAttribute spiRequest: SpiRequest,
        redirectAttributes: RedirectAttributes
    ): String {
        // Serviceの更新処理呼び出し
        val response = spiService.updateSpi(spiRequest)

        // 完了メッセージをセットして一覧へリダイレクト
        redirectAttributes.addFlashAttribute("message", response.message)
        return "redirect:/manage/spi"
    }

    // --- 削除処理 ---
    @PostMapping("/manage/spi/delete")
    fun postManageSpiDelete(
        @RequestParam("spiId") spiId: String,
        redirectAttributes: RedirectAttributes
    ): String {
        // Serviceの削除処理呼び出し
        val response = spiService.deleteSpi(spiId)

        // 完了メッセージをセットして一覧へリダイレクト
        redirectAttributes.addFlashAttribute("message", response.message)
        return "redirect:/manage/spi"
    }

    // --- 一覧画面表示 ---
    @GetMapping("/manage/cabgab")
    fun getManageCabgab(
        @RequestParam(name = "status", defaultValue = "all") status: String,
        model: Model
    ): String {
        // 全件取得
        val allList = cabGabService.getAllCabGab()

        // SPIのフィルタリングロジックを参考に、必要に応じてカテゴリ分け
        val filteredList = when (status) {
            "cab"  -> allList.filter { it.cabgabCategory == "CAB" }
            "gab"  -> allList.filter { it.cabgabCategory == "GAB" }
            else   -> allList
        }

        model.addAttribute("cabgabList", filteredList)
        model.addAttribute("selectedStatus", status)

        return "manage/cabgab/manage-cabgab-main"
    }

    // --- 検索・フィルタ用POST ---
    @PostMapping("/manage/cabgab")
    fun postManageCabgab(
        @RequestParam(name = "status", defaultValue = "all") status: String
    ): String {
        return "redirect:/manage/cabgab?status=$status"
    }

    // --- 追加画面表示 ---
    @GetMapping("/manage/cabgab/add")
    fun getManageCabgabAdd(model: Model): String {
        // 空のRequestオブジェクトを渡す（クラス名はプロジェクトの定義に合わせてください）
        model.addAttribute("cabgabRequest", CabGabRequest())
        return "manage/cabgab/manage-cabgab-add"
    }

    // --- 追加処理 ---
    @PostMapping("/manage/cabgab/add")
    fun postManageCabgabAdd(
        @ModelAttribute cabgabRequest: CabGabRequest,
        redirectAttributes: RedirectAttributes
    ): String {
        // Serviceの追加処理呼び出し
        val response = cabGabService.insertCabGab(cabgabRequest)

        // 完了メッセージをセットして一覧へリダイレクト
        redirectAttributes.addFlashAttribute("message", response.message)
        return "redirect:/manage/cabgab"
    }

    // --- 編集画面表示 ---
    @GetMapping("/manage/cabgab/edit")
    fun getManageCabgabEdit(
        @RequestParam("cabgabId") cabgabId: String,
        model: Model
    ): String {
        // Serviceから1件取得
        val request = cabGabService.getCabGabById(cabgabId)

        if (request == null) {
            return "redirect:/manage/cabgab"
        }

        model.addAttribute("cabgabRequest", request)
        return "manage/cabgab/manage-cabgab-edit"
    }

    // --- 更新処理 ---
    @PostMapping("/manage/cabgab/edit")
    fun postManageCabgabEdit(
        @ModelAttribute cabgabRequest: CabGabRequest,
        redirectAttributes: RedirectAttributes
    ): String {
        // Serviceの更新処理呼び出し
        val response = cabGabService.updateCabGab(cabgabRequest)

        redirectAttributes.addFlashAttribute("message", response.message)
        return "redirect:/manage/cabgab"
    }

    // --- 削除処理 ---
    @PostMapping("/manage/cabgab/delete")
    fun postManageCabgabDelete(
        @RequestParam("cabgabId") cabgabId: String,
        redirectAttributes: RedirectAttributes
    ): String {
        // Serviceの削除処理呼び出し
        val response = cabGabService.deleteCabGab(cabgabId)

        redirectAttributes.addFlashAttribute("message", response.message)
        return "redirect:/manage/cabgab"
    }

    @GetMapping("/fish")
    fun getfishing(): String {
        return "/sea"
    }
}