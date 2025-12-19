package jp.sabakan.mirai.service

import jp.sabakan.mirai.MessageConfig
import jp.sabakan.mirai.data.InterviewData
import jp.sabakan.mirai.entity.InterviewEntity
import jp.sabakan.mirai.repository.InterviewRepository
import jp.sabakan.mirai.request.InterviewRequest
import jp.sabakan.mirai.response.InterviewResponse
import org.apache.logging.log4j.message.Message
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.stereotype.Service
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.UUID

@Service
class InterviewService {
    @Autowired
    lateinit var interviewRepository: InterviewRepository

    /**
     * 指定ユーザの面接履歴を取得する
     *
     * @param request 面接履歴リクエスト
     * @return 面接履歴レスポンス
     * @throws Exception 面接履歴が見つからない場合
     */
    fun getInterviews(request: InterviewRequest): InterviewResponse {
        // リクエストからデータへ変換
        val data = InterviewData()
        data.userId = request.userId

        // リポジトリへ問い合わせ
        val table: List<Map<String, Any?>> = interviewRepository.getInterviews(data)
        val list: List<InterviewEntity> = tableToListEntity(table)

        if (list.isNotEmpty()) {
            throw Exception(MessageConfig.INTERVIEW_NOT_FOUND)
        }

        return InterviewResponse().apply {
            interviews = list
            message = null
        }
    }

    /**
     * 面接履歴を新規登録する
     *
     * @param request 面接履歴リクエスト
     * @return 面接履歴レスポンス
     */
    fun insertInterview(request: InterviewRequest): InterviewResponse {
        var lastException: Exception? = null

        // ID重複対応のためリトライ処理
        val maxRetry = 5
        repeat(maxRetry) { attempt ->
            try {
                // リクエストからデータへ変換
                val data = InterviewData()
                data.interviewId = toCreateInterviewId()
                data.userId = request.userId
                data.interviewExpression = request.interviewExpression
                data.interviewEyes = request.interviewEyes
                data.interviewPosture = request.interviewPosture
                data.interviewVoice = request.interviewVoice
                data.interviewScore = request.interviewScore

                // リポジトリへ登録
                interviewRepository.insertInterview(data)
                lastException = null

                // レスポンス生成
                val response = InterviewResponse()
                response.message = MessageConfig.INTERVIEW_INSERT_SUCCESS
                return response // 正常終了
            } catch (e: DataIntegrityViolationException) {
                // ID重複の場合はリトライ
                println("ID重複: リトライ${attempt + 1}/$maxRetry")
                lastException = e
            } catch (e: Exception) {
                throw Exception(MessageConfig.INTERVIEW_INSERT_FAILED)
            }
        }
        // レスポンス生成
        val response = InterviewResponse()
        response.message = MessageConfig.INTERVIEW_INSERT_SUCCESS
        return response // 正常終了
    }

    /**
     * 面接IDを生成する
    *
     * @return 面接ID
     */
    private fun toCreateInterviewId(): String {
        // UUIDを生成
        val uuid = UUID.randomUUID().toString()

        // 新しい面接IDの生成
        return "I$uuid"
    }

    /**
     * テーブルデータをエンティティリストに変換する
     *
     * @param table テーブルデータ
     * @return エンティティリスト
     */
    private fun tableToListEntity(table: List<Map<String, Any?>>): List<InterviewEntity> {
        val list = mutableListOf<InterviewEntity>()
        for (row in table) {
            val entity = InterviewEntity()
            entity.interviewId = row["interview_id"] as String?
            entity.userId = row["user_id"] as String?
            entity.interviewExpression = row["interview_expression"] as String?
            entity.interviewEyes = row["interview_eyes"] as String?
            entity.interviewPosture = row["interview_posture"] as String?
            entity.interviewVoice = row["interview_voice"] as String?
            entity.interviewDate = row["interview_date"] as String?
            entity.interviewScore = row["interview_score"] as String?
            list.add(entity)
        }
        return list
    }
}