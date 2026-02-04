package jp.sabakan.mirai.repository

import jp.sabakan.mirai.data.CabGabData
import jp.sabakan.mirai.data.CabGabDetailData
import jp.sabakan.mirai.data.CabGabHistoryData
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import org.springframework.stereotype.Repository

/**
 * CABGABデータへのデータアクセスを担当するRepository
 * データベースへの読み書きのみを行い、ビジネスロジックは含まない
 */
@Repository
class CabGabRepository {

    @Autowired
    lateinit var jdbc: NamedParameterJdbcTemplate

    // --- SQL定義エリア ---

    // カテゴリ別にランダムで問題を取得するSQL
    private val selectCabGabByCategorySql = """
        SELECT * FROM cabgab_m 
        WHERE cabgab_category = :cabgabCategory
    """.trimIndent()

    // 結果を保存するSQL
    private val insertCabGabHistorySql = """
        INSERT INTO cabgab_history_t (cabgab_hs_id, user_id, is_finished)
        VALUES (:cabgabHsId, :userId, FALSE)
    """.trimIndent()

    // 詳細結果を保存するSQL
    private val insertCabGabDetailSql = """
        INSERT INTO cabgab_detail_t (cabgab_dl_id, cabgab_hs_id, question_number, cabgab_id, is_correct)
        VALUES (:cabgabDlId, :cabgabHsId, :questionNumber, :cabgabId, FALSE)
    """.trimIndent()

    // 詳細結果の回答を更新するSQL
    private val updateCabGabDetailAnswerSql = """
        UPDATE cabgab_detail_t 
        SET user_answer = :userAnswer, 
            is_correct = :isCorrect
        WHERE cabgab_hs_id = :cabgabHsId AND question_number = :questionNumber
    """.trimIndent()

    // 詳細結果を履歴IDで取得するSQL（質問番号順）
    private val findDetailsOrderedSql = """
        SELECT d.*, m.* FROM cabgab_detail_t d
        JOIN cabgab_m m ON d.cabgab_id = m.cabgab_id
        WHERE d.cabgab_hs_id = :cabgabHsId
        ORDER BY d.question_number ASC
    """.trimIndent()

    // 履歴を完了に更新するSQL
    private val updateHistoryFinishedSql = """
        UPDATE cabgab_history_t SET is_finished = TRUE WHERE cabgab_hs_id = :cabgabHsId
    """.trimIndent()

    // ユーザ別に完了した履歴を取得するSQL
    private val selectHistoryByUserSql = """
        SELECT 
            cabgab_hs_id, 
            cabgab_hs_date, 
            (SELECT COUNT(*) FROM cabgab_detail_t WHERE cabgab_hs_id = h.cabgab_hs_id) as total_questions,
            (SELECT COUNT(*) FROM cabgab_detail_t WHERE cabgab_hs_id = h.cabgab_hs_id AND is_correct = TRUE) as correct_count
        FROM cabgab_history_t h
        WHERE user_id = :userId AND is_finished = TRUE
        ORDER BY cabgab_hs_date DESC
    """.trimIndent()

    // 進行中の履歴IDを取得するSQL
    private val selectInProgressSql = """
        SELECT cabgab_hs_id FROM cabgab_history_t
        WHERE user_id = :userId AND is_finished = FALSE
        ORDER BY cabgab_hs_date DESC
        LIMIT 1
    """.trimIndent()

    // 全CABGAB問題を取得するSQL
    private val selectAllCabGabSql = """
        SELECT * FROM cabgab_m ORDER BY cabgab_category, cabgab_id
    """.trimIndent()

    // CABGAB問題をIDで取得するSQL
    private val selectCabGabByIdSql = """
        SELECT * FROM cabgab_m WHERE cabgab_id = :cabgabId
    """.trimIndent()

