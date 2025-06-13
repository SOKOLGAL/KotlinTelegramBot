package org.example

import java.io.File

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

    val trainer = LearnWordsTrainer()
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
                    } else {
                        println(question.asConsoleString())
                    }
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

data class Statistics(
    val totalCount: Int,
    var learnedCount: Int,
    val percent: Int,
)

data class Question(
    val variants: List<Word>,
    val correctAnswer: Word,
)

class LearnWordsTrainer {
    private val dictionary: List<Word> = loadDictionary()
    private var question: Question? = null

    fun getStatistics(): Statistics {
        val learnedCount = dictionary.filter { (it.correctAnswersCount ?: 0) >= NUMBER_CORRECT_ANSWERS }.size
        val totalCount = dictionary.size
        val percent = learnedCount * PERCENT / totalCount
        return Statistics(totalCount, learnedCount, percent)
    }

    fun getNextQuestion(): Question? {
        val notLearnedList = dictionary.filter { (it.correctAnswersCount ?: 0) < NUMBER_CORRECT_ANSWERS }
        if (notLearnedList.isEmpty()) return null
        val questionWord = notLearnedList.shuffled().take(NUMBER_OF_UNLEARNED_WORDS_TO_SHOW).shuffled()
        val correctAnswer = questionWord.random()
        question = Question(
            variants = questionWord,
            correctAnswer = correctAnswer,
        )
        return question
    }

    fun checkAnswer(userAnswerInput: Int?): Boolean {
        return question?.let {
            val correctAnswerId = it.variants.indexOf(it.correctAnswer)
            if (correctAnswerId == userAnswerInput) {
                it.correctAnswer.correctAnswersCount = (it.correctAnswer.correctAnswersCount ?: 0) + 1
                saveDictionary(dictionary)
                true
            } else {
                false
            }
        } ?: false
    }

    private fun loadDictionary(): List<Word> {
        val dictionary: MutableList<Word> = mutableListOf()
        val wordsFile: File = File("words.txt")
        wordsFile.readLines().forEach {
            val splitLine = it.split("|")
            dictionary.add(Word(splitLine[0], splitLine[1], splitLine[2].toIntOrNull() ?: 0))
        }
        return dictionary
    }

    private fun saveDictionary(dictionary: List<Word>) {
        val wordsFile: File = File("words.txt")
        wordsFile.writeText("")
        for (word in dictionary) {
            wordsFile.appendText("${word.original}|${word.translate}|${word.correctAnswersCount}\n")
        }
    }
}

const val NUMBER_CORRECT_ANSWERS = 3
const val PERCENT = 100
const val NUMBER_OF_UNLEARNED_WORDS_TO_SHOW = 4