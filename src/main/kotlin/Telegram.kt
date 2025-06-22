package org.example

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

@Serializable
data class Update(
    @SerialName("update_id")
    val updateId: Long,
    @SerialName("message")
    val message: Message? = null,
    @SerialName("callback_query")
    val callbackQuery: CallbackQuery? = null,
)

@Serializable
data class Response(
    @SerialName("result")
    val result: List<Update>,
)

@Serializable
data class Message(
    @SerialName("text")
    val text: String,
    @SerialName("chat")
    val chat: Chat,
)

@Serializable
data class CallbackQuery(
    @SerialName("data")
    val data: String,
    @SerialName("message")
    val message: Message? = null,
)

@Serializable
data class Chat(
    @SerialName("id")
    val id: Long,
)

@Serializable
data class SendMessageRequest(
    @SerialName("chat_id")
    val chatId: Long?,
    @SerialName("text")
    val text: String,
    @SerialName("reply_markup")
    val replyMarkup: ReplyMarkup? = null,
)

@Serializable
data class ReplyMarkup(
    @SerialName("inline_keyboard")
    val inlineKeyboard: List<List<InlineKeyboard>>,
)

@Serializable
data class InlineKeyboard(
    @SerialName("callback_data")
    val callbackData: String,
    @SerialName("text")
    val text: String,
)

@Serializable
data class GetUpdates(
    @SerialName("update_id")
    val updateId: Long,
)

fun main(args: Array<String>) {

    val botToken = args[0]
    var updateId: Long = 0L

    val telegramBotService = TelegramBotService(botToken)

    val json = Json {
        ignoreUnknownKeys = true
    }

    val trainer = LearnWordsTrainer()

    while (true) {
        Thread.sleep(2000)
        val responseString: String = telegramBotService.getUpdates(updateId)
        println(responseString)
        val response: Response = json.decodeFromString(responseString)
        val updates = response.result
        val firstUpdate = updates.firstOrNull() ?: continue
        updateId = firstUpdate.updateId.plus(1)

        val text = firstUpdate.message?.text
        val chatId = firstUpdate.message?.chat?.id ?: firstUpdate.callbackQuery?.message?.chat?.id
        val data = firstUpdate.callbackQuery?.data

        if (text?.lowercase() == "/start") {
            telegramBotService.sendMenu(chatId)
        }
        if (data?.lowercase() == STATISTICS_BUTTON_PRESSED) {
            telegramBotService.sendStatistics(trainer.getStatistics(), chatId)
        }
        if (data?.lowercase() == LEARN_WORD_BUTTON_PRESSED) {
            checkNextQuestionAndSend(trainer, telegramBotService, chatId)
        }
        if (data?.startsWith(CALLBACK_DATA_ANSWER_PREFIX) == true) {
            val userAnswerIndex = data.substringAfter(CALLBACK_DATA_ANSWER_PREFIX).toIntOrNull() ?: -1
            val isCorrect = trainer.checkAnswer(userAnswerIndex)
            if (isCorrect) {
                telegramBotService.sendMessage(chatId, "Правильно!")
            } else {
                val message = """
               Неправильно!
               ${trainer.question?.correctAnswer?.original} - это ${trainer.question?.correctAnswer?.translate}
               """.trimIndent()
                telegramBotService.sendMessage(chatId, message)
            }
            checkNextQuestionAndSend(trainer, telegramBotService, chatId)
        }
    }
}

fun checkNextQuestionAndSend(
    trainer: LearnWordsTrainer,
    telegramBotService: TelegramBotService,
    chatId: Long?,
) {
    val nextQuestion = trainer.getNextQuestion()
    if (nextQuestion == null) {
        telegramBotService.sendMessage(chatId, "Все слова в словаре выучены")
    } else {
        telegramBotService.sendQuestion(chatId, nextQuestion)
    }
}

const val STATISTICS_BUTTON_PRESSED = "statistics_clicked"
const val LEARN_WORD_BUTTON_PRESSED = "learn_words_clicked"
const val CALLBACK_DATA_ANSWER_PREFIX = "answer_"