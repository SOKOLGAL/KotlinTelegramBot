package org.example

fun main(args: Array<String>) {

    val telegramBotService = TelegramBotService(arrayOf(args[0]))
    var updateIdRegex: Regex
    var messageTextRegex: Regex
    var chatIdRegex: Regex

    while (true) {
        Thread.sleep(2000)
        val updates: String = telegramBotService.getUpdates()
        println(updates)

        updateIdRegex = "\"update_id\":(.+?),\n\"message\":".toRegex()
        val matchResult1: MatchResult? = updateIdRegex.find(updates)
        val group1: MatchGroupCollection? = matchResult1?.groups
        telegramBotService.updateId = group1?.get(1)?.value?.toInt()?.plus(1) ?: continue
        println(telegramBotService.updateId)

        messageTextRegex = "\"text\":\"(.+?)\"".toRegex()
        val matchResult: MatchResult? = messageTextRegex.find(updates)
        val group: MatchGroupCollection? = matchResult?.groups
        val text = group?.get(1)?.value
        println(text)

        chatIdRegex = "\"chat\": {\n\"id\": (.+?),\n\"first_name\"".toRegex()
        val matchResult2: MatchResult? = chatIdRegex.find(updates)
        val group2: MatchGroupCollection? = matchResult2?.groups
        val chatId = group2?.get(0)?.value?.toInt()
        println(chatId)

        telegramBotService.sendMessage(chatId, text)
    }

}