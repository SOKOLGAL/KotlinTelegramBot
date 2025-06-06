package org.example.stage_1

import java.io.File

fun main() {


    val wordsFile: File = File("words.txt")
    wordsFile.createNewFile()

    for (i in wordsFile.readLines()) {
        println(i)
    }

}