    // CABGABマスタの登録・更新・削除SQL
    private val insertCabGabMasterSql = """
        INSERT INTO cabgab_m (
            cabgab_id, cabgab_content, cabgab_answer1, cabgab_answer2, cabgab_answer3, cabgab_answer4, 
            cabgab_correct_answer, cabgab_category
        ) VALUES (
            :cabgabId, :cabgabContent, :cabgabAnswer1, :cabgabAnswer2, :cabgabAnswer3, :cabgabAnswer4, 
            :cabgabCorrectAnswer, :cabgabCategory
        )
    """.trimIndent()

    // CABGABマスタの更新SQL
    private val updateCabGabMasterSql = """
        UPDATE cabgab_m SET 
            cabgab_content = :cabgabContent,
            cabgab_answer1 = :cabgabAnswer1,
            cabgab_answer2 = :cabgabAnswer2,
            cabgab_answer3 = :cabgabAnswer3,
            cabgab_answer4 = :cabgabAnswer4,
            cabgab_correct_answer = :cabgabCorrectAnswer,
            cabgab_category = :cabgabCategory
        WHERE cabgab_id = :cabgabId
    """.trimIndent()

    // CABGABマスタの削除SQL
    private val deleteCabGabMasterSql = """
        DELETE FROM cabgab_m WHERE cabgab_id = :cabgabId
    """.trimIndent()

    // CABGABマスタの件数取得SQL
    private val getCabGabCount = """
        SELECT COUNT(*) AS cnt FROM cabgab_m
    """.trimIndent()

    // --- メソッドエリア ---

    /**
     * 指定カテゴリのCABGAB問題からランダムに指定数取得する
     *
     * @param data CABGABカテゴリ情報
     * @param limit 取得数
     * @return ランダムに選ばれたCABGAB問題リスト
     */
    fun getRandomQuestions(data: CabGabData, limit: Int): List<Map<String, Any?>> {
        val params = mapOf("cabgabCategory" to data.cabgabCategory)

        // 1. 候補を全件取得
        val candidates = jdbc.queryForList(selectCabGabByCategorySql, params)

        // データが0件の場合は空リストを返す
        if (candidates.isEmpty()) return emptyList()

        // 2. 指定数(limit)になるまで、候補からランダムに選び続ける（重複あり）
        val result = mutableListOf<Map<String, Any?>>()
        repeat(limit) {
            // candidates.random() はリストからランダムに1つ要素を返す
            result.add(candidates.random())
        }

        return result
    }

    /**
     * CABGAB履歴を挿入する
     *
     * @param data CABGAB履歴データ
     */
    fun insertHistory(data: CabGabHistoryData) {
        val params = mapOf<String, Any?>(
            "cabgabHsId" to data.cabgabHsId, "userId" to data.userId
        )
        jdbc.update(insertCabGabHistorySql, params)
    }

    /**
     * CABGAB詳細結果を挿入する
     *
     * @param data CABGAB詳細データ
     */
    fun insertDetail(data: CabGabDetailData) {
        val params = mapOf<String, Any?>(
            "cabgabDlId" to data.cabgabDlId,
            "cabgabHsId" to data.cabgabHsId,
            "questionNumber" to data.questionNumber,
            "cabgabId" to data.cabgabId
        )
        jdbc.update(insertCabGabDetailSql, params)
    }

    /**
     * CABGAB詳細結果の回答を更新する
     *
     * @param data CABGAB詳細データ
     */
    fun updateDetailAnswer(data: CabGabDetailData) {
        val params = mapOf<String, Any?>(
            "userAnswer" to data.userAnswer,
            "isCorrect" to data.isCorrect,
            "cabgabHsId" to data.cabgabHsId,
            "questionNumber" to data.questionNumber
        )
        jdbc.update(updateCabGabDetailAnswerSql, params)
    }

    /**
     * CABGAB履歴IDに紐づく詳細結果を質問番号順で取得する
     *
     * @param data CABGAB履歴データ
     * @return 詳細結果リスト
     */
    fun findDetailsWithQuestion(data: CabGabHistoryData): List<Map<String, Any?>> {
        val param = mapOf<String, Any?>(
            "cabgabHsId" to data.cabgabHsId
        )
        return jdbc.queryForList(findDetailsOrderedSql, param)
    }

