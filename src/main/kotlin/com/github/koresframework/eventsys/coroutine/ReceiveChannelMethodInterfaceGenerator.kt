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
package com.github.koresframework.eventsys.coroutine

/**
 * Generator of implementation of stub interfaces that provides `ReceiveChannel<Event>` of event handlers.
 * This is commonly used with interface specified in [ChannelEvent annotation][com.github.jonathanxd.eventsys.coroutine.ap.ReceiveChannelEvent].
 *
 * @see com.github.jonathanxd.eventsys.coroutine.impl.ReceiveChannelMethodInterfaceGeneratorImpl
 */
interface ReceiveChannelMethodInterfaceGenerator {
    /**
     * EventsChannelFactory to use to generate ReceiveChannels
     */
    val factory: EventsChannelFactory

    /**
     * Generates implementation of [itf] and creates an instance.
     */
    fun <T> create(itf: Class<T>): T

}