package jp.sabakan.mirai.repository

import jp.sabakan.mirai.data.CabGabData
import jp.sabakan.mirai.data.CabGabDetailData
import jp.sabakan.mirai.data.CabGabHistoryData
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import org.springframework.stereotype.Repository
import java.math.BigDecimal

@Repository
class CabGabRepository {
    @Autowired
    lateinit var jdbc: NamedParameterJdbcTemplate

    // すべてのCabGab質問を取得するSQLクエリ
    val getAllCabGabSql = """
        SELECT * FROM cabgab_m ORDER BY cabgab_category, cabgab_id
    """.trimIndent()

    // 指定IDのCabGab質問を取得するSQLクエリ
    val getCabGabByIdSql = """
        SELECT * FROM cabgab_m WHERE cabgab_id = :cabgabId
    """.trimIndent()

    // カテゴリーごとにCabGab質問を取得するSQLクエリ
    val getCabGabByCategory = """
    SELECT * FROM cabgab_m
    WHERE cabgab_category = :cabgabCategory
    ORDER BY RAND() LIMIT 1
    """.trimIndent()

    // 正解を取得するSQLクエリ
    val getCorrectAnswerSql = """
        SELECT cabgab_correct_answer FROM cabgab_m WHERE cabgab_id = :cabgabId
    """.trimIndent()

    // 未完了のCabGab履歴を取得するSQLクエリ
    val getUnfinishedSql = """
        SELECT cabgab_hs_id FROM cabgab_history_t 
        WHERE user_id = :userId AND is_finished = FALSE
        LIMIT 1
    """.trimIndent()

    // CabGab明細の正誤情報を取得するSQLクエリ
    val getDetailsSql = """
        SELECT is_correct FROM cabgab_detail_t 
        WHERE cabgab_hs_id = :cabgabHsId
    """.trimIndent()

    // 問題文を追加するSQLクエリ
    val insertCabGab = """
        INSERT INTO cabgab_m (cabgab_id, cabgab_content, cabgab_answer1, cabgab_answer2, cabgab_answer3, cabgab_answer4, cabgab_correct_answer, cabgab_category)
        VALUES (:cabgabId, :cabgabContent, :cabgabAnswer1, :cabgabAnswer2, :cabgabAnswer3, :cabgabAnswer4, :cabgabCorrectAnswer, :cabgabCategory)
    """.trimIndent()

    // CabGab履歴を追加するSQLクエリ
    val insertCabGabHistorySql = """
        INSERT INTO cabgab_history_t (cabgab_hs_id, user_id, total_questions, correct_count, accuracy_rate, cabgab_hs_date, is_finished)
        VALUES (:cabgabHsId, :userId, :totalQuestions, :correctCount, :accuracyRate, CURRENT_TIMESTAMP, FALSE)
    """.trimIndent()

    // CabGab明細を追加するSQLクエリ
    val insertCabGabDetailSql = """
        INSERT INTO cabgab_detail_t (cabgab_dl_id, cabgab_hs_id, cabgab_id, user_answer, is_correct)
        VALUES (:cabgabDlId, :cabgabHsId, :cabgabId, :userAnswer, :isCorrect)
    """.trimIndent()

    // CabGab問題文を更新するSQLクエリ
    val updateCabGabSql = """
        UPDATE cabgab_m 
        SET cabgab_content = :cabgabContent, 
            cabgab_answer1 = :cabgabAnswer1, 
            cabgab_answer2 = :cabgabAnswer2, 
            cabgab_answer3 = :cabgabAnswer3, 
            cabgab_answer4 = :cabgabAnswer4, 
            cabgab_correct_answer = :cabgabCorrectAnswer, 
            cabgab_category = :cabgabCategory
        WHERE cabgab_id = :cabgabId
    """.trimIndent()

    // CabGab履歴を更新するSQLクエリ（試験終了時）
    val updateCabGabHistorySql = """
        UPDATE cabgab_history_t 
        SET correct_count = :correctCount, accuracy_rate = :accuracyRate, is_finished = TRUE 
        WHERE cabgab_hs_id = :cabgabHsId
    """.trimIndent()

