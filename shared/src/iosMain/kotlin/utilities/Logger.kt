package utilities

import platform.Foundation.NSLog

actual class Logger actual constructor(private val tag: String) {
    actual fun log(message: String) {
        NSLog("[$tag] $message")
    }
}
