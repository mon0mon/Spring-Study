package xyz.mon0mon.springboottogglzaspect

import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id

@Entity
class Employee(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long?,
    var salary: Double
)
