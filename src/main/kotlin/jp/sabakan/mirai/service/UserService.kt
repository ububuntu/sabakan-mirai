package jp.sabakan.mirai.service

import jp.sabakan.mirai.data.UserData
import jp.sabakan.mirai.entity.UserEntity
import jp.sabakan.mirai.repository.UserRepository
import jp.sabakan.mirai.request.UserRequest
import jp.sabakan.mirai.response.UserResponse
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Service
class UserService {
    @Autowired
    lateinit var userRepository: UserRepository

    /**
     * ユーザログイン処理
     *
     * @param data ユーザデータ
     * @return ユーザエンティティ
     * @throws UserNameNotFoundException ユーザ名またはパスワードが違う場合
     */
    fun getUserLogin(data: UserData):  UserEntity {
        // リポジトリへ問い合わせ
        val table: List<Map<String, Any?>> = userRepository.getUserLogin(data)
        val list: List<UserEntity> = tableToListEntity(table)

        // 結果を返す
        return list.singleOrNull()
            //?: throw UserNameNotFoundException("ユーザ名またはパスワードが違います")
            ?: throw Exception("ユーザ名またはパスワードが違います")
    }

    /**
     * 指定ユーザの情報を取得する
     *
     * @param data ユーザデータ
     * @return ユーザエンティティ
     * @throws UserNameNotFoundException ユーザ情報が見つからない場合
     */
    fun getUserDetail(data: UserData): UserEntity {
        // リポジトリへ問い合わせ
        val table: List<Map<String, Any?>> = userRepository.getUserDetail(data)
        val list: List<UserEntity> = tableToListEntity(table)

        // 結果を返す
        return list.singleOrNull()
            //?: throw UserNameNotFoundException("ユーザ名またはパスワードが違います")
            ?: throw Exception("ユーザ情報が見つかりません")
    }

    /**
     * ユーザ登録処理
     *
     * @param request ユーザ登録リクエスト
     * @return ユーザ登録レスポンス
     */
    fun insertUser(request: UserRequest): UserResponse {
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
        } catch (e: Exception) {
            throw Exception("ユーザ登録に失敗しました")
        }

        // レスポンス生成
        val response = UserResponse()
        response.message = "ユーザ登録が完了しました"
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
            throw Exception("ユーザ登録に失敗しました")
        }

        // レスポンス生成
        val response = UserResponse()
        response.message = "ユーザ情報の更新が完了しました"
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
            throw Exception("ユーザ登録に失敗しました")
        }

        // レスポンス生成
        val response = UserResponse()
        response.message = "ユーザ削除が完了しました"
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
     * ユーザID生成処理
     *
     * @return ユーザID
     */
    private val userIdLock = Any()
    private fun toCreateUserId(): String {
        synchronized(userIdLock) {
            // 現在の年月（yyyyMM）
            val currentYm = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMM"))

            // 現在年月に一致する最大のユーザIDを取得（例: "U20251200007"）
            val maxUserId = userRepository.getMaxUserId(currentYm) // LIKE 'U202512%'

            // 連番の決定
            val nextSerial = if (maxUserId != null && maxUserId.length >= 13) {
                val maxYm = maxUserId.substring(1, 7)
                val maxSerial = maxUserId.substring(7).toIntOrNull() ?: 0
                if (maxYm == currentYm) maxSerial + 1 else 1
            } else {
                1
            }

            // ゼロ埋め5桁の連番
            val serialStr = String.format("%05d", nextSerial)

            // ユーザID生成（例: U20251200008）
            return "U$currentYm$serialStr"
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
            entity.isValid = row["is_valid"] as Boolean?
            entity.createdAt = row["created_at"] as java.util.Date?
            entity.lastedAt = row["lasted_at"] as java.util.Date?
            list.add(entity)
        }
        return list
    }

}