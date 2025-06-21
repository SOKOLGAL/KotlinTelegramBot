package org.example

import java.net.URI
import java.net.URLEncoder
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.nio.charset.StandardCharsets

class TelegramBotService(private val botToken: String) {
    companion object {
        const val BASE_URL: String = "https://api.telegram.org"
    }

    private var client: HttpClient = HttpClient.newBuilder().build()

    fun getUpdates(updateId: Int): String {
        val urlGetUpdates = "$BASE_URL/bot$botToken/getUpdates?offset=$updateId"
        val request: HttpRequest = HttpRequest.newBuilder().uri(URI.create(urlGetUpdates)).build()
        val response: HttpResponse<String> = client.send(request, HttpResponse.BodyHandlers.ofString())
        return response.body()
    }

    fun sendMessage(chatId: Int, text: String): String {
        val encoded = URLEncoder.encode(
            text,
            StandardCharsets.UTF_8
        )
        println(encoded)
        val urlSendMessage = "$BASE_URL/bot$botToken/sendMessage?chat_id=$chatId&text=$encoded"
        val request: HttpRequest = HttpRequest.newBuilder().uri(URI.create(urlSendMessage)).build()
        val response: HttpResponse<String> = client.send(request, HttpResponse.BodyHandlers.ofString())
        return response.body()
    }

    fun sendMenu(chatId: Int): String {
        val urlSendMessage = "$BASE_URL/bot$botToken/sendMessage"
        val sendMenuBody = """
          {
              "chat_id": $chatId,
              "text": "Основное меню",
              "reply_markup": {
                  "inline_keyboard": [
                      [
                          {
                              "text": "Изучить слова",
                              "callback_data": "learn_words_clicked"
                          },
                          {
                              "text": "Статистика",
                              "callback_data": "statistics_clicked"
                          }
                      ]
                  ]
              }
          }
      """.trimIndent()
        val request: HttpRequest = HttpRequest.newBuilder().uri(URI.create(urlSendMessage))
            .header("Content-Type", "application/json")
            .POST(HttpRequest.BodyPublishers.ofString(sendMenuBody))
            .build()
        val response: HttpResponse<String> = client.send(request, HttpResponse.BodyHandlers.ofString())
        return response.body()
    }

    fun sendStatistics(statistics: Statistics, chatId: Int): String {
        val text = "Выучено ${statistics.learnedCount} из ${statistics.totalCount} слов | " +
                "${statistics.percent}%\n"
        val encoded = URLEncoder.encode(
            text,
            StandardCharsets.UTF_8
        )
        val urlSendMessage = "$BASE_URL/bot$botToken/sendMessage?chat_id=$chatId&text=$encoded"
        val request: HttpRequest = HttpRequest.newBuilder().uri(URI.create(urlSendMessage)).build()
        val response: HttpResponse<String> = client.send(request, HttpResponse.BodyHandlers.ofString())
        return response.body()
    }

    fun sendQuestion(chatId: Int, question: Question): String {
        val optionsList = question.variants.mapIndexed { index, word ->
            word.translate
        }
        val urlSendMessage = "$BASE_URL/bot$botToken/sendMessage"
        val sendQuestionBody = """
{
"chat_id": $chatId,
"text": "${question.correctAnswer.original}",
"reply_markup": {
  "inline_keyboard": [
    [
      {
        "text": "${optionsList[0]}",
        "callback_data": "$CALLBACK_DATA_ANSWER_PREFIX${optionsList.indexOf(optionsList[0])}"
      },
      {
        "text": "${optionsList[1]}",
        "callback_data": "$CALLBACK_DATA_ANSWER_PREFIX${optionsList.indexOf(optionsList[1])}"
      },
      {
        "text": "${optionsList[2]}",
        "callback_data": "$CALLBACK_DATA_ANSWER_PREFIX${optionsList.indexOf(optionsList[2])}"
      },
      {
        "text": "${optionsList[3]}",
        "callback_data": "$CALLBACK_DATA_ANSWER_PREFIX${optionsList.indexOf(optionsList[3])}"
      }
    ]
  ]
}
}
""".trimIndent()
        val request: HttpRequest = HttpRequest.newBuilder().uri(URI.create(urlSendMessage))
            .header("Content-type", "application/json")
            .POST(HttpRequest.BodyPublishers.ofString(sendQuestionBody))
            .build()
        val response: HttpResponse<String> = client.send(request, HttpResponse.BodyHandlers.ofString())
        return response.body()
    }
}