    /**
     * CABGAB履歴を完了に更新する
     *
     * @param data CABGAB履歴データ
     */
    fun updateHistoryFinished(data: CabGabHistoryData) {
        val param = mapOf<String, Any?>(
            "cabgabHsId" to data.cabgabHsId
        )
        jdbc.update(updateHistoryFinishedSql, param)
    }

    /**
     * ユーザIDに紐づく完了したCABGAB履歴を取得する
     *
     * @param data CABGAB履歴データ
     * @return CABGAB履歴リスト
     */
    fun findHistoryByUserId(data: CabGabHistoryData): List<Map<String, Any?>> {
        val params = mapOf<String, Any?>(
            "userId" to data.userId
        )
        return jdbc.queryForList(selectHistoryByUserSql, params)
    }

    /**
     * ユーザIDに紐づく進行中のCABGAB履歴IDを取得する
     *
     * @param data CABGAB履歴データ
     * @return 進行中のCABGAB履歴ID (存在しない場合はnull)
     */
    fun findInProgressId(data: CabGabHistoryData): String? {
        val params = mapOf<String, Any?>(
            "userId" to data.userId
        )
        val list = jdbc.queryForList(selectInProgressSql, params)
        return if (list.isNotEmpty()) list[0]["cabgab_hs_id"] as String else null
    }

    /**
     * 全CABGAB問題を取得する
     *
     * @return CABGAB問題リスト
     */
    fun findAllCabGab(): List<Map<String, Any?>> {
        val params = emptyMap<String, Any>()
        return jdbc.queryForList(selectAllCabGabSql, params)
    }

    /**
     * CABGAB問題をIDで取得する
     *
     * @param data CABGABデータ
     * @return CABGAB問題データ (存在しない場合はnull)
     */
    fun findCabGabById(data: CabGabData): Map<String, Any?>? {
        val list = jdbc.queryForList(selectCabGabByIdSql, mapOf("cabgabId" to data.cabgabId))
        return list.firstOrNull()
    }fun insertCabGabMaster(data: CabGabData): Int {
        val params = mapOf<String, Any?>(
            "cabgabId" to data.cabgabId,
            "cabgabContent" to data.cabgabContent,
            "cabgabAnswer1" to data.cabgabAnswer1,
            "cabgabAnswer2" to data.cabgabAnswer2,
            "cabgabAnswer3" to data.cabgabAnswer3,
            "cabgabAnswer4" to data.cabgabAnswer4,
            "cabgabCorrectAnswer" to data.cabgabCorrectAnswer,
            "cabgabCategory" to data.cabgabCategory
        )
        return jdbc.update(insertCabGabMasterSql, params)
    }

    /**
     * CABGABマスタを更新する
     *
     * @param data CABGABデータ
     * @return 更新件数
     */
    fun updateCabGabMaster(data: CabGabData): Int {
        val params = mapOf<String, Any?>(
            "cabgabId" to data.cabgabId,
            "cabgabContent" to data.cabgabContent,
            "cabgabAnswer1" to data.cabgabAnswer1,
            "cabgabAnswer2" to data.cabgabAnswer2,
            "cabgabAnswer3" to data.cabgabAnswer3,
            "cabgabAnswer4" to data.cabgabAnswer4,
            "cabgabCorrectAnswer" to data.cabgabCorrectAnswer,
            "cabgabCategory" to data.cabgabCategory
        )
        return jdbc.update(updateCabGabMasterSql, params)
    }

    /**
     * CABGABマスタを削除する
     *
     * @param data CABGABデータ
     * @return 削除件数
     */
    fun deleteCabGabMaster(data: CabGabData): Int {
        val params = mapOf<String, Any?>(
            "cabgabId" to data.cabgabId
        )
        return jdbc.update(deleteCabGabMasterSql, params)
    }

    /**
     * CABGABマスタの件数を取得する
     *
     * @return CABGABマスタ件数
     */
    fun countCabGab(): Int {
        val params = emptyMap<String, Any>()
        return jdbc.queryForObject(getCabGabCount, params, Int::class.java) ?: 0
    }
}