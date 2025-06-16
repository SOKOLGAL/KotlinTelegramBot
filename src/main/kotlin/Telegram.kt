package org.example

import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse

fun main(args: Array<String>) {

    val botToken = args[0]
    var updateId = 0

    while (true) {
        Thread.sleep(2000)
        val updates: String = getUpdates(botToken, updateId)
        println(updates)

        val updateIdRegex = "\"update_id\":(.+?),\n\"message\":".toRegex()
        val matchResult1: MatchResult? = updateIdRegex.find(updates)
        val group1: MatchGroupCollection? = matchResult1?.groups
        updateId = group1?.get(1)?.value?.toInt()?.plus(1) ?: continue

        val messageTextRegex: Regex = "\"text\":\"(.+?)\"".toRegex()
        val matchResult: MatchResult? = messageTextRegex.find(updates)
        val group: MatchGroupCollection? = matchResult?.groups
        val text = group?.get(1)?.value
        println(text)
    }

}

fun getUpdates(botToken: String, updateId: Int): String {
    val urlGetUpdates = "https://api.telegram.org/bot$botToken/getUpdates?offset=$updateId"
    val client: HttpClient = HttpClient.newBuilder().build()
    val request: HttpRequest = HttpRequest.newBuilder().uri(URI.create(urlGetUpdates)).build()
    val response: HttpResponse<String> = client.send(request, HttpResponse.BodyHandlers.ofString())
    return response.body()
}