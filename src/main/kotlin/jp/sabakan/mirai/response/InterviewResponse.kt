package jp.sabakan.mirai.response

import jp.sabakan.mirai.entity.InterviewEntity
import lombok.Data

@Data
class InterviewResponse {
    // 面接履歴一覧
    var interviews: List<InterviewEntity>? = null

    // 返却メッセージ
    var message: String? = null
}