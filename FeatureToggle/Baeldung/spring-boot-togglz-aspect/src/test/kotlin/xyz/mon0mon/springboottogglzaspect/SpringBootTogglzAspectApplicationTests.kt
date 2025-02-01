package xyz.mon0mon.springboottogglzaspect

import org.junit.jupiter.api.Test
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.TestConstructor
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.transaction.annotation.Transactional
import kotlin.test.junit5.JUnit5Asserter.assertEquals
import kotlin.test.junit5.JUnit5Asserter.assertNotEquals

@SpringBootTest
@Transactional
@AutoConfigureMockMvc
@TestConstructor(autowireMode = TestConstructor.AutowireMode.ALL)
class SpringBootTogglzAspectApplicationTests(
    private val mockMvc: MockMvc,
    private val employeeRepository: EmployeeRepository
) {

    @Test
    fun `givenFeaturePropertyFalse_whenIncreaseSalary_thenNoIncrease`() {
        val emp = Employee(null, 2000.0)
        employeeRepository.save(emp)

        System.setProperty("employee.feature", "false")

        mockMvc.perform(
            post("/increaseSalary")
                .param("id", emp.id.toString())
        )
            .andExpect(status().isOk)

        val findOne = employeeRepository.findById(emp.id!!).orElseThrow()
        assertNotEquals("salary incorrect", 2200.0, emp.salary)
    }

    @Test
    fun `givenFeaturePropertyTrue_whenIncreaseSalary_thenIncrease`() {
        val emp = Employee(null, 2000.0)
        employeeRepository.save(emp)

        System.setProperty("employee.feature", "true")

        mockMvc.perform(
            post("/increaseSalary")
                .param("id", emp.id.toString())
        )
            .andExpect(status().isOk)

        val findOne = employeeRepository.findById(emp.id!!).orElseThrow()
        assertEquals("salary incorrect", 2200.0, emp.salary)
    }
}
