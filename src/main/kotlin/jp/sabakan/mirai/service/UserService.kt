package jp.sabakan.mirai.service

import jp.sabakan.mirai.MessageConfig
import jp.sabakan.mirai.data.GoalData
import jp.sabakan.mirai.data.UserData
import jp.sabakan.mirai.entity.GoalEntity
import jp.sabakan.mirai.entity.UserEntity
import jp.sabakan.mirai.repository.UserRepository
import jp.sabakan.mirai.request.GoalRequest
import jp.sabakan.mirai.request.UserRequest
import jp.sabakan.mirai.response.UserResponse
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDate
import java.time.temporal.ChronoUnit
import java.util.UUID

@Service
class UserService {
    @Autowired
    lateinit var userRepository: UserRepository

    /**
     * ユーザ一覧を取得する
     *
     * @return ユーザのリスト
     */
    fun getUserList(): List<UserEntity> {
        val table = userRepository.getUserList()
        return tableToListEntity(table)
    }

    /**
     * ユーザ情報を1件取得する
     *
     * @param userId ユーザID
     * @return ユーザ情報エンティティ
     */
    fun getOneUserList(request: UserRequest): UserEntity? {
        // ユーザ情報取得
        val data = UserData().apply {
            this.userId = request.userId
        }
        val map = userRepository.getOneUserList(data) ?: return null

        // エンティティに変換して返す
        val entityList = tableToListEntity(listOf(map))
        return entityList.firstOrNull()
    }

    /**
     * ユーザ一覧を名前で検索する
     *
     * @param request ユーザ情報リクエスト
     * @return ユーザのリスト
     */
    fun getUserListByName(request: UserRequest): List<UserEntity> {
        val table: List<Map<String, Any?>>

        if (request.keyword.isNullOrBlank()) {
            // 空白またはnullの場合は全件取得
            table = userRepository.getUserList()
        } else {
            // 入力がある場合は部分一致検索 (%で囲む)
            val data = UserData().apply {
                this.keyword = "%"+request.keyword+"%"
            }
            table = userRepository.getUserListByName(data)
        }

        return tableToListEntity(table)
    }

    /**
     * ユーザ情報を登録する
     *
     * @param request ユーザ情報リクエスト
     * @return ユーザ情報レスポンス
     */
    fun insertUser(request: UserRequest): UserResponse {
        val response = UserResponse()

        // ユーザIDを生成
        val userId = toCreateUserId()

        // ユーザ情報登録
        val data = UserData().apply {
            this.userId = userId
            this.userName = request.userName
            this.userAddress = request.userAddress
            this.password = request.password
            this.userRole = request.userRole
            this.isValid = request.isValid
        }

        try {
            userRepository.insertUser(data)
            response.message = MessageConfig.USER_REGISTERED
        } catch (e: DataIntegrityViolationException) {
            response.message = MessageConfig.USER_REGISTER_FAILED
        }

        return response
    }

    /**
     * ユーザ情報を更新する
     *
     * @param request ユーザ情報リクエスト
     * @return ユーザ情報レスポンス
     */
    fun updateUser(request: UserRequest): Boolean {
        // ユーザIDがnullの場合は処理を中断
        val userId = request.userId ?: return false

        // 現在のパスワードを取得
        val currentPassword = duplicatePassword(userId) ?: return false

        val response = UserResponse()

        // ユーザ情報更新
        val data = UserData().apply {
            this.userId = request.userId
            this.userName = request.userName
            this.userAddress = request.userAddress
            this.userRole = request.userRole
            this.isValid = request.isValid

            this.password = if (request.password.isNullOrEmpty()) {
                currentPassword
            } else {
                request.password
                //passwordEncoder.encode(request.password)
            }
        }

        // ユーザ情報更新処理を実行
        try {
            userRepository.updateUser(data)
            response.message = MessageConfig.USER_UPDATE_SUCCESS
        } catch (e: DataIntegrityViolationException) {
            response.message = MessageConfig.USER_UPDATE_FAILED
        }

        // 3. 更新実行
        val count = userRepository.updateUser(data)
        return count > 0
    }

