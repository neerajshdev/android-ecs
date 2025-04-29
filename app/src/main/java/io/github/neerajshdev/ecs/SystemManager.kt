package io.github.neerajshdev.ecs

import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.CopyOnWriteArrayList
import kotlin.reflect.KClass

/**
 * Manages systems registration and event processing
 */
object SystemManager {
    // Tag for logging
    private const val TAG = "SystemManager"
    
    private val systems = CopyOnWriteArrayList<ISystem>()
    private val eventSubscriptions = ConcurrentHashMap<KClass<out Event>, MutableList<ISystem>>()
    
    /**
     * Adds a system to the world
     */
    fun addSystem(system: ISystem) {
        if (systems.contains(system)) return
        
        systems.add(system)
        
        // Register event subscriptions based on the system's interested events
        for (eventType in system.getInterestedEvents()) {
            val subscribers = eventSubscriptions.computeIfAbsent(eventType) { CopyOnWriteArrayList() }
            subscribers.add(system)
        }

        // Initialize the system
        system.initialize()

        // Notify the world that a system has been added
        World.notifyEvent(SystemAddedEvent(system))
    }
    
    /**
     * Removes a system from the world
     */
    fun removeSystem(system: ISystem) {
        if (!systems.contains(system)) return
        
        // Cleanup the system
        system.cleanup()
        
        // Remove system from event subscriptions
        for (eventType in system.getInterestedEvents()) {
            eventSubscriptions[eventType]?.remove(system)
        }
        
        // Remove the system
        systems.remove(system)
        
        // Notify the world that a system has been removed
        World.notifyEvent(SystemRemovedEvent(system))
    }
    
    /**
     * Process an event by dispatching it to all interested systems.
     */
    fun processEvent(event: Event) {
        val eventType = event::class
        val subscribers = eventSubscriptions[eventType] ?: return
        for (system in subscribers) {
            system.processEvent(event)
        }
    }
    
    /**
     * Gets all registered systems
     */
    fun getAllSystems(): List<ISystem> = systems.toList()
    
    /**
     * Gets a system by its class
     */
    @Suppress("UNCHECKED_CAST")
    fun <T : ISystem> getSystem(systemClass: Class<T>): T? {
        return systems.find { systemClass.isInstance(it) } as T?
    }
} 