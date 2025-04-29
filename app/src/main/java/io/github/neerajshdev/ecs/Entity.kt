package io.github.neerajshdev.ecs

import java.util.UUID
import java.util.concurrent.ConcurrentHashMap
import kotlin.reflect.KClass

/**
 * Represents a unique entity in the ECS.
 * An entity is just an ID that components can be attached to.
 */
class Entity private constructor(val id: UUID) {
    companion object {
        private val entities = ConcurrentHashMap<UUID, Entity>()
        
        /**
         * Creates a new entity with a unique ID
         */
        fun create(): Entity {
            val id = UUID.randomUUID()
            val entity = Entity(id)
            entities[id] = entity
            
            // Notify the world that a new entity has been created
            World.notifyEvent(EntityCreatedEvent(entity))
            
            return entity
        }
        
        /**
         * Destroys an entity and removes all its components
         */
        fun destroy(entity: Entity) {
            entities.remove(entity.id)
            
            // Notify the world that an entity has been destroyed
            World.notifyEvent(EntityDestroyedEvent(entity))
        }
        
        /**
         * Gets an entity by its ID
         */
        fun getById(id: UUID): Entity? = entities[id]
    }
    
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Entity) return false
        return id == other.id
    }
    
    override fun hashCode(): Int = id.hashCode()
}

/**
 * Event triggered when a new entity is created
 */
data class EntityCreatedEvent(val entity: Entity) : Event

/**
 * Event triggered when an entity is destroyed
 */
data class EntityDestroyedEvent(val entity: Entity) : Event

// ===================== Fluent Builder & Extensions =====================

/**
 * Fluent builder for creating entities with components.
 * Example:
 *   val entity = EntityBuilder().with(PositionComponent(0,0)).with(VelocityComponent(1,1)).build()
 */
class EntityBuilder {
    private val components = mutableListOf<Component>()
    fun with(component: Component): EntityBuilder {
        components.add(component)
        return this
    }
    fun build(): Entity {
        val entity = Entity.create()
        components.forEach { entity.addComponent(it) }
        return entity
    }
}

/**
 * Extension: Get a component by type
 */
inline operator fun <reified T : Component> Entity.get(type: KClass<T>): T? = this.getComponent<T>()
/**
 * Extension: Set a component by type
 */
inline operator fun <reified T : Component> Entity.set(type: KClass<T>, value: T) = this.addComponent(value)
/**
 * Operator: Add a component to an entity
 */
operator fun Entity.plusAssign(component: Component) { this.addComponent(component) }
/**
 * Operator: Remove a component from an entity by type
 */
inline operator fun <reified T : Component> Entity.minusAssign(type: KClass<T>) { this.removeComponent<T>() }

/**
 * Checks if the entity has a component of type T.
 * Usage: if (entity.has<PositionComponent>()) { ... }
 */
inline fun <reified T : Component> Entity.has(): Boolean = this.hasComponent<T>()

/**
 * Removes all components from the entity.
 * Usage: entity.clear()
 */
fun Entity.clear() = ComponentManager.removeAllComponents(this) 