    /**
     * パスワードを更新する
     *
     * @param request ユーザ情報リクエスト
     * @return 更新成功:true, 失敗:false
     */
    fun updatePassword(request: UserRequest): Boolean {
        val response = UserResponse()
        val newPassword = request.password ?: return false

        // ユーザ情報取得
        val data = UserData().apply {
            this.userId = request.userId
        }
        val map = userRepository.getOneUserList(data) ?: return false

        // ユーザ情報更新
        val inputdata = UserData().apply {
            this.userId =  request.userId
            this.userName = map["user_name"] as String?
            this.userAddress = map["user_address"] as String?
            this.password = newPassword
            this.userRole = map["user_role"] as String?
            this.isValid = map["user_valid"] as Boolean?

        }

        // ユーザ情報更新処理を実行
        try {
            userRepository.updateUser(data)
            response.message = MessageConfig.USER_UPDATE_SUCCESS
        } catch (e: DataIntegrityViolationException) {
            response.message = MessageConfig.USER_UPDATE_FAILED
        }

        // 3. 更新実行
        val count = userRepository.updateUser(data)
        return count > 0
    }

    /**
     * パスワードの重複チェックを行う
     *
     * @param userId ユーザID
     * @return パスワード
     */
    private fun duplicatePassword(userId: String): String? {
        val request = UserRequest().apply {
            this.userId = userId
        }
        val userEntity = getOneUserList(request)
        return userEntity?.password
    }

    /**
     * ユーザIDを生成する
     *
     * @return ユーザID
     */
    private fun toCreateUserId(): String{
        // UUIDを生成
        val uuid = UUID.randomUUID().toString()

        // 新しいユーザIDを返す
        return "U$uuid"
    }

    /**
     * 目標情報を取得する
     *
     * @param request 目標情報リクエスト
     * @return 目標情報エンティティリスト
     */
    fun getGoal(request: GoalRequest): List<GoalEntity> {
        val data = GoalData().apply {
            this.userId = request.userId
        }
        // repositoryはListを返す
        val tableList = userRepository.getGoal(data)

        if (tableList.isEmpty()) {
            return emptyList()
        }

        // 1件目を取得すると仮定（複数ある場合はループ処理が必要）
        val row = tableList[0]

        val entity = GoalEntity().apply {
            this.goalId = row["goal_id"] as String?
            this.userId = row["user_id"] as String?
            this.goalContent = row["goal_content"] as String?
            this.goalDate = row["goal_date"] as java.util.Date?
        }

        if (entity.goalDate != null) {
            val today = LocalDate.now()
            val target = java.sql.Date(entity.goalDate!!.time).toLocalDate()
            val diff = ChronoUnit.DAYS.between(today, target)
            entity.remainingDays = if (diff < 0) 0 else diff
        }

        return listOf(entity)
    }

    /**
     * 目標情報を保存する（新規登録または更新）
     *
     * @param request 目標情報リクエスト
     */
    fun saveGoal(request: GoalRequest){
        val searchData = GoalData().apply {
            this.userId = request.userId
        }
        // DBから検索
        val currentGoalList = userRepository.getGoal(searchData)

        if (currentGoalList.isEmpty()) {
            // 新規登録
            val newId = "G" + UUID.randomUUID().toString()
            val newData = GoalData().apply {
                this.goalId = newId
                this.userId = request.userId
                this.goalContent = request.goalContent
                this.goalDate = request.goalDate
            }
            userRepository.insertGoal(newData)
        } else {
            // 更新 (リストの最初の要素のIDを使う)
            val existingGoalId = currentGoalList[0]["goal_id"] as String
            val updateData = GoalData().apply {
                this.goalId = existingGoalId
                this.goalContent = request.goalContent
                this.goalDate = request.goalDate
            }
            userRepository.updateGoal(updateData)
        }
    }

    @Transactional(rollbackFor = [Exception::class])
    fun deleteUser(request: UserRequest): Boolean {
        val userId = request.userId ?: return false

        try{
            userRepository.deleteUser(userId)
            return true
        } catch (e: DataIntegrityViolationException) {
            throw e
        }
    }

    /**
     * テーブルデータをエンティティリストに変換する
     *
     * @param table テーブルデータ
     * @return エンティティリスト
     */
    private fun tableToListEntity(table: List<Map<String, Any?>>): List<UserEntity> {
        val list: MutableList<UserEntity> = mutableListOf()
        for (row in table) {
            val entity = UserEntity()
            entity.userId = row["user_id"] as String?
            entity.userName = row["user_name"] as String?
            entity.userAddress = row["user_address"] as String?
            entity.password = row["password"] as String?
            entity.userRole = row["user_role"] as String?
            entity.isValid = row["user_valid"] as Boolean?
            entity.createdAt = row["created_at"] as java.util.Date?
            entity.lastedAt = row["lasted_at"] as java.util.Date?
            list.add(entity)
        }
        return list
    }
}