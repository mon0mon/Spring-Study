package xyz.mon0mon.springboottogglzaspect

import org.springframework.data.repository.CrudRepository

interface EmployeeRepository: CrudRepository<Employee, Long>
