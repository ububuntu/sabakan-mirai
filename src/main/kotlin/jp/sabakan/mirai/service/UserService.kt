package jp.sabakan.mirai.service

import jp.sabakan.mirai.MessageConfig
import jp.sabakan.mirai.data.UserData
import jp.sabakan.mirai.entity.UserEntity
import jp.sabakan.mirai.repository.UserRepository
import jp.sabakan.mirai.request.UserRequest
import jp.sabakan.mirai.response.UserResponse
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.stereotype.Service
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.UUID

@Service
class UserService {
    @Autowired
    lateinit var userRepository: UserRepository

    /**
     * 指定ユーザの情報を取得する
     *
     * @param data ユーザデータ
     * @return ユーザレスポンス
     * @throws UserNameNotFoundException ユーザが見つからない場合
     */
    fun getUserDetail(data: UserData): UserResponse {
        // リポジトリへ問い合わせ
        val table: List<Map<String, Any?>> = userRepository.getUserDetail(data)
        val list: List<UserEntity> = tableToListEntity(table)

        // 結果を返す
        val user = list.singleOrNull()
            //?: throw UserNameNotFoundException(MessageConfig.USER_NOT_FOUND)
            ?: throw Exception(MessageConfig.USER_NOT_FOUND)

        return UserResponse().apply {
            users = listOf(user)
            message = null
            userRole = user.userRole
        }
    }

    /**
     * ユーザ登録処理
     *
     * @param request ユーザ登録リクエスト
     * @return ユーザ登録レスポンス
     */
    fun insertUser(request: UserRequest): UserResponse {
        var lastException: Exception? = null

        // ID重複対応のためリトライ処理
        val maxRetry = 5
        repeat(maxRetry) { attempt ->
            try {
                // リクエストからデータ変換
                val data = UserData()
                data.userId = toCreateUserId()
                data.userName = request.userName
                data.userAddress = request.userAddress
                data.password = request.password
                data.userRole = request.userRole
                data.isValid = request.isValid

                // リポジトリへ登録処理依頼
                userRepository.insertUser(data)
                lastException = null

                // レスポンス生成
                val response = UserResponse()
                response.message = MessageConfig.USER_REGISTERED
                return response // 正常終了
            } catch (e: DataIntegrityViolationException) {
                // ID重複の場合はリトライ
                println("ユーザID重複: リトライ ${attempt + 1}/$maxRetry")
                lastException = e
            } catch (e: Exception) {
                throw Exception(MessageConfig.USER_REGISTER_FAILED)
            }
        }
        // レスポンス生成
        val response = UserResponse()
        response.message = MessageConfig.USER_REGISTERED
        return response
    }

    /**
     * ユーザ更新処理
     *
     * @param request ユーザ更新リクエスト
     * @return ユーザ更新レスポンス
     */
    var updateUser = fun(request: UserRequest): UserResponse {
        try{
            // リクエストからデータ変換
            val data = UserData()
            data.userId = request.userId
            data.userName = request.userName
            data.userAddress = request.userAddress
            data.password = request.password
            data.userRole = request.userRole
            data.isValid = request.isValid

            // リポジトリへ更新処理依頼
            userRepository.updateUser(data)
        } catch (e: Exception) {
            throw Exception(MessageConfig.USER_UPDATE_FAILED)
        }

        // レスポンス生成
        val response = UserResponse()
        response.message = MessageConfig.USER_UPDATE_SUCCESS
        return response
    }

    /**
     * ユーザ削除処理
     *
     * @param request ユーザ削除リクエスト
     * @return ユーザ削除レスポンス
     */
    var deleteUser = fun(request: UserRequest): UserResponse {
        try {
            // リクエストからデータ変換
            val data = UserData()
            data.userId = request.userId

            // リポジトリへ削除処理依頼
            userRepository.deleteUser(data)
        } catch (e: Exception) {
            throw Exception(MessageConfig.USER_DELETE_FAILED)
        }

        // レスポンス生成
        val response = UserResponse()
        response.message = MessageConfig.USER_DELETE_SUCCESS
        return response
    }

    /**
     * ユーザ一覧取得処理
     *
     * @param request ユーザ一覧取得リクエスト
     * @return ユーザ一覧取得レスポンス
     */
    val getUserList = fun(request: UserRequest): UserResponse {
        // リポジトリへ問い合わせ
        val table: List<Map<String, Any?>> = userRepository.getUserList()
        val list: List<UserEntity> = tableToListEntity(table)

        // レスポンス生成
        val response = UserResponse()
        response.users = list
        response.message = "ユーザ一覧の取得が完了しました"
        return response
    }

    /**
     * ユーザ検索処理
     *
     * @param request ユーザ検索リクエスト
     * @return ユーザ検索レスポンス
     */
    val searchUser = fun(request: UserRequest): UserResponse {
        // リポジトリへ問い合わせ
        val table: List<Map<String, Any?>> = userRepository.searchUser(request)
        val list: List<UserEntity> = tableToListEntity(table)

        // レスポンス生成
        val response = UserResponse()
        response.users = list
        response.message = "ユーザ検索の取得が完了しました"
        return response
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
            entity.isValid = row["is_valid"] as Boolean?
            entity.createdAt = row["created_at"] as java.util.Date?
            entity.lastedAt = row["lasted_at"] as java.util.Date?
            list.add(entity)
        }
        return list
    }
}