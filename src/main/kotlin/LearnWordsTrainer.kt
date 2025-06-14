package org.example

import java.io.File

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