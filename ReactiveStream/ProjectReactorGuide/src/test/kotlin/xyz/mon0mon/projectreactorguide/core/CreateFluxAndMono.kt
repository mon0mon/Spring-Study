package xyz.mon0mon.projectreactorguide.core

import org.junit.jupiter.api.Test
import org.reactivestreams.Subscription
import reactor.core.publisher.BaseSubscriber
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

/**
 * Project Reactor Reference Guide
 *
 * [Simple Ways to Create a Flux or Mono and Subscribe to it](https://projectreactor.io/docs/core/release/reference/coreFeatures/simple-ways-to-create-a-flux-or-mono-and-subscribe-to-it.html)
 */
class CreateFluxAndMono {

    @Test
    fun `create sequence of String`() {
        val seq1 = Flux.just("foo", "bar", "foobar")

        val iterable = listOf("foo", "bar", "foobar")
        val seq2 = Flux.fromIterable(iterable)
    }

    @Test
    fun `create sequence of String by factory method`() {
        val noData = Mono.empty<String>()

        val data = Mono.just("foo")

        val numbersFromFiveToSeven = Flux.range(5, 3)
    }

    @Test
    fun `subscribe methods examples - 1`() {
        val ints = Flux.range(1, 3)

        ints.subscribe()
    }

    @Test
    fun `subscribe methods examples - 2`() {
        val ints = Flux.range(1, 3)

        ints.subscribe(::println)
    }

    @Test
    fun `subscribe methods examples - 3`() {
        val ints = Flux.range(1, 4)
            .map {
                if (it <= 3) return@map it
                throw RuntimeException("Got to 4")
            }

        ints.subscribe(
            { println(it) },
            { println("Error: $it") }
        )
    }

    @Test
    fun `subscribe methods examples - 4`() {
        val ints = Flux.range(1, 4)

        ints.subscribe(
            { println(it) },
            { println("Error: $it") },
            { println("Done") }
        )
    }

    @Test
    fun `BaseSubscriber example`() {
        val ss = SampleSubscriber<Int>()

        val ints = Flux.range(1, 4)

        ints.subscribe(ss)
    }

    @Test
    fun `On Backpressure and Ways to Reshape Requests`() {
        Flux.range(1, 10)
            .doOnRequest { println("request of $it") }
            .subscribe(object : BaseSubscriber<Int>() {
                override fun hookOnSubscribe(subscription: Subscription) {
                    request(1)
                }

                override fun hookOnNext(value: Int) {
                    println("Cancelling after having received $value")
                    cancel()
                }
            })
    }
}


/**
 * Alternative to Lambdas
 */
private class SampleSubscriber<T> : BaseSubscriber<T>() {
    override fun hookOnSubscribe(subscription: Subscription) {
        println("Subscribed")
        request(1)
    }

    override fun hookOnNext(value: T & Any) {
        println(value)
        request(1)
    }
}
