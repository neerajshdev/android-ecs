package io.github.neerajshdev.ecs

import kotlin.reflect.KClass

/**
 * Interface for all systems in the ECS.
 * Systems contain the logic that operates on entities with specific components.
 */
interface ISystem {
    /**
     * Returns the list of event types this system is interested in.
     */
    fun getInterestedEvents(): List<KClass<out Event>>

    /**
     * Processes a specific event. Systems must fetch entities as needed.
     */
    fun processEvent(event: Event)

    /**
     * Called when the system is added to the world
     */
    fun initialize() {}

    /**
     * Called when the system is removed from the world
     */
    fun cleanup() {}
}