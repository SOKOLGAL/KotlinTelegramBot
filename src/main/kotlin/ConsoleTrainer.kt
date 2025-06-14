package org.example

data class Word(
    val original: String,
    val translate: String,
    var correctAnswersCount: Int? = 0,
)

fun Question.asConsoleString(): String {
    val variants = this.variants
        .mapIndexed { index: Int, word: Word -> " ${index + 1} - ${word.translate}" }
        .joinToString("\n")
    return this.correctAnswer.original + ":\n" + variants + "\n ----------\n 0 - Меню"
}

fun main() {

    val trainer = try {
        LearnWordsTrainer(3, 4)
    } catch (e: Exception) {
        println("Невозможно загрузить словарь")
        return
    }

    val greetings = """
        Меню:
        1 - Учить слова
        2 - Статистика
        0 - Выход
    """.trimIndent()

    while (true) {
        println(greetings)
        when (readlnOrNull()?.toIntOrNull()) {
            1 -> {
                while (true) {
                    val question = trainer.getNextQuestion()
                    if (question == null) {
                        println("Все слова в словаре выучены")
                        break
                    }
                    println(question.asConsoleString())
                    val userAnswerInput = readlnOrNull()?.toIntOrNull()
                    if (userAnswerInput == 0) break
                    if (trainer.checkAnswer(userAnswerInput?.minus(1))) {
                        println("Правильно!\n")
                    } else {
                        println(
                            "Неправильно! ${question.correctAnswer.original} - " +
                                    "это ${question.correctAnswer.translate}\n"
                        )
                    }
                }
            }

            2 -> {
                val statistics = trainer.getStatistics()
                println(
                    "Выучено ${statistics.learnedCount} из ${statistics.totalCount} слов | " +
                            "${statistics.percent}%\n"
                )
            }

            0 -> break
            else -> println("Введите число 1, 2 или 0")
        }
    }

}

const val PERCENT = 100