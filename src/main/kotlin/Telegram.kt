package org.example

fun main(args: Array<String>) {

    val botToken = args[0]
    val telegramBotService = TelegramBotService(botToken)
    val updateIdRegex: Regex = "\"update_id\":(.+?),\n\"message\":".toRegex()
    val messageTextRegex: Regex = "\"text\":\"(.+?)\"".toRegex()
    val chatIdRegex: Regex = """},"chat":\{"id":(\d+),"first_name"""".toRegex()
    var updateId: Int = 0

    while (true) {
        Thread.sleep(2000)
        val updates: String = telegramBotService.getUpdates(updateId)
        println(updates)
        val updateIdMatchResult: MatchResult? = updateIdRegex.find(updates)
        val updateIdMatchGroup: MatchGroupCollection? = updateIdMatchResult?.groups
        updateId = updateIdMatchGroup?.get(1)?.value?.toInt()?.plus(1) ?: continue

        val textMatchResult: MatchResult? = messageTextRegex.find(updates)
        val textMatchGroup: MatchGroupCollection? = textMatchResult?.groups
        val text = textMatchGroup?.get(1)?.value

        val chatIdMatchResult: MatchResult? = chatIdRegex.find(updates)
        val chatIdMatchGroup: MatchGroupCollection? = chatIdMatchResult?.groups
        val chatId = chatIdMatchGroup?.get(1)?.value?.toIntOrNull() ?: continue

        if (text != null) {
            if (text.length in MINIMUM_NUMBER_OF_CHARACTERS_IN_TEXT..MAXIMUM_NUMBER_OF_CHARACTERS_IN_TEXT) {
                telegramBotService.sendMessage(chatId, text)
            }
        }
    }

}

const val MINIMUM_NUMBER_OF_CHARACTERS_IN_TEXT = 1
const val MAXIMUM_NUMBER_OF_CHARACTERS_IN_TEXT = 4096