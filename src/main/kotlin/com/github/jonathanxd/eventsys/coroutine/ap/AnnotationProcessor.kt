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

import com.github.jonathanxd.kores.extra.getUnificationInstance
import com.github.jonathanxd.kores.generic.GenericSignature
import com.github.jonathanxd.kores.source.process.PlainSourceGenerator
import com.github.jonathanxd.kores.type.*
import java.io.IOException
import javax.annotation.processing.AbstractProcessor
import javax.annotation.processing.Filer
import javax.annotation.processing.RoundEnvironment
import javax.lang.model.SourceVersion
import javax.lang.model.element.Element
import javax.lang.model.element.TypeElement
import javax.tools.FileObject
import javax.tools.StandardLocation

class AnnotationProcessor : AbstractProcessor() {
    override fun process(annotations: MutableSet<out TypeElement>, roundEnv: RoundEnvironment): Boolean {
        val elements = roundEnv.getElementsAnnotatedWith(ReceiveChannelEvent::class.java)
        val observableEvents =
                elements.filterIsInstance<TypeElement>()
                        .flatMap { type ->
                            val params = type.getKoresTypeFromTypeParameters(processingEnv.elementUtils)
                            val signature =
                                    if (type.typeParameters.isEmpty()
                                            || params !is GenericType
                                            || !params.bounds.all { it.type is GenericType })
                                        GenericSignature.empty()
                                    else
                                        GenericSignature(params.bounds.map { it.type as GenericType }.toTypedArray())

                            getObservableAnnotations(type).map {
                                ReceiveChannelEventElement(
                                        it.value(),
                                        it.getMethodName(type),
                                        signature,
                                        params,
                                        type
                                )
                            }
                        }



        if (observableEvents.isNotEmpty()) {
            val sourceGen = PlainSourceGenerator()

            val grouped = observableEvents.groupBy { it.typeName }

            grouped.forEach { (name, factInf) ->
                val declaration = ReceiveChannelEventsInterfaceGenerator.processNamed(name, factInf)

                val file = get(processingEnv.filer, declaration.packageName, declaration.simpleName)

                file?.delete()

                val classFile = processingEnv.filer.createSourceFile(declaration.qualifiedName,
                        *factInf.map { it.origin }.toTypedArray())

                val outputStream = classFile.openOutputStream()

                outputStream.write(sourceGen.process(declaration).toByteArray(Charsets.UTF_8))

                outputStream.flush()
                outputStream.close()

            }
        }

        return true
    }


    override fun getSupportedAnnotationTypes(): MutableSet<String> {
        return mutableSetOf("com.github.jonathanxd.eventsys.coroutine.ap.ReceiveChannelEvent")
    }

    override fun getSupportedSourceVersion(): SourceVersion {
        return SourceVersion.latest()
    }

    private fun getObservableAnnotations(element: Element): List<ReceiveChannelEventUnification> =
            element.annotationMirrors.filter {
                it.annotationType.getKoresType(processingEnv.elementUtils).concreteType.`is`(ReceiveChannelEvent::class.java)
            }.map {
                getUnificationInstance(
                        it,
                    ReceiveChannelEventUnification::class.java, { null }, processingEnv.elementUtils)
            }

    private fun ReceiveChannelEventUnification.getMethodName(type: Element) =
        this.methodName().ifEmpty { type.simpleName.toString().decapitalize() }

    private fun get(filer: Filer, pkg: String, name: String): FileObject? =
            try {
                filer.getResource(StandardLocation.SOURCE_OUTPUT, pkg, name)
            } catch (e: IOException) {
                null
            }

}
