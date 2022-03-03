package utilities

import android.util.Log

actual class Logger actual constructor(private val tag: String) {
    actual fun log(message: String) {
        Log.i(tag, message)
    }
}
