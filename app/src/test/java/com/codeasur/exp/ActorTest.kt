package com.codeasur.exp

import com.codeasur.exp.actor.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.junit.Test

val sayHello = behaviorOf<String, String>("sayHello")
val printHello = signalOf<String>("printHello")
val throwError = behaviorOf<Unit, Unit>("throwError")
val shutdown = signalOf<Unit>("shutdown")

class ActorTest {
    class HelloActor(context: ActorContext) : Actor(context) {
        private val childActor by lazy {
            spawn("child") { ChildActor(it) }
        }

        class ChildActor(context: ActorContext) : Actor(context) {
            override fun ActionBuilder.register() {
                sayHello impl { name -> "Hello, $name" }
                throwError impl { throw RuntimeException("Test error") }
            }

            override suspend fun preStart() {
                logger.info { "Child started" }
            }

            override suspend fun postStop() {
                logger.info { "Child stopped" }
            }
        }

        override fun ActionBuilder.register() {
            sayHello impl { name ->
                sayHello(childActor, name).await()
            }

            printHello impl {
                logger.info { "hello $it" }
            }

            throwError impl {
                throwError(childActor, Unit).await()
            }

            shutdown impl {
                stop()
            }
        }
    }

    @Test
    fun testBasicMessage() {
        val actor = ActorSystem.createRootActor("root") { HelloActor(it) }
        runBlocking {
            delay(1000)
            val result = sayHello(actor, "world").await()
            assert(result == "Hello, world")
        }
    }

    @Test
    fun testErrorHandling() {
        val actor = ActorSystem.createRootActor("root") { HelloActor(it) }
        runBlocking {
            try {
                throwError(actor, Unit).await()
                assert(false) { "Should throw exception" }
            } catch (e: RuntimeException) {
                assert(e.message == "Test error")
            }
        }
    }

    @Test
    fun testLifecycle() {
        val actor = ActorSystem.createRootActor("root") { HelloActor(it) }
        runBlocking {
            shutdown(actor, Unit)
            delay(1000)
            // Verify actor is stopped by trying to send a message
            try {
                sayHello(actor, "world").await()
                assert(false) { "Should throw exception" }
            } catch (e: Exception) {
                // Expected behavior
            }
        }
    }

    @Test
    fun test() {
        val actor = ActorSystem.createRootActor("root") { HelloActor(it) }
        runBlocking {
            printHello(actor, "world")
        }
    }
}
