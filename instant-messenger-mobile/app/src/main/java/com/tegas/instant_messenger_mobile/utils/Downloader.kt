package com.tegas.instant_messenger_mobile.utils

interface Downloader {
    fun downloadFile(url: String): Long
}