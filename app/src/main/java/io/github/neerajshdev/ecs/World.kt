package io.github.neerajshdev.ecs

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import io.github.aakira.napier.Napier
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.atomic.AtomicBoolean

/**
 * The World is the central manager for the ECS.
 * It manages entities, components, and systems.
 */
object World {
    // Use Napier for logging
    /**
     * Initializes the world with a thread pool of the specified size
     */
    fun initialize(threadPoolSize: Int = Runtime.getRuntime().availableProcessors()) {
        // Create a fixed thread pool for system execution
        executor = Executors.newFixedThreadPool(threadPoolSize)
        
        // Start the event processing loop
        running.set(true)
        executor?.submit { processEvents() }
    }
    
    /**
     * Shuts down the world and cleans up resources
     */
    fun shutdown() {
        running.set(false)
        
        // Shutdown executor service
        executor?.shutdown()
        executor = null
    }
    
    /**
     * Creates a new entity
     */
    fun createEntity(): Entity {
        return Entity.create()
    }
    
    /**
     * Destroys an entity
     */
    fun destroyEntity(entity: Entity) {
        // First remove all components
        ComponentManager.removeAllComponents(entity)
        
        // Then destroy the entity
        Entity.destroy(entity)
    }
    
    /**
     * Adds a system to the world
     */
    fun addSystem(system: ISystem) {
        SystemManager.addSystem(system)
    }
    
    /**
     * Removes a system from the world
     */
    fun removeSystem(system: ISystem) {
        SystemManager.removeSystem(system)
    }
    
    /**
     * Notifies the world of an event
     */
    fun notifyEvent(event: Event) {
        eventQueue.offer(event)
    }
    
    /**
     * Process events in the queue
     */
    private fun processEvents() {
        while (running.get() || !eventQueue.isEmpty()) {
            try {
                val event = eventQueue.poll()
                if (event != null) {
                    // Log the event being processed
                    val eventName = event::class.simpleName ?: "Unknown"
                    Napier.i("Processing event: $eventName")
                    
                    // Process the event
                    SystemManager.processEvent(event)
                    
                    // Log that event processing is complete
                    Napier.i("Completed processing event: $eventName")
                } else if (running.get()) {
                    // If no events but still running, wait a bit
                    Thread.sleep(1)
                }
            } catch (e: Exception) {
                // Log the exception but keep processing events
                Napier.e("Error processing event: ${e.message}", e)
                e.printStackTrace()
            }
        }
    }
    
    // Flag to indicate if the world is running
    private val running = AtomicBoolean(false)
    
    // Thread pool for executing systems
    private var executor: ExecutorService? = null
    
    // Event queue for processing events
    private val eventQueue = LinkedBlockingQueue<Event>()
}

/**
 * Compose-friendly system registration. Automatically removes the system when the composable leaves composition.
 */
@Composable
fun RegisterSystem(system: ISystem) {
    DisposableEffect(system) {
        World.addSystem(system)
        onDispose { World.removeSystem(system) }
    }
}

/**
 * Inline event dispatch from an entity.
 */
fun Entity.dispatch(event: Event) {
    World.notifyEvent(event)
} 