package jp.sabakan.mirai

import org.springframework.context.annotation.Configuration

@Configuration
public class MessageConfig {
    companion object{
        // M000: テストメッセージ
        const val TEST = "テストメッセージ"

        // M001: ユーザ登録が完了しました
        const val USER_REGISTERED = "ユーザ登録が完了しました"

        // M002: ユーザ登録に失敗しました
        const val USER_REGISTER_FAILED = "ユーザ登録に失敗しました"

        // M003: ユーザ情報の更新が完了しました
        const val USER_UPDATE_SUCCESS = "ユーザ情報の更新が完了しました"

        // M004: ユーザ情報の更新に失敗しました
        const val USER_UPDATE_FAILED = "ユーザ情報の更新に失敗しました"

        // M005: ユーザ情報の削除が完了しました
        const val USER_DELETE_SUCCESS = "ユーザ情報の削除が完了しました"

        // M006: ユーザ情報の削除に失敗しました
        const val USER_DELETE_FAILED = "ユーザ情報の削除に失敗しました"

        // M007: ユーザ情報の取得に失敗しました
        const val USER_NOT_FOUND = "ユーザ情報が見つかりません"

        // M008: ログインに成功しました
        const val LOGIN_SUCCESS = "ログインに成功しました"

        // M009: ログインに失敗しました
        const val LOGIN_FAILED = "ユーザ名またはパスワードが違います"

        // M010: ログアウトしました
        const val LOGOUT_SUCCESS = "ログアウトしました"

        // M011: パスワードの変更が完了しました
        const val PASSWORD_CHANGE_SUCCESS = "パスワードの変更が完了しました"

        // M012: パスワードの変更に失敗しました
        const val PASSWORD_CHANGE_FAILED = "パスワードの変更に失敗しました"

        // M013: 未入力エラー
        const val NOT_BLANK_ERROR = "必須項目が未入力です"

        // M014: 文字数制限超過エラー
        const val LENGTH_MAXIMUM_ERROR = "入力が長すぎます"

        // M015: 必須入力エラー(ユーザ名)
        const val USERNAME_NOT_BLANK_ERROR = "ユーザ名は必須です"

        // M016: 必須入力エラー(メールアドレス)
        const val EMAIL_NOT_BLANK_ERROR = "メールアドレスは必須です"

        // M017: 入力形式エラー(メールアドレス)
        const val EMAIL_INVALID_FORMAT_ERROR = "メールアドレスの形式が不正です"

        // M018: 目標設定が完了しました
        const val GOAL_SET_SUCCESS = "目標設定が完了しました"

        // M019: 目標設定に失敗しました
        const val GOAL_SET_FAILED = "目標設定に失敗しました"

        // M020: 目標情報の更新が完了しました
        const val GOAL_UPDATE_SUCCESS = "目標情報の更新が完了しました"

        // M201: 面接履歴の取得に失敗しました
        const val INTERVIEW_NOT_FOUND = "面接履歴が見つかりません"

        // M202: 面接履歴の登録に成功しました
        const val INTERVIEW_INSERT_SUCCESS = "面接履歴の登録に成功しました"

        // M203: 面接履歴の登録に失敗しました
        const val INTERVIEW_INSERT_FAILED = "面接履歴の登録に失敗しました"

        // M301: SPI問題の登録に成功しました
        const val SPI_INSERT_SUCCESS = "SPI問題の登録に成功しました"

        // M302: SPI問題の登録に失敗しました
        const val SPI_INSERT_FAILED = "SPI問題の登録に失敗しました"

        // M303: SPI問題の削除に成功しました
        const val SPI_DELETE_SUCCESS = "SPI問題の削除に成功しました"

        // M304: SPI問題の削除に失敗しました
        const val SPI_DELETE_FAILED = "SPI問題の削除に失敗しました"

        // M305: SPI問題の更新に成功しました
        const val SPI_UPDATE_SUCCESS = "SPI問題の更新に成功しました"

        // M306: SPI問題の更新に失敗しました
        const val SPI_UPDATE_FAILED = "SPI問題の更新に失敗しました"

        // M307: Cab/Gab問題の登録に成功しました
        const val CABGAB_INSERT_SUCCESS = "Cab/Gab問題の登録に成功しました"

        // M308: Cab/Gab問題の登録に失敗しました
        const val CABGAB_INSERT_FAILED = "Cab/Gab問題の登録に失敗しました"

        // M309: Cab/Gab問題の削除に成功しました
        const val CABGAB_DELETE_SUCCESS = "Cab/Gab問題の削除に成功しました"

        // M310: Cab/Gab問題の削除に失敗しました
        const val CABGAB_DELETE_FAILED = "Cab/Gab問題の削除に失敗しました"

        // M401: ESの登録に成功しました
        const val ES_INSERT_SUCCESS = "ESの登録に成功しました"

        // M402: ESの登録に失敗しました
        const val ES_INSERT_FAILED = "ESの登録に失敗しました"

        // M403: ESの更新に成功しました
        const val ES_UPDATE_SUCCESS = "ESの更新に成功しました"

        // M404: ESの更新に失敗しました
        const val ES_UPDATE_FAILED = "ESの更新に失敗しました"

        // M405: ESの削除に成功しました
        const val ES_DELETE_SUCCESS = "ESの削除に成功しました"

        // M406: ESの削除に失敗しました
        const val ES_DELETE_FAILED = "ESの削除に失敗しました"
    }
}

