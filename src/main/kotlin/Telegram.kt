package org.example

fun main(args: Array<String>) {

    val botToken = args[0]
    val telegramBotService = TelegramBotService(botToken)
    var updateIdRegex: Regex
    var messageTextRegex: Regex
    var chatIdRegex: Regex
    var updateId: Int = 0

    while (true) {
        Thread.sleep(2000)
        val updates: String = telegramBotService.getUpdates(updateId)
        println(updates)
        updateIdRegex = "\"update_id\":(.+?),\n\"message\":".toRegex()
        val matchResult1: MatchResult? = updateIdRegex.find(updates)
        val group1: MatchGroupCollection? = matchResult1?.groups
        updateId = group1?.get(1)?.value?.toInt()?.plus(1) ?: continue

        messageTextRegex = "\"text\":\"(.+?)\"".toRegex()
        val matchResult: MatchResult? = messageTextRegex.find(updates)
        val group: MatchGroupCollection? = matchResult?.groups
        val text = group?.get(1)?.value

        chatIdRegex = """},"chat":\{"id":(\d+),"first_name"""".toRegex()
        val matchResult2: MatchResult? = chatIdRegex.find(updates)
        val group2: MatchGroupCollection? = matchResult2?.groups
        val chatId = group2?.get(1)?.value?.toIntOrNull()

        if (chatId != null && text != null) {
            if (text.length in 1..4096) {
                telegramBotService.sendMessage(chatId, text)
            }
        }
    }

}