package ru.sokolova.englishtelegrambot.telegram

import ru.sokolova.englishtelegrambot.trainer.LearnWordsTrainer
import ru.sokolova.englishtelegrambot.telegram.api.TelegramBotService
import ru.sokolova.englishtelegrambot.telegram.api.entities.Response

fun main(args: Array<String>) {

    val botToken = args[0]
    var updateId = 0L

    val telegramBotService = TelegramBotService(botToken)
    val trainer = LearnWordsTrainer()

    while (true) {
        Thread.sleep(2000)

        val response: Response = telegramBotService.getUpdates(updateId) ?: continue
        val updates = response.result
        val firstUpdate = updates.firstOrNull() ?: continue
        updateId = firstUpdate.updateId.plus(1)

        val text = firstUpdate.message?.text
        val chatId = firstUpdate.message?.chat?.id ?: firstUpdate.callbackQuery?.message?.chat?.id
        val data = firstUpdate.callbackQuery?.data

        if (text?.lowercase() == COMMAND_START) {
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
const val COMMAND_START = "/start"