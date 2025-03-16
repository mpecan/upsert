package si.pecan.upsert

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class UpsertApplication

fun main(args: Array<String>) {
    runApplication<UpsertApplication>(*args)
}
