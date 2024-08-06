package com.rubio.converter.backend.service

import android.util.Log
import java.io.BufferedInputStream
import java.io.IOException
import java.io.InputStream
import java.net.URL


object DebugService  {

    @JvmStatic
    fun send(text: String) {
        Log.i("TelegramService", "Sending message $text")

        val thread = Thread(Runnable {
            try {
                var urlString = "https://api.telegram.org/bot%s/sendMessage?chat_id=%s&text=%s"

                // Add Telegram token
                // I am kind of hoping that I don't need to recreate this.
                val apiToken = "7403190036:AAGnnVB7GIUfaz8_l6UTufZ6CLLTLw6HDg4"

                //Add chatId (given chatId is fake)
                val chatId = "1034380614"

                urlString = String.format(urlString, apiToken, chatId, text)

                try {
                    val url = URL(urlString)
                    val conn = url.openConnection()
                    val `is`: InputStream = BufferedInputStream(conn.getInputStream())
                } catch (e: IOException) {
                    Log.e("DebugService", "[1] Error sending telegram message", e)
                }
            } catch (e: Exception) {
                Log.e("DebugService", "[2] Error sending telegram message", e)
            }
        })
        thread.start()
    }
}