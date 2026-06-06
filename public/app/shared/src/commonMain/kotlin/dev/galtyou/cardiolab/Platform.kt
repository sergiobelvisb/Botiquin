package dev.galtyou.cardiolab

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform