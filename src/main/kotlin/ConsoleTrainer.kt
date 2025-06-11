package org.example

import java.io.File

data class Word(
    val original: String,
    val translate: String,
    var correctAnswersCount: Int? = 0,
)

fun main() {

    val greetings = """
        Меню:
        1 - Учить слова
        2 - Статистика
        0 - Выход
    """.trimIndent()
    val dictionary: List<Word> = loadDictionary()

    while (true) {
        println(greetings)
        val enterMenuItemNumber = readlnOrNull()?.toIntOrNull()
        when (enterMenuItemNumber) {
            1 -> learnWords(dictionary)
            2 -> getLearnedWords(dictionary)
            0 -> break
            else -> println("Введите число 1, 2 или 0")
        }
    }

}

fun learnWords(dictionary: List<Word>) {
    val notLearnedList = dictionary.filter { (it.correctAnswersCount ?: 0) < NUMBER_CORRECT_ANSWERS }
    if (notLearnedList.isEmpty()) return println("Все слова в словаре выучены")
    val questionWords = notLearnedList.shuffled().take(NUMBER_OF_UNLEARNED_WORDS_TO_SHOW).shuffled()
    val correctAnswer = questionWords.map { it.original }[0]
    println("\n$correctAnswer:")
    var correctAnswerId: Int = 0
    questionWords.shuffled().forEachIndexed { index, word ->
        println("${index + 1} - ${word.translate}")
        for (i in questionWords) {
            if (word.original == correctAnswer)
                correctAnswerId = index + 1
        }
    }
    println("----------\n0 - Меню")
    val userAnswerInput = readlnOrNull()?.toIntOrNull()
    if (userAnswerInput == 0) return
    if (userAnswerInput == correctAnswerId) {
        println("Правильно!")
        for (i in dictionary) {
            if (i.original == correctAnswer) {
                i.correctAnswersCount = (i.correctAnswersCount ?: 0) + 1
            }
        }
        saveDictionary(dictionary)
    } else {
        for (i in questionWords) {
            if (i.original == correctAnswer)
                println("Неправильно! $correctAnswer - это ${i.translate}")
        }
    }
}

fun saveDictionary(dictionary: List<Word>) {
    val wordsFile: File = File("words.txt")
    wordsFile.writeText(dictionary.toString())
}

fun getLearnedWords(dictionary: List<Word>) {
    val totalCount = dictionary.size
    var learnedCount = 0
    dictionary.forEach {
        if ((it.correctAnswersCount ?: 0) >= NUMBER_CORRECT_ANSWERS) {
            learnedCount++
        }
    }
    val percent = learnedCount * PERCENT / totalCount
    println("Выучено $learnedCount из $totalCount | $percent\n")
}

fun loadDictionary(): List<Word> {
    val wordsFile: File = File("words.txt")
    val lines = wordsFile.readLines()
    val dictionary: MutableList<Word> = mutableListOf()

    for (line in lines) {
        val line = line.split("|")
        val word = Word(original = line[0], translate = line[1], correctAnswersCount = line[2].toIntOrNull() ?: 0)
        dictionary.add(word)
    }
    return dictionary
}

const val NUMBER_CORRECT_ANSWERS = 3
const val PERCENT = 100
const val NUMBER_OF_UNLEARNED_WORDS_TO_SHOW = 4