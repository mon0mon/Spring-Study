package xyz.mon0mon.chatsample.domain.support.extension

import org.springframework.data.repository.CrudRepository

fun <T, ID : Any> CrudRepository<T, ID>.findByIdOrThrow(id: ID): T = findById(id).get()
