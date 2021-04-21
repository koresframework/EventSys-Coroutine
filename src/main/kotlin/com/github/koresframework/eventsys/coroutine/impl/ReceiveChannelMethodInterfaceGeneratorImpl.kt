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
package com.github.koresframework.eventsys.coroutine.impl

import com.github.koresframework.eventsys.coroutine.EventsChannelFactory
import com.github.koresframework.eventsys.coroutine.ReceiveChannelMethodInterfaceGenerator
import com.github.jonathanxd.iutils.type.TypeUtil
import com.github.jonathanxd.kores.Instructions
import com.github.jonathanxd.kores.base.Access
import com.github.jonathanxd.kores.base.MethodDeclaration
import com.github.jonathanxd.kores.common.VariableRef
import com.github.jonathanxd.kores.factory.*
import com.github.jonathanxd.kores.type.asGeneric
import com.github.jonathanxd.kores.util.conversion.toVariableAccess
import com.github.jonathanxd.kores.util.conversion.typeSpec
import com.github.jonathanxd.koresproxy.InvokeSuper
import com.github.jonathanxd.koresproxy.KoresProxy
import com.github.jonathanxd.koresproxy.gen.Custom
import com.github.jonathanxd.koresproxy.gen.CustomHandlerGenerator
import com.github.jonathanxd.koresproxy.gen.DirectInvocationCustom
import com.github.jonathanxd.koresproxy.gen.GenEnv
import com.github.jonathanxd.koresproxy.internals.Util
import com.github.koresframework.eventsys.event.Event
import com.github.koresframework.eventsys.event.annotation.TypeParam
import com.github.koresframework.eventsys.gen.event.createGenericType
import kotlinx.coroutines.channels.ReceiveChannel
import java.lang.reflect.Method
import java.lang.reflect.Type

/**
 * Default implementation of [ReceiveChannelMethodInterfaceGenerator].
 *
 * This class generated method implementations that invoke [EventsChannelFactory.channel] on [factory].
 *
 * This implementation uses *CodeProxy* [DirectInvocationCustom] to generate invocations, this
 * means that it generates direct invocation to methods (no reflection, no method handle).
 */
class ReceiveChannelMethodInterfaceGeneratorImpl(override val factory: EventsChannelFactory) :
    ReceiveChannelMethodInterfaceGenerator {

    override fun <T> create(itf: Class<T>): T =
            KoresProxy.newProxyInstance<T>(arrayOf(), arrayOf()) {
                it.addInterface(itf)
                    .classLoader(itf.classLoader)
                    .addCustomGenerator(InvokeSuper::class.java)
                    .addCustom(ToEventsCustom(this.factory))
                    .invocationHandler { _, _, _, _ ->
                        InvokeSuper.INSTANCE
                    }
            }


    private class ToEventsCustom(val events: EventsChannelFactory) : DirectInvocationCustom {

        override fun getAdditionalProperties(): List<Custom.Property> =
                listOf(Custom.Property(VariableRef(EventsChannelFactory::class.java, "factory"), null))

        override fun getValueForConstructorProperties(): List<Any> =
                listOf(this.events)

        override fun generateSpecCache(m: Method): Boolean = false

        override fun getCustomHandlerGenerators(): List<CustomHandlerGenerator> =
                listOf(EventsInvoke)


        object EventsInvoke : CustomHandlerGenerator {
            private val evts = VariableRef(EventsChannelFactory::class.java, "factory")
            override fun handle(target: Method, methodDeclaration: MethodDeclaration, env: GenEnv): Instructions {
                if (target.declaringClass == Object::class.java) {
                    env.isInvokeHandler = false
                    env.isMayProceed = false

                    return Instructions.fromPart(returnValue(target.returnType,
                            invokeSpecial(target.declaringClass,
                                    Access.SUPER,
                                    target.name,
                                    target.typeSpec,
                                    methodDeclaration.parameters.map { it.toVariableAccess() }
                            )
                    ))
                }

                val typeInfo = TypeUtil.toTypeInfo(target.genericReturnType)
                if (typeInfo.typeParameters.size == 1
                        && Event::class.java.isAssignableFrom(typeInfo.getTypeParameter(0).typeClass)) {

                    env.isInvokeHandler = false
                    env.isMayProceed = false

                    val eventTypeParameter =
                            if (target.parameterCount == 1 && target.parameters[0].isAnnotationPresent(TypeParam::class.java))
                                accessVariable(target.parameters[0].type, target.parameters[0].name)
                            else
                                createGenericType(target.genericReturnType.asGeneric.bounds[0].type)


                    return Instructions.fromPart(
                            returnValue(ReceiveChannel::class.java, accessThisField(
                                EventsChannelFactory::class.java, Util.getAdditionalPropertyFieldName(
                                    evts
                                ))
                                    .invokeInterface(
                                        EventsChannelFactory::class.java,
                                            "channel",
                                            typeSpec(ReceiveChannel::class.java, Type::class.java),
                                            listOf(eventTypeParameter))))
                }

                return Instructions.empty()
            }
        }
    }
}