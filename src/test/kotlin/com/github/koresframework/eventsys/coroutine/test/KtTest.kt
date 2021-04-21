/**
 *      EventSys-Coroutine - EventSys Extension to support Kotlin Coroutines.
 *
 *         The MIT License (MIT)
 *
 *      Copyright (c) 2021 JonathanxD (https://github.com/JonathanxD/) <jhrldev@gmail.com>
 *      Copyright (c) 2021 contributors
 *
 *      Permission is hereby granted, free of charge, to any person obtaining a copy
 *      of this software and associated documentation files (the "Software"), to deal
 *      in the Software without restriction, including without limitation the rights
 *      to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 *      copies of the Software, and to permit persons to whom the Software is
 *      furnished to do so, subject to the following conditions:
 *
 *      The above copyright notice and this permission notice shall be included in
 *      all copies or substantial portions of the Software.
 *
 *      THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 *      IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 *      FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 *      AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 *      LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 *      OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 *      THE SOFTWARE.
 */
package com.github.koresframework.eventsys.coroutine.test

import com.github.koresframework.eventsys.coroutine.channel
import com.github.koresframework.eventsys.coroutine.impl.EventsChannelFactoryImpl
import com.github.koresframework.eventsys.event.Event
import com.github.koresframework.eventsys.event.EventListener
import com.github.koresframework.eventsys.event.annotation.Name
import com.github.koresframework.eventsys.gen.event.CommonEventGenerator
import com.github.koresframework.eventsys.impl.CommonLogger
import com.github.koresframework.eventsys.impl.DefaultEventManager
import com.github.koresframework.eventsys.impl.PerChannelEventListenerRegistry
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import org.junit.jupiter.api.assertTimeout
import java.time.Duration
import kotlin.test.Test
import kotlin.test.assertEquals

class KtTest {

    @Test
    fun test() {
        assertTimeout(Duration.ofSeconds(30)) {
            val sorter = Comparator.comparing(EventListener<*>::priority)
            val logger = CommonLogger()
            val commonEventGenerator = CommonEventGenerator(logger)

            val eventListenerRegistry = PerChannelEventListenerRegistry(
                sorter,
                logger,
                commonEventGenerator
            )
            val manager = DefaultEventManager(eventListenerRegistry)
            val factory = EventsChannelFactoryImpl(eventListenerRegistry)
            val myEventFactory =
                commonEventGenerator.createFactory<MyEventFactory>(MyEventFactory::class.java).resolve()

            val channel = factory.channel<ConnectEvent>()

            var evts = 0

            GlobalScope.launch {
                channel.consumeAsFlow().filter { it.user.age >= 18 }.collect {
                    assertEquals(it.user.name, "UserB")
                    ++evts
                }
            }

            manager.dispatch(myEventFactory.createConnectEvent(User("UserA", 10)), this)
            manager.dispatch(myEventFactory.createConnectEvent(User("UserB", 18)), this)

            while (evts != 1) {
                Thread.onSpinWait()
            }
        }
    }

    interface MyEventFactory {
        fun createConnectEvent(user: @Name("user") User): ConnectEvent
    }

    interface ConnectEvent : Event {
        val user: User
    }

    data class User(val name: String, val age: Int)
}