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
package com.github.jonathanxd.eventsys.coroutine.impl

import com.github.jonathanxd.eventsys.coroutine.EventsChannelFactory
import com.github.jonathanxd.iutils.type.TypeInfo
import com.github.koresframework.eventsys.error.ExceptionListenError
import com.github.koresframework.eventsys.event.Event
import com.github.koresframework.eventsys.event.EventListener
import com.github.koresframework.eventsys.event.EventListenerRegistry
import com.github.koresframework.eventsys.event.EventManager
import com.github.koresframework.eventsys.result.ListenResult
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.channels.SendChannel
import kotlinx.coroutines.flow.flow
import java.lang.reflect.Type

class EventsChannelFactoryImpl(val listenerRegistry: EventListenerRegistry) : EventsChannelFactory {
    private val channels = mutableMapOf<Type, Channel<*>>()

    @Suppress("UNCHECKED_CAST")
    override fun <T : Event> channel(eventType: Type): ReceiveChannel<T> =
        this.channels.computeIfAbsent(eventType) {
            Channel<T>(Channel.UNLIMITED).also { this.registerListener(eventType, it) }
        } as Channel<T>


    private fun <T: Event> registerListener(eventType: Type, sendChannel: SendChannel<T>) {
        this.listenerRegistry.registerListener(this, eventType, EvtListener(sendChannel))
    }

    private class EvtListener<T: Event>(private val sendChannel: SendChannel<T>) : EventListener<T> {
        override fun onEvent(event: T, dispatcher: Any): ListenResult {
            return try {
                ListenResult.Value(this.sendChannel.offer(event))
            } catch (e: Throwable) {
                ListenResult.Failed(ExceptionListenError(e))
            }
        }
    }
}