    // CabGab明細の件数を取得するSQLクエリ
    val countDetailsSql = """
        SELECT COUNT(*) FROM cabgab_detail_t 
        WHERE cabgab_hs_id = :cabgabHsId
    """.trimIndent()

    // CABGABを削除するSQLクエリ
    val deleteCabGabSql = """
        DELETE FROM cabgab_m WHERE cabgab_id = :cabgabId
    """.trimIndent()

    // CabGab明細を削除するSQLクエリ
    val deleteCabGabDetailSql = """
        DELETE FROM cabgab_detail_t WHERE cabgab_id = :cabgabId
    """.trimIndent()

    /**
     * すべてのCabGab質問を取得する
     *
     * @return CabGab質問のリスト
     */
    fun getAllCabGab(): List<Map<String, Any?>> {
        return jdbc.queryForList(getAllCabGabSql, emptyMap<String, Any?>())
    }

    /**
     * 指定IDのCabGab質問を取得する
     *
     * @param data CabGabData（cabGabIdを含む）
     * @return 指定IDのCabGab質問のリスト
     */
    fun getCabGabById(data: CabGabData): List<Map<String, Any?>> {
        // パラメータマップを作成
        val params = mapOf(
            "cabgabId" to data.cabGabId
        )

        // クエリを実行して結果を取得
        return jdbc.queryForList(getCabGabByIdSql, params)
    }

    /**
     * CabGabの問題文を追加する
     *
     * @param data CabGabData（追加する問題文のデータを含む）
     * @return 更新件数
     */
    fun insertCabGab(data: CabGabData): Int {
        // パラメータマップを作成
        val params = mapOf<String, Any?>(
            "cabgabId" to data.cabGabId,
            "cabgabContent" to data.cabGabContent,
            "cabgabAnswer1" to data.cabGabAnswer1,
            "cabgabAnswer2" to data.cabGabAnswer2,
            "cabgabAnswer3" to data.cabGabAnswer3,
            "cabgabAnswer4" to data.cabGabAnswer4,
            "cabgabCorrectAnswer" to data.cabGabCorrectAnswer,
            "cabgabCategory" to data.cabGabCategory
        )

        // クエリを実行して更新件数を取得
        return jdbc.update(insertCabGab, params)
    }

    /**
     * CabGab履歴を追加する
     *
     * @param historyData CabGabHistoryData（追加する履歴データを含む）
     * @return 更新件数
     */
    fun insertHistory(historyData: CabGabHistoryData): Int {
        // パラメータマップを作成
        val params = mapOf(
            "cabgabHsId" to historyData.cabGabHsId,
            "userId" to historyData.userId,
            "totalQuestions" to historyData.totalQuestions,
            "correctCount" to historyData.correctCount,
            "accuracyRate" to historyData.accuracyRate
        )

        // クエリを実行して更新件数を取得
        return jdbc.update(insertCabGabHistorySql, params)
    }

    /**
     * CabGab明細を追加する
     *
     * @param detailData CabGabDetailData（追加する明細データを含む）
     * @return 更新件数
     */
    fun insertDetail(detailData: CabGabDetailData): Int {
        // パラメータマップを作成
        val params = mapOf(
            "cabgabDlId" to detailData.cabGabDlId,
            "cabgabHsId" to detailData.cabGabHsId,
            "cabgabId" to detailData.cabGabId,
            "userAnswer" to detailData.userAnswer,
            "isCorrect" to detailData.isCorrect
        )

        // クエリを実行して更新件数を取得
        return jdbc.update(insertCabGabDetailSql, params)
    }

    /**
     * CabGab履歴を更新する（試験終了時）
     *
     * @param cabgabHsId CabGab履歴ID
     * @param correctCount 正解数
     * @param accuracyRate 正答率
     * @return 更新件数
     */
    fun updateExamResult(cabgabHsId: String, correctCount: Int, accuracyRate: BigDecimal): Int {
        // パラメータマップを作成
        val params = mapOf(
            "cabgabHsId" to cabgabHsId,
            "correctCount" to correctCount,
            "accuracyRate" to accuracyRate
        )

        // クエリを実行して更新件数を取得
        return jdbc.update(updateCabGabHistorySql, params)
    }

