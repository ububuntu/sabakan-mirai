package jp.sabakan.mirai

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class MiraiApplication

fun main(args: Array<String>) {
	runApplication<MiraiApplication>(*args)
}
