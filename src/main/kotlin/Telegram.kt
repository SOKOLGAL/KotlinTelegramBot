package org.example

fun main(args: Array<String>) {

    val botToken = args[0]
    var updateId: Int = 0

    val telegramBotService = TelegramBotService(botToken)

    val updateIdRegex: Regex = "\"update_id\":(.+?),".toRegex()
    val messageTextRegex: Regex = "\"text\":\"(.+?)\"".toRegex()
    val chatIdRegex: Regex = """},"chat":\{"id":(\d+),"first_name"""".toRegex()
    val dataRegex: Regex = "\"data\":\"(.+?)\"".toRegex()

    val trainer = LearnWordsTrainer()

    while (true) {
        Thread.sleep(2000)
        val updates: String = telegramBotService.getUpdates(updateId)
        println(updates)
        updateId = updateIdRegex.find(updates)?.groups?.get(1)?.value?.toIntOrNull()?.plus(1) ?: continue
        val text = messageTextRegex.find(updates)?.groups?.get(1)?.value
        val chatId = chatIdRegex.find(updates)?.groups?.get(1)?.value?.toIntOrNull() ?: continue
        val data = dataRegex.find(updates)?.groups?.get(1)?.value

        if (text?.lowercase() == "hello") {
            telegramBotService.sendMessage(chatId, text)
        }
        if (text?.lowercase() == "/start") {
            telegramBotService.sendMenu(chatId)
        }

        if (data?.lowercase() == STATISTICS_BUTTON_PRESSED) {
            telegramBotService.sendStatistics(trainer.getStatistics(), chatId)
        }
        if (data?.lowercase() == LEARN_WORD_BUTTON_PRESSED) {
            checkNextQuestionAndSend(trainer, telegramBotService, chatId)
        }
    }
}

fun checkNextQuestionAndSend(
    trainer: LearnWordsTrainer,
    telegramBotService: TelegramBotService,
    chatId: Int
) {
    val nextQuestion = trainer.getNextQuestion()
    if (nextQuestion == null) {
        telegramBotService.sendMessage(chatId, "Все слова в словаре выучены")
    } else {
        telegramBotService.sendQuestion(chatId, trainer.question)
    }
}

const val STATISTICS_BUTTON_PRESSED = "statistics_clicked"
const val LEARN_WORD_BUTTON_PRESSED = "learn_words_clicked"
const val CALLBACK_DATA_ANSWER_PREFIX = "answer_"