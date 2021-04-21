# EventSys-Coroutine

Extension for [EventSys](https://github.com/ProjectSandstone/EventSys) to support [Kotlin Coroutines](https://kotlinlang.org/docs/coroutines-guide.html) [ReceiveChannel](https://kotlinlang.org/docs/channels.html) as Listeners

# Usage example:

```kotlin

val listenerRegistry = //...
val manager = //...
val factory = EventsChannelFactoryImpl(listenerRegistry)
factory.channel<ConnectEvent>()
        .consumeAsFlow()
        .map { it.user }
        .filter { user -> user.age >= 16 }
        .collect { user ->
            // ...
        }
```

# @ReceiveChannelEvent

Works in the same way as [EventSys Factory](https://github.com/koresframework/EventSys/wiki/Factory-annotation), but generates a stub interface that return `ReceiveChannel<T>` event handlers (this means that returned channel receives data from a listener which handles the annotated event). The `value` of *@ReceiveChannelEvent* cannot be the same as of the *@Factory* (obvious reasons).

To generate implementation of generated stub observable event handler interface, you can use `ReceiveChannelEventsInterfaceGeneratorImpl` (default implementation of `ReceiveChannelEventsInterfaceGenerator`).

Example:

```kotlin
@ReceiveChannelEvent("com.github.koresframework.eventsys.coroutine.example.ExampleEvents")
interface BuyEvent : Event {
    val user: User
    val amount: Int
}

class ReceiveBuyEventExample {
    suspend fun example() {
        val factory = ...
        val generator = ReceiveChannelEventsInterfaceGeneratorImpl(factory)
        val exampleEvents = generator.create(ExampleEvents::class.java) // ExampleEvents = Generated stub interface
        exampleEvents.buyEvent()
            .consumeAsFlow()
            .map { it.user }
            .filter { it.age >= 18 }
            .collect { ... }
    }
}
```