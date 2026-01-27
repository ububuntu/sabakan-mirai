package jp.sabakan.mirai.request

import lombok.Data

@Data
class InterviewRequest {
    // 面接ID
    var interviewId: String? = null

    // ユーザID
    var userId: String? = null

    // 表情評価
    var interviewExpression: String? = null

    // 視線評価
    var interviewEyes: String? = null

    // 姿勢評価
    var interviewPosture: String? = null

    // 発話速度評価
    var interviewVoice: String? = null

    // 面接日時
    var interviewDate: String? = null

    // 面接総合評価
    var interviewScore: String? = null

    // 面接コメント
    var interviewComment: String? = null
}