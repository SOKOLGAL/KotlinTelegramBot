package ru.sokolova.englishtelegrambot.telegram.api

import kotlinx.serialization.json.Json
import ru.sokolova.englishtelegrambot.telegram.*
import ru.sokolova.englishtelegrambot.telegram.api.entities.InlineKeyboard
import ru.sokolova.englishtelegrambot.telegram.api.entities.ReplyMarkup
import ru.sokolova.englishtelegrambot.telegram.api.entities.Response
import ru.sokolova.englishtelegrambot.telegram.api.entities.SendMessageRequest
import ru.sokolova.englishtelegrambot.trainer.modal.Question
import ru.sokolova.englishtelegrambot.trainer.modal.Statistics
import java.io.IOException
import java.net.URI
import java.net.URLEncoder
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.nio.charset.StandardCharsets
import java.time.Duration.ofSeconds
import kotlin.time.Duration

class TelegramBotService(private val botToken: String) {

    private val client: HttpClient = HttpClient.newBuilder()
        .connectTimeout(Duration.run { ofSeconds(10) })
        .version(HttpClient.Version.HTTP_2)
        .build()
    private val json: Json = Json { ignoreUnknownKeys = true }

    fun sendRequestWithRetry(
        request: HttpRequest,
        maxRetries: Int = 3,
        delayMillis: Long = 1000L
    ): HttpResponse<String> {
        var lastException: Exception? = null

        repeat(maxRetries) { attempt ->
            try {
                val response = client.send(request, HttpResponse.BodyHandlers.ofString())
                return response
            } catch (e: IOException) {
                lastException = e
                println("Попытка ${attempt + 1} провалена: ${e.message}. Повтор через $delayMillis мс")
                Thread.sleep(delayMillis)
            }
        }

        throw lastException ?: RuntimeException("Неизвестная ошибка при отправке запроса")
    }

    fun getUpdates(updateId: Long): Response? {
        val urlGetUpdates = "$BASE_URL/bot$botToken/getUpdates?offset=$updateId"
        val request: HttpRequest = HttpRequest.newBuilder().uri(URI.create(urlGetUpdates)).build()
        val response: HttpResponse<String> = client.send(request, HttpResponse.BodyHandlers.ofString())
        val responseString = response.body()
        println(responseString)
        return runCatching { json.decodeFromString<Response>(responseString) }.getOrNull()
    }

    fun sendMessage(chatId: Long?, text: String): String {
        val urlSendMessage = "$BASE_URL/bot$botToken/sendMessage"
        val requestBody = SendMessageRequest(
            chatId = chatId,
            text = text,
        )
        val requestBodyString = json.encodeToString(requestBody)
        val request: HttpRequest = HttpRequest.newBuilder().uri(URI.create(urlSendMessage))
            .header("Content-type", "application/json")
            .POST(HttpRequest.BodyPublishers.ofString(requestBodyString))
            .build()
        val response: HttpResponse<String> = client.send(request, HttpResponse.BodyHandlers.ofString())
        return response.body()
    }

    fun sendMenu(chatId: Long?): String {
        val urlSendMessage = "$BASE_URL/bot$botToken/sendMessage"
        val requestBody = SendMessageRequest(
            chatId = chatId,
            text = "Основное меню",
            replyMarkup = ReplyMarkup(
                listOf(
                    listOf(
                        InlineKeyboard(text = "Изучить слова", callbackData = LEARN_WORD_BUTTON_PRESSED),
                        InlineKeyboard(text = "Статистика", callbackData = STATISTICS_BUTTON_PRESSED)
                    ),
                    listOf(
                        InlineKeyboard(text = "Сбросить прогресс", callbackData = RESET_PRESSED)
                    )
                )
            )
        )
        val requestBodyString = json.encodeToString(requestBody)
        val request: HttpRequest = HttpRequest.newBuilder().uri(URI.create(urlSendMessage))
            .header("Content-Type", "application/json")
            .POST(HttpRequest.BodyPublishers.ofString(requestBodyString))
            .build()
        val response: HttpResponse<String> = client.send(request, HttpResponse.BodyHandlers.ofString())
        return response.body()
    }

    fun sendStatistics(statistics: Statistics, chatId: Long?): String {
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

    fun sendQuestion(chatId: Long?, question: Question): String {
        val urlSendMessage = "$BASE_URL/bot$botToken/sendMessage"
        val requestBody = SendMessageRequest(
            chatId = chatId,
            text = question.correctAnswer.original.replaceFirstChar { it.titlecase() }
                    + " ${question.correctAnswer.transcription}",
            replyMarkup = ReplyMarkup(
                question.variants.mapIndexed { index, word ->
                    listOf(
                        InlineKeyboard(
                            text = word.translate.replaceFirstChar { it.titlecase() },
                            callbackData = "$CALLBACK_DATA_ANSWER_PREFIX$index"
                        )
                    )
                } + listOf(
                    listOf(
                        InlineKeyboard(
                            text = "Выход",
                            callbackData = CALLBACK_DATA_EXIT
                        )
                    )
                )
            )
        )
        val requestBodyString = json.encodeToString(requestBody)
        val request: HttpRequest = HttpRequest.newBuilder().uri(URI.create(urlSendMessage))
            .header("Content-type", "application/json")
            .POST(HttpRequest.BodyPublishers.ofString(requestBodyString))
            .build()
        val response: HttpResponse<String> = client.send(request, HttpResponse.BodyHandlers.ofString())
        return response.body()
    }

    companion object {
        const val BASE_URL: String = "https://api.telegram.org"
    }
}