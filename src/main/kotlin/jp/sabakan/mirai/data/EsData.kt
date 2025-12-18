package jp.sabakan.mirai.data

import lombok.Data

@Data
class EsData {
    // EsID
    var esId: String? = null

    // ユーザID
    var userId: String? = null

    // ES内容 - 志望動機
    var esContentReason: String? = null

    // ES内容 - 自己PR
    var esContentSelfpr: String? = null

    // ES内容 - 学生時代の活動
    var esContentActivities: String? = null

    // ES内容 - 長所短所
    var esContentStwe: String? = null

    // 志望職種
    var esOccupation: String? = null

    // ES作成・更新日
    var esDate: String? = null
}