package com.example.trainyourglove

import android.app.Application
import android.util.Log
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.example.trainyourglove.data.db.AppDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File

/**
 * Instrumented test, which will execute on an Android device.
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
private const val TAG = "ExampleInstrumentedTest"

@RunWith(AndroidJUnit4::class)
class ExampleInstrumentedTest {
    @Test
    fun useAppContext() {
        // Context of the app under test.
        val dao =
            AppDatabase.getInstance(InstrumentationRegistry.getInstrumentation().context as Application)
                .gesturesDao()
        CoroutineScope(Dispatchers.Unconfined).launch {
            val gestures = dao.getRecordedGestures()
            val gesture = gestures[0]
            val rf = File(gesture.dataFileUri)
            Log.d(TAG, "useAppContext: ${rf.exists()}")
        }
    }
}