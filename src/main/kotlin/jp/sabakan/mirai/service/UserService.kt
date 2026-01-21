package jp.sabakan.mirai.service

import jp.sabakan.mirai.MessageConfig
import jp.sabakan.mirai.data.UserData
import jp.sabakan.mirai.entity.UserEntity
import jp.sabakan.mirai.repository.UserRepository
import jp.sabakan.mirai.request.UserRequest
import jp.sabakan.mirai.response.UserResponse
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Calendar
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