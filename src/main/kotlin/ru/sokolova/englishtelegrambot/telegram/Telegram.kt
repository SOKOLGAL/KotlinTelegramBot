package ru.sokolova.englishtelegrambot.telegram

import ru.sokolova.englishtelegrambot.trainer.LearnWordsTrainer
import ru.sokolova.englishtelegrambot.telegram.api.TelegramBotService
import ru.sokolova.englishtelegrambot.telegram.api.entities.Response
import ru.sokolova.englishtelegrambot.telegram.api.entities.Update

fun main(args: Array<String>) {

    val botToken = args[0]
    var lastUpdateId = 0L

    val telegramBotService = TelegramBotService(botToken)
    val trainers = HashMap<Long, LearnWordsTrainer>()

    while (true) {
        Thread.sleep(POLLING_DELAY_MS)

        val response: Response = telegramBotService.getUpdates(lastUpdateId) ?: continue
        if (response.result.isEmpty()) continue
        val sortedUpdates = response.result.sortedBy { it.updateId }
        sortedUpdates.forEach { handleUpdate(it, trainers, telegramBotService) }
        lastUpdateId = sortedUpdates.last().updateId + 1
    }
}

fun handleUpdate(
    update: Update, trainers: HashMap<Long, LearnWordsTrainer>,
    telegramBotService: TelegramBotService
) {
    val text = update.message?.text
    val chatId = update.message?.chat?.id ?: update.callbackQuery?.message?.chat?.id ?: return
    val data = update.callbackQuery?.data

    val trainer = trainers.getOrPut(chatId) { LearnWordsTrainer("$chatId.txt") }

    when {
        text?.lowercase() == COMMAND_START ->
            telegramBotService.sendMenu(chatId)

        data?.lowercase() == STATISTICS_BUTTON_PRESSED ->
            telegramBotService.sendStatistics(trainer.getStatistics(), chatId)

        data?.lowercase() == LEARN_WORD_BUTTON_PRESSED ->
            checkNextQuestionAndSend(trainer, telegramBotService, chatId)

        data?.lowercase() == CALLBACK_DATA_EXIT ->
            telegramBotService.sendMenu(chatId)

        data?.lowercase() == RESET_PRESSED -> {
            trainer.resetProgress()
            telegramBotService.sendMessage(chatId, "Прогресс сброшен")
        }

        data?.startsWith(CALLBACK_DATA_ANSWER_PREFIX) == true -> {
            val userAnswerIndex = data.substringAfter(CALLBACK_DATA_ANSWER_PREFIX).toIntOrNull() ?: -1
            val isCorrect = trainer.checkAnswer(userAnswerIndex)
            if (isCorrect) {
                telegramBotService.sendMessage(chatId, "Правильно!")
            } else {
                val message = """
              Неправильно!
              ${trainer.question?.correctAnswer?.original?.replaceFirstChar { it.titlecase() } ?: ""} 
              - это ${trainer.question?.correctAnswer?.translate}
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
const val RESET_PRESSED = "reset_clicked"
const val CALLBACK_DATA_EXIT = "exit_clicked"
const val CALLBACK_DATA_ANSWER_PREFIX = "answer_"
const val COMMAND_START = "/start"
const val POLLING_DELAY_MS = 2000L