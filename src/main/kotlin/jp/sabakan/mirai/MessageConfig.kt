package jp.sabakan.mirai

import org.springframework.context.annotation.Configuration

@Configuration
public class MessageConfig {
    companion object {
        // --- 共通 / テスト (000-009) ---
        // M000: テストメッセージ
        const val TEST = "テストメッセージ"

        // --- ユーザ・認証関連 (010-049) ---
        // M010: ユーザ登録が完了しました
        const val USER_REGISTERED = "ユーザ登録が完了しました"
        // M011: ユーザ登録に失敗しました
        const val USER_REGISTER_FAILED = "ユーザ登録に失敗しました"
        // M012: ユーザ情報の更新が完了しました
        const val USER_UPDATE_SUCCESS = "ユーザ情報の更新が完了しました"
        // M013: ユーザ情報の更新に失敗しました
        const val USER_UPDATE_FAILED = "ユーザ情報の更新に失敗しました"
        // M014: ユーザ情報が見つかりません
        const val USER_NOT_FOUND = "ユーザ情報が見つかりません"
        // M015: ログインに成功しました
        const val LOGIN_SUCCESS = "ログインに成功しました"
        // M016: ログインに失敗しました
        const val LOGIN_FAILED = "ユーザ名またはパスワードが違います"
        // M017: ログアウトしました
        const val LOGOUT_SUCCESS = "ログアウトしました"
        // M018: パスワードの変更が完了しました
        const val PASSWORD_CHANGE_SUCCESS = "パスワードの変更が完了しました"
        // M019: パスワードの変更に失敗しました
        const val PASSWORD_CHANGE_FAILED = "パスワードの変更に失敗しました"

        // --- バリデーションエラー関連 (050-099) ---
        // M050: 必須項目が未入力です
        const val NOT_BLANK_ERROR = "必須項目が未入力です"
        // M051: ユーザ名は必須です
        const val USERNAME_NOT_BLANK_ERROR = "ユーザ名は必須です"
        // M052: メールアドレスは必須です
        const val EMAIL_NOT_BLANK_ERROR = "メールアドレスは必須です"
        // M053: メールアドレスの形式が不正です
        const val EMAIL_INVALID_FORMAT_ERROR = "メールアドレスの形式が不正です"
        // M054: 現在のパスワードが正しくありません
        const val CURRENT_PASSWORD_INCORRECT_ERROR = "現在のパスワードが正しくありません"
        // M055: 新しいパスワードと確認用パスワードが一致しません
        const val PASSWORD_MISMATCH_ERROR = "新しいパスワードと確認用パスワードが一致しません"
        // M056: パスワード入力内容エラー
        const val PASSWORD_INPUT_ERROR = "パスワードは英字と数字を含む8文字以上で入力してください"
        // M057: パスワード文字数制限エラー
        const val PASSWORD_LENGTH_ERROR = "パスワードは8文字以上100文字以内で入力してください"
        // M058: 入力文字数が多すぎます
        const val LENGTH_MAXIMUM_ERROR = "入力文字数が多すぎます"
        // M059: 不正な文字が含まれています
        const val INVALID_CHARACTER_ERROR = "不正な文字が含まれています"

        // --- 目標設定関連 (100-199) ---
        // M100: 目標設定が完了しました
        const val GOAL_SET_SUCCESS = "目標設定が完了しました"
        // M101: 目標設定に失敗しました
        const val GOAL_SET_FAILED = "目標設定に失敗しました"
        // M102: 目標情報の更新が完了しました
        const val GOAL_UPDATE_SUCCESS = "目標情報の更新が完了しました"

        // --- 面接履歴関連 (200-299) ---
        // M200: 面接履歴が見つかりません
        const val INTERVIEW_NOT_FOUND = "面接履歴が見つかりません"
        // M201: 面接履歴の登録に成功しました
        const val INTERVIEW_INSERT_SUCCESS = "面接履歴の登録に成功しました"
        // M202: 面接履歴の登録に失敗しました
        const val INTERVIEW_INSERT_FAILED = "面接履歴の登録に失敗しました"

        // --- SPI / CabGab 関連 (300-399) ---
        // M300: SPI問題の登録に成功しました
        const val SPI_INSERT_SUCCESS = "SPI問題の登録に成功しました"
        // M301: SPI問題の登録に失敗しました
        const val SPI_INSERT_FAILED = "SPI問題の登録に失敗しました"
        // M302: SPI問題の削除に成功しました
        const val SPI_DELETE_SUCCESS = "SPI問題の削除に成功しました"
        // M303: SPI問題の削除に失敗しました
        const val SPI_DELETE_FAILED = "SPI問題の削除に失敗しました"
        // M304: SPI問題の更新に成功しました
        const val SPI_UPDATE_SUCCESS = "SPI問題の更新に成功しました"
        // M305: SPI問題の更新に失敗しました
        const val SPI_UPDATE_FAILED = "SPI問題の更新に失敗しました"

        // M310: Cab/Gab問題の登録に成功しました
        const val CABGAB_INSERT_SUCCESS = "Cab/Gab問題の登録に成功しました"
        // M311: Cab/Gab問題の登録に失敗しました
        const val CABGAB_INSERT_FAILED = "Cab/Gab問題の登録に失敗しました"
        // M312: Cab/Gab問題の削除に成功しました
        const val CABGAB_DELETE_SUCCESS = "Cab/Gab問題の削除に成功しました"
        // M313: Cab/Gab問題の削除に失敗しました
        const val CABGAB_DELETE_FAILED = "Cab/Gab問題の削除に失敗しました"
        // M314: Cab/Gab問題の更新に成功しました
        const val CABGAB_UPDATE_SUCCESS = "Cab/Gab問題의更新に成功しました"
        // M315: Cab/Gab問題の更新に失敗しました
        const val CABGAB_UPDATE_FAILED = "Cab/Gab問題の更新に失敗しました"

        // --- ES(エントリーシート)関連 (400-499) ---
        // M400: ESの登録に成功しました
        const val ES_INSERT_SUCCESS = "ESの登録に成功しました"
        // M401: ESの登録に失敗しました
        const val ES_INSERT_FAILED = "ESの登録に失敗しました"
        // M402: ESの更新に成功しました
        const val ES_UPDATE_SUCCESS = "ESの更新に成功しました"
        // M403: ESの更新に失敗しました
        const val ES_UPDATE_FAILED = "ESの更新に失敗しました"
        // M404: ESの削除に成功しました
        const val ES_DELETE_SUCCESS = "ESの削除に成功しました"
        // M405: ESの削除に失敗しました
        const val ES_DELETE_FAILED = "ESの削除に失敗しました"
    }
}
