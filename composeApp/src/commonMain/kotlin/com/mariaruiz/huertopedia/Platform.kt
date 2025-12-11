package com.mariaruiz.huertopedia

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform