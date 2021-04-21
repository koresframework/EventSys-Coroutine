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
import com.github.koresframework.eventsys.coroutine.impl.ReceiveChannelMethodInterfaceGeneratorImpl
import com.github.koresframework.eventsys.ap.Factory
import com.github.koresframework.eventsys.event.Event
import com.github.koresframework.eventsys.event.EventListener
import com.github.koresframework.eventsys.event.annotation.Name
import com.github.koresframework.eventsys.event.annotation.TypeParam
import com.github.koresframework.eventsys.gen.event.CommonEventGenerator
import com.github.koresframework.eventsys.impl.CommonLogger
import com.github.koresframework.eventsys.impl.DefaultEventManager
import com.github.koresframework.eventsys.impl.PerChannelEventListenerRegistry
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import org.junit.jupiter.api.assertTimeout
import java.lang.reflect.Type
import java.time.Duration
import kotlin.test.Test
import kotlin.test.assertEquals

class KtFactoryTest {

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
            val channelFactory = EventsChannelFactoryImpl(eventListenerRegistry)
            val gen = ReceiveChannelMethodInterfaceGeneratorImpl(channelFactory)
            val factory: EvtFactory = manager.eventGenerator.createFactory<EvtFactory>(EvtFactory::class.java).resolve()
            val myEvents = gen.create(MyEvents::class.java)

            val list = mutableListOf<Int>()
            val channel = channelFactory.channel<MyEvent>(MyEvent::class.java)

            GlobalScope.launch {
                channel
                    .consumeAsFlow()
                    .map { it.amount }
                    .filter { it > 9 }
                    .collect { list += it }
            }

            manager.dispatch(factory.createMyEvent(5), this)
            manager.dispatch(factory.createMyEvent(9), this)
            manager.dispatch(factory.createMyEvent(10), this)
            manager.dispatch(factory.createMyEvent(19), this)

            while (list.size < 2) {
                Thread.onSpinWait()
            }

            assertEquals(listOf(10, 19), list)

            val myEventsChannel = myEvents.myEvent()

            GlobalScope.launch {
                myEventsChannel
                    .consumeAsFlow()
                    .map { it.amount }
                    .filter { it > 9 }
                    .collect { list += it }
            }

            manager.dispatch(factory.createMyEvent(391), this)
            manager.dispatch(factory.createMyEvent(3), this)

            while (list.size < 4) {
                Thread.onSpinWait()
            }

            assertEquals(listOf(10, 19, 391, 391), list)
        }
    }

    interface MyEvents {
        fun myEvent(): ReceiveChannel<MyEvent>
        fun <T> myEvent2(@TypeParam type: Type): ReceiveChannel<MyGenericEvent<T>>
    }

    interface MyGenericEvent<T> : Event {
        val value: T
    }

    interface EvtFactory {
        fun createMyEvent(@Name("amount") amount: Int): MyEvent
    }

    @Factory("com.github.jonathanxd.eventsys.coroutine.test.KtFactoryTest.EvtFactory")
    interface MyEvent : Event {
        val amount: Int
    }

}