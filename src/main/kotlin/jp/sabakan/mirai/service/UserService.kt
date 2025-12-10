package jp.sabakan.mirai.service

import jp.sabakan.mirai.data.UserData
import jp.sabakan.mirai.entity.UserEntity
import jp.sabakan.mirai.repository.UserRepository
import jp.sabakan.mirai.request.UserRequest
import jp.sabakan.mirai.response.UserResponse
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

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
        // リクエストからデータ変換
        val data = UserData()
        data.userId = request.userId
        data.userName = request.userName
        data.userAddress = request.userAddress
        data.password = request.password
        data.userRole = request.userRole
        data.isValid = request.isValid

        // リポジトリへ登録処理依頼
        userRepository.insertUser(data)

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
        // リクエストからデータ変換
        val data = UserData()
        data.userId = request.userId

        // リポジトリへ削除処理依頼
        userRepository.deleteUser(data)

        // レスポンス生成
        val response = UserResponse()
        response.message = "ユーザ削除が完了しました"
        return response
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