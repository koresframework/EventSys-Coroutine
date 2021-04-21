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
package com.github.jonathanxd.eventsys.coroutine.ap

import com.github.koresframework.eventsys.ap.Factory
import com.github.jonathanxd.eventsys.coroutine.ReceiveChannelMethodInterfaceGenerator
import kotlinx.coroutines.channels.ReceiveChannel

/**
 * Annotation used to enable generation of an abstract method in [value] that returns [ReceiveChannel] of annotated event class.
 *
 * This annotation does a work similar to [Factory].
 * The generated interface depends on [ReceiveChannel Runtime Code Generation][ReceiveChannelMethodInterfaceGenerator] to be implemented.
 *
 * @property value Name of target class to add event listeners.
 * @property methodName Name of the method of observable event method. Default is the same as de-capitalized annotated type.
 */
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.SOURCE)
annotation class ReceiveChannelEvent(val value: String,
                                     val methodName: String = "")
