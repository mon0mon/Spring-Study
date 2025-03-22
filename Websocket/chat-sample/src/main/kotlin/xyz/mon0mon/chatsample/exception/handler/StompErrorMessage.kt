package xyz.mon0mon.chatsample.exception.handler

import org.springframework.http.HttpStatus

class StompErrorMessage (
    val message: String,
    status: HttpStatus
) {
    val status: StompErrorMessageStatusData = StompErrorMessageStatusData(status)
}

class StompErrorMessageStatusData (
    status: HttpStatus
) {
    val code: Int = status.value()
    val message: String = status.reasonPhrase
}
