package io.github.neerajshdev.ecs

/**
 * Interface for all events in the ECS.
 * Events are used to communicate between systems.
 */
interface Event

/**
 * Event triggered when a system is added to the world
 */
data class SystemAddedEvent(val system: ISystem) : Event

/**
 * Event triggered when a system is removed from the world
 */
data class SystemRemovedEvent(val system: ISystem) : Event 