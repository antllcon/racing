package com.mobility.race.data

data class Server(
    val host: String,
    val port: Int,
    val path: String

) {
    companion object {
        fun default() = Server(
            host = "51.250.32.73",
            port = 8080,
            path = "/"
        )

        fun securityDefault() = Server(
            host = "thecorpus.ru",
            port = 443,
            path = "/"
        )
    }
}