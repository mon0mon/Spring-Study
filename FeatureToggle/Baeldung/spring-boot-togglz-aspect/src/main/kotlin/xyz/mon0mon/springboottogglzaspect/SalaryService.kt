package xyz.mon0mon.springboottogglzaspect

import org.springframework.stereotype.Service

@Service
class SalaryService(
    val employeeRepository: EmployeeRepository
) {
    @FeatureAssociation(value = MyFeatures.EMPLOYEE_MANAGEMENT_FEATURE)
    fun increaseSalary(id: Long) {
        val employee = employeeRepository.findById(id).orElseThrow()
        employee.salary += employee.salary * 0.1
        employeeRepository.save(employee)
    }
}
