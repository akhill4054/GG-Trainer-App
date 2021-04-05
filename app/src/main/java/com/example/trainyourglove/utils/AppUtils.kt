package com.example.trainyourglove.utils

class AppUtils {
    companion object {
        @JvmStatic
        fun formatDuration(duration: Long): String {
            val d = duration / 1000 // In seconds
            val mm = d / 60
            val ss = d % 60
            return "${if (mm < 10) "0$mm" else "$mm"}:${if (ss < 10) "0$ss" else "$ss"}"
        }
    }
}