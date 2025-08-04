package xyz.mon0mon.projectreactorguide.core

import org.junit.jupiter.api.Test
import reactor.core.publisher.Flux
import java.util.concurrent.atomic.AtomicLong

/**
 * Project Reactor Reference Guide
 *
 * [Programmatically creating a sequence](https://projectreactor.io/docs/core/release/reference/coreFeatures/programmatically-creating-sequence.html)
 */
class CreatingASequence {
    
    @Test
    fun `Synchronous generate`() {
        val flux = Flux.generate(
            { 0 },
            { state, sink ->
                sink.next("3 x $state = ${3 * state}")
                if (state == 10) sink.complete()
                state + 1
            }
        )

        flux.subscribe(::println)
    }

    @Test
    fun `Synchronous generate2 - mutable state`() {
        val flux = Flux.generate(
            { AtomicLong() },
            { state, sink ->
                val i = state.andIncrement
                sink.next("3 x $i = ${3 * i}")
                if (i == 10L) sink.complete()
                state
            }
        )

        flux.subscribe(::println)
    }

    @Test
    fun `Synchronous generate3 - consumer`() {
        val flux = Flux.generate(
            { AtomicLong() },
            { state, sink ->
                val i = state.andIncrement
                sink.next("3 x $i = ${3 * i}")
                if (i == 10L) sink.complete()
                state
            },
            { println("state: $it") }
        )

        flux.subscribe(::println)
    }
}
