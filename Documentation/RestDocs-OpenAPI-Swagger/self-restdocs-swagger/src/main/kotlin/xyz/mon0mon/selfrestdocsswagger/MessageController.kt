package xyz.mon0mon.selfrestdocsswagger

import io.github.oshai.kotlinlogging.KLogger
import io.github.oshai.kotlinlogging.KotlinLogging
import jakarta.annotation.PostConstruct
import org.springframework.http.ResponseEntity
import org.springframework.util.StringUtils
import org.springframework.web.bind.annotation.*
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicLong

private val logger: KLogger = KotlinLogging.logger {}

@RestController
@RequestMapping("/api/message")
class MessageController {
    val seq: AtomicLong = AtomicLong(1)

    companion object {
        val map: ConcurrentHashMap<Long, String> = ConcurrentHashMap()
    }

    @PostConstruct
    fun init() {
        logger.info { "Init Sample Message" }

        MessageController.map[seq.getAndIncrement()] = "Hello, World!"
    }

    @GetMapping
    fun getSample(@RequestParam id: Long): ResponseEntity<SampleResponse> {
        val data = map.getOrDefault(id, null)

        require(data != null) {
            logger.error { "No Such Message" }
        }

        logger.info { "Found $id : $data" }

        val response = SampleResponse(id, data)
        return ResponseEntity.ok(response)
    }

    @PostMapping
    fun createSample(@RequestBody req: SampleRequest): ResponseEntity<SampleResponse> {
        require(StringUtils.hasText(req.message)) {
            logger.error { "Message can't be empty or null" }
        }

        val res = SampleResponse(seq.getAndIncrement(), req.message)
        map[res.id] = res.message

        logger.info { "Registered ${res.message} at ${res.id}" }
        return ResponseEntity.ok(res)
    }
}

data class SampleRequest(
    val message: String
)

data class SampleResponse(
    val id: Long,
    val message: String
)
