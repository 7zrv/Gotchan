package com.gotchan

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class GotchanApplication

fun main(args: Array<String>) {
    runApplication<GotchanApplication>(*args)
}
