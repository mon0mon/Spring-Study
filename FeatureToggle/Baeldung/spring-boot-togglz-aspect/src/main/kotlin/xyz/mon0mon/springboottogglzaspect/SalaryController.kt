package xyz.mon0mon.springboottogglzaspect

import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
class SalaryController(
    val salaryService: SalaryService
) {
    @PostMapping("/increaseSalary")
    fun increaseSalary(@RequestParam id: Long) {
        salaryService.increaseSalary(id)
    }
}
