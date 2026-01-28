package jp.sabakan.mirai.request

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size
import jp.sabakan.mirai.MessageConfig
import lombok.Data

@Data
class EsRequest {
    // EsID
    var esId: String? = null

    // ユーザID
    var userId: String? = null

    // ES内容 - 志望動機
    @field:NotBlank
    @field:Size(max = 500, message = MessageConfig.LENGTH_MAXIMUM_ERROR)
    var esContentReason: String? = null

    // ES内容 - 自己PR
    @field:NotBlank
    @field:Size(max = 500, message = MessageConfig.LENGTH_MAXIMUM_ERROR)
    var esContentSelfpr: String? = null

    // ES内容 - 学生時代の活動
    @field:NotBlank
    @field:Size(max = 500, message = MessageConfig.LENGTH_MAXIMUM_ERROR)
    var esContentActivities: String? = null

    // ES内容 - 長所短所
    @field:NotBlank
    @field:Size(max = 500, message = MessageConfig.LENGTH_MAXIMUM_ERROR)
    var esContentStwe: String? = null

    // 志望職種
    @field:NotBlank
    @field:Size(max = 500, message = MessageConfig.LENGTH_MAXIMUM_ERROR)
    var esOccupation: String? = null

    // ES作成・更新日
    var esDate: String? = null
}