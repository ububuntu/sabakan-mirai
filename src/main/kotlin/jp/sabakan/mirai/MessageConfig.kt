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

        // M201: 面接履歴の取得に失敗しました
        const val INTERVIEW_NOT_FOUND = "面接履歴が見つかりません"

        // M202: 面接履歴の登録に成功しました
        const val INTERVIEW_INSERT_SUCCESS = "面接履歴の登録に成功しました"

        // M203: 面接履歴の登録に失敗しました
        const val INTERVIEW_INSERT_FAILED = "面接履歴の登録に失敗しました"

        // M301-2: Cab/Gab問題の登録に成功しました
        const val CABGAB_INSERT_SUCCESS = "Cab/Gab問題の登録に成功しました"

        // M302-2: Cab/Gab問題の登録に失敗しました
        const val CABGAB_INSERT_FAILED = "Cab/Gab問題の登録に失敗しました"

        // M303-2: Cab/Gab問題の削除に成功しました
        const val CABGAB_DELETE_SUCCESS = "Cab/Gab問題の削除に成功しました"

        // M304-2: Cab/Gab問題の削除に失敗しました
        const val CABGAB_DELETE_FAILED = "Cab/Gab問題の削除に失敗しました"


    }
}