    /**
     * カテゴリーごとにCabGab質問を取得する
     *
     * @param data CabGabData（cabGabCategoryを含む）
     * @return 指定カテゴリーのCabGab質問のリスト
     */
    fun getCabGab(data: CabGabData): List<Map<String, Any?>> {
        // パラメータマップを作成
        val params = mapOf(
            "cabgabCategory" to data.cabGabCategory
        )

        // クエリを実行して結果を取得
        return jdbc.queryForList(getCabGabByCategory, params)
    }

    /**
     * 未完了のCabGab履歴IDを取得する
     *
     * @param userId ユーザーID
     * @return 未完了のCabGab履歴ID、存在しない場合はnull
     */
    fun getUnfinishedCabGabHsId(userId: String): String? {
        val params = mapOf("userId" to userId)
        return try{
            // クエリを実行して未完了のCabGab履歴IDを取得
            jdbc.queryForObject(getUnfinishedSql, params, String::class.java)
        } catch (e: Exception){
            // 未完了のCabGab履歴が存在しない場合はnullを返す
            null
        }
    }

    /**
     * 指定CabGab履歴IDの正誤情報を取得する
     *
     * @param cabgabHsId CabGab履歴ID
     * @return 正誤情報のリスト
     */
    fun findDetailsByHistoryId(cabgabHsId: String): List<Boolean> {
        // パラメータマップを作成
        val params = mapOf("cabgabHsId" to cabgabHsId)
        return jdbc.query(getDetailsSql, params) { rs, _ ->
            rs.getBoolean("is_correct")
        }
    }

    /**
     * 指定CabGabIDの正解番号を取得する
     *
     * @param cabgabId CabGabID
     * @return 正解番号、存在しない場合はnull
     */
    fun getCorrectAnswer(cabgabId: String): Int? {
        // パラメータマップを作成
        val params = mapOf("cabgabId" to cabgabId)
        return try{
            // クエリを実行して正解番号を取得
            jdbc.queryForObject(getCorrectAnswerSql, params, Int::class.java)
        } catch (e: Exception){
            // 正解番号が存在しない場合はnullを返す
            null
        }
    }

    /**
     * 指定CabGab履歴IDの明細件数を取得する
     *
     * @param cabgabHsId CabGab履歴ID
     * @return 明細件数
     */
    fun countDetailsByHistoryId(cabgabHsId: String): Int {
        // パラメータマップを作成
        val params = mapOf("cabgabHsId" to cabgabHsId)
        // クエリを実行して明細件数を取得
        return jdbc.queryForObject(countDetailsSql, params, Int::class.java) ?: 0
    }

    /**
     * CabGab問題文を更新する
     *
     * @param data CabGabData（更新する問題文のデータを含む）
     * @return 更新件数
     */
    fun updateCabGab(data: CabGabData): Int {
        // パラメータマップを作成
        val params = mapOf<String, Any?>(
            "cabgabId" to data.cabGabId,
            "cabgabContent" to data.cabGabContent,
            "cabgabAnswer1" to data.cabGabAnswer1,
            "cabgabAnswer2" to data.cabGabAnswer2,
            "cabgabAnswer3" to data.cabGabAnswer3,
            "cabgabAnswer4" to data.cabGabAnswer4,
            "cabgabCorrectAnswer" to data.cabGabCorrectAnswer,
            "cabgabCategory" to data.cabGabCategory
        )

        // クエリを実行して更新件数を取得
        return jdbc.update(updateCabGabSql, params)
    }

    /**
     * CabGabを削除する
     *
     * @param cabgabId CabGabID
     * @return 更新件数
     */
    fun deleteCabGab(cabgabId: String): Int {
        // パラメータマップを作成
        val params = mapOf("cabgabId" to cabgabId)

        // クエリを実行して更新件数を取得
        jdbc.update(deleteCabGabDetailSql, params)
        return jdbc.update(deleteCabGabSql, params)
    }
}