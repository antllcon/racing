package com.mobility.race.data

data class Server(
    val host: String,
    val port: Int,
    val path: String

) {
    companion object {
        fun default() = Server(
            host = "158.160.184.74",
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