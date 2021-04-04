package com.example.trainyourglove

import org.junit.Test

import org.junit.Assert.*
import umich.cse.yctung.androidlibsvm.LibSVM

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class ExampleUnitTest {
    @Test
    fun addition_isCorrect() {


        assertEquals(4, 2 + 2)
    }

    @Test
    fun libSVM_isWorking() {
        val svm = LibSVM.getInstance()

    }
}