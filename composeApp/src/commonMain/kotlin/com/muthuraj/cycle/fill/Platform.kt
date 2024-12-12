package com.muthuraj.cycle.fill

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform

expect fun isDebug(): Boolean