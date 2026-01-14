package jp.sabakan.mirai.service

import jp.sabakan.mirai.MessageConfig
import jp.sabakan.mirai.data.AnsweredCabGabData
import jp.sabakan.mirai.data.AnsweredSpiData
import jp.sabakan.mirai.data.SpiData
import jp.sabakan.mirai.entity.SpiEntity
import jp.sabakan.mirai.repository.SpiRepository
import jp.sabakan.mirai.request.CabGabRequest
import jp.sabakan.mirai.request.SpiRequest
import jp.sabakan.mirai.response.CabGabResponse
import jp.sabakan.mirai.response.SpiResponse
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Service
class SpiService {
    @Autowired
    lateinit var spiRepository: SpiRepository

    /**
     * SPI質問を登録する
     *
     * @param request SPIリクエスト
     * @return SPIレスポンス
     */
    fun insertSpi(request: SpiRequest): SpiResponse {
        //リクエストからデータ変換
        val data = SpiData().apply {
            spiId = toCreateId()
            spiContent = request.spiContent
            spiAnswer1 = request.spiAnswer1
            spiAnswer2 = request.spiAnswer2
            spiAnswer3 = request.spiAnswer3
            spiAnswer4 = request.spiAnswer4
            spiCorrectAnswer = request.spiCorrectAnswer
            spiCategory = request.spiCategory
        }

        //リポジトリへ登録処理
        val insertCount = spiRepository.insertSpi(data)

        // 登録結果を確認
        if (insertCount == 0) {
            return SpiResponse().apply {
                message = MessageConfig.SPI_INSERT_FAILED
            }
        }

        //結果を返す
        return SpiResponse().apply {
            message = MessageConfig.SPI_INSERT_SUCCESS
        }
    }

    // #TODO rateとresultを分割する必要がある
    /**
     * SPI回答を登録する
     *
     * @param request SPIリクエスト
     * @return SPIレスポンス
     */
    @Transactional
    fun insertSpiAnswer(request: SpiRequest): SpiResponse {
        //リクエストからデータ変換
        val data = AnsweredSpiData()
        data.spiResultId = toCreateId()
        data.spiId = request.spiId
        data.userId = request.userId
        data.userAnswer = request.userAnswer
        data.correctAnswer = request.correctAnswer

        //リポジトリへ登録処理
        val update = spiRepository.insertSpiAnswer(data)

        // 登録結果を確認
        if (update == 0) {
            return SpiResponse().apply {
                message = null
            }
        }

        // パラメータマップの作成
        val rateUpdate = spiRepository.updateSpiAnswerRate(data)

        // 登録結果を確認
        if (rateUpdate == 0) {
            val rateInsertCount  = spiRepository.insertSpiAnswerRate(data)

            if (rateInsertCount == 0) {
                throw RuntimeException("Failed to insert SPI answer.")
            }
        } else if (rateUpdate == 1) {
            // 多分ありえないエラー
            throw IllegalStateException("Failed to update SPI answer.")
        }

        //結果を返す
        return SpiResponse().apply {
            message = null
        }
    }

    /**
     * 指定カテゴリーのSPI質問を取得する
     *
     * @param request SPIリクエスト
     * @return SPIレスポンス
     */
    fun getSpi(request: SpiRequest): SpiResponse {
        //リクエストからデータ変換
        val data = SpiData()
        data.spiCategory = request.spiCategory

        //リポジトリへ問い合わせ
        val table: List<Map<String, Any?>> = spiRepository.getSpi(data)
        val list: List<SpiEntity> = tableToListEntity(table)

        //結果を返す
        return SpiResponse().apply {
            spis = list
            message = null
        }
    }

    /**
     * SPI質問リストを取得する
     *
     * @param request SPIリクエスト
     * @return SPIレスポンス
     */
    fun getSpiList(request: SpiRequest): SpiResponse {
        //リクエストからデータ変換
        val data = AnsweredSpiData()
        data.userId = request.userId

        //リポジトリへ問い合わせ
        val table: List<Map<String, Any?>> = spiRepository.getSpiList(data)
        val list: List<SpiEntity> = tableToListEntity(table)

        //結果を返す
        return SpiResponse().apply {
            spis = list
            message = null
        }
    }

    /**
     * SPI質問を削除する
     *
     * @param request SPIリクエスト
     * @return SPIレスポンス
     */
    fun deleteSpi(request: SpiRequest): SpiResponse {
        //リクエストからデータ変換
        val data = AnsweredSpiData()
        data.spiId = request.spiId
        data.userId = request.userId

        //リポジトリへ削除処理
        val deleteCount = spiRepository.deleteSpi(data)

        // 結果を返す
        return if (deleteCount == 0) {
            SpiResponse().apply {
                message = MessageConfig.SPI_DELETE_FAILED
            }
        } else {
            SpiResponse().apply {
                message = MessageConfig.SPI_DELETE_SUCCESS
            }
        }
    }

    /**
     * ユーザのSPI回答を削除する
     *
     * @param request SPIリクエスト
     * @return SPIレスポンス
     */
    fun deleteSpiByUser(request: SpiRequest): SpiResponse {
        //リクエストからデータ変換
        val data = AnsweredSpiData()
        data.userId = request.userId

        //リポジトリへ削除処理
        val deleteCount = spiRepository.deleteSpiByUser(data)

        // 結果を返す
        return if (deleteCount == 0) {
            SpiResponse().apply {
                message = MessageConfig.SPI_DELETE_FAILED
            }
        } else {
            SpiResponse().apply {
                message = MessageConfig.SPI_DELETE_SUCCESS
            }
        }
    }

    /**
     * 新しいSPI IDを生成する
     *
     * @return 新しいSPI ID
     */
    private fun toCreateId(): String {
        // UUIDを生成
        val uuid = UUID.randomUUID().toString()

        // 新しいSPI IDを作成
        return "S$uuid"
    }

    /**
     * テーブルデータをSpiEntityリストに変換する
     *
     * @param table テーブルデータ
     * @return SpiEntityリスト
     */
    fun tableToListEntity(table: List<Map<String, Any?>>): List<SpiEntity> {
        val list: MutableList<SpiEntity> = mutableListOf()
        for (row in table) {
            val entity = SpiEntity().apply {
                spiId = row["spi_id"] as String?
                spiContent = row["spi_content"] as String?
                spiAnswer1 = row["spi_answer_1"] as String?
                spiAnswer2 = row["spi_answer_2"] as String?
                spiAnswer3 = row["spi_answer_3"] as String?
                spiAnswer4 = row["spi_answer_4"] as String?
                spiCorrectAnswer = (row["spi_correct_answer"] as Number?)?.toInt()
                spiCategory = row["spi_category"] as String?
            }
            list.add(entity)
        }
        return list
    }
}