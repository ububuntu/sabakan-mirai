package jp.sabakan.mirai.service

import jp.sabakan.mirai.MessageConfig
import jp.sabakan.mirai.data.UserData
import jp.sabakan.mirai.entity.UserEntity
import jp.sabakan.mirai.repository.UserRepository
import org.apache.logging.log4j.message.Message
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

@Service
class LoginsService {
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
        //?: throw UserNameNotFoundException(MessageConfig.LOGIN_FAILED)
            ?: throw Exception(MessageConfig.LOGIN_FAILED)
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