package io.github.neerajshdev.ecs

import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

/**
 * Interface for all components in the ECS.
 * Components hold data but no logic.
 */
interface Component

/**
 * Manages component storage and associations with entities.
 */
object ComponentManager {
    // Maps component types to a map of entity IDs to component instances
    private val componentStores = ConcurrentHashMap<Class<out Component>, ConcurrentHashMap<UUID, Component>>()
    
    /**
     * Adds a component to an entity
     */
    fun <T : Component> addComponent(entity: Entity, component: T) {
        val componentType = component.javaClass
        val entityComponents = componentStores.computeIfAbsent(componentType) { ConcurrentHashMap() }
        
        val previousComponent = entityComponents.put(entity.id, component)
        
        // Only trigger event if this is a new component or replacing one
        if (previousComponent != component) {
            if (previousComponent != null) {
                // If replacing, fire an update event
                World.notifyEvent(ComponentUpdatedEvent(entity, previousComponent, component))
            } else {
                // If new, fire an add event
                World.notifyEvent(ComponentAddedEvent(entity, component))
            }
        }
    }
    
    /**
     * Updates a component of an entity
     * This explicitly fires a component updated event
     */
    fun <T : Component> updateComponent(entity: Entity, component: T) {
        val componentType = component.javaClass
        val entityComponents = componentStores.computeIfAbsent(componentType) { ConcurrentHashMap() }
        
        val previousComponent = entityComponents.put(entity.id, component)
        
        if (previousComponent != null) {
            // Fire update event
            World.notifyEvent(ComponentUpdatedEvent(entity, previousComponent, component))
        } else {
            // If component didn't exist, fire add event
            World.notifyEvent(ComponentAddedEvent(entity, component))
        }
    }
    
    /**
     * Removes a component from an entity
     */
    fun <T : Component> removeComponent(entity: Entity, componentType: Class<T>) {
        val entityComponents = componentStores[componentType] ?: return
        
        val removedComponent = entityComponents.remove(entity.id)
        if (removedComponent != null) {
            World.notifyEvent(ComponentRemovedEvent(entity, removedComponent))
        }
    }
    
    /**
     * Gets a component of type T for an entity
     */
    @Suppress("UNCHECKED_CAST")
    fun <T : Component> getComponent(entity: Entity, componentType: Class<T>): T? {
        val entityComponents = componentStores[componentType] ?: return null
        return entityComponents[entity.id] as T?
    }
    
    /**
     * Checks if an entity has a component of type T
     */
    fun <T : Component> hasComponent(entity: Entity, componentType: Class<T>): Boolean {
        val entityComponents = componentStores[componentType] ?: return false
        return entityComponents.containsKey(entity.id)
    }
    
    /**
     * Gets all entities that have a specific component type
     */
    fun <T : Component> getEntitiesWithComponent(componentType: Class<T>): List<Entity> {
        val entityComponents = componentStores[componentType] ?: return emptyList()
        
        return entityComponents.keys.mapNotNull { entityId ->
            Entity.getById(entityId)
        }
    }
    
    /**
     * Gets all entities that have all the specified component types
     */
    fun getEntitiesWithComponents(componentTypes: List<Class<out Component>>): List<Entity> {
        if (componentTypes.isEmpty()) return emptyList()
        
        // Start with entities that have the first component type
        var entities = getEntitiesWithComponent(componentTypes[0])
        
        // Filter by each additional component type
        for (i in 1 until componentTypes.size) {
            val componentType = componentTypes[i]
            entities = entities.filter { entity ->
                hasComponent(entity, componentType)
            }
        }
        
        return entities
    }
    
    /**
     * Gets all entities that have any component
     */
    fun getAllEntities(): List<Entity> {
        val entityIds = mutableSetOf<UUID>()
        
        // Collect all entity IDs from all component stores
        for (componentStore in componentStores.values) {
            entityIds.addAll(componentStore.keys)
        }
        
        // Convert IDs to Entity objects
        return entityIds.mapNotNull { entityId ->
            Entity.getById(entityId)
        }
    }
    
    /**
     * Removes all components for an entity
     */
    fun removeAllComponents(entity: Entity) {
        for (componentStore in componentStores.values) {
            val removedComponent = componentStore.remove(entity.id)
            if (removedComponent != null) {
                World.notifyEvent(ComponentRemovedEvent(entity, removedComponent))
            }
        }
    }
}

/**
 * Event triggered when a component is added to an entity
 */
data class ComponentAddedEvent(val entity: Entity, val component: Component) : Event

/**
 * Event triggered when a component is removed from an entity
 */
data class ComponentRemovedEvent(val entity: Entity, val component: Component) : Event

/**
 * Event triggered when a component is updated on an entity
 */
data class ComponentUpdatedEvent(
    val entity: Entity, 
    val previousComponent: Component, 
    val newComponent: Component
) : Event

// Extension functions for Entity to make working with components more convenient
fun <T : Component> Entity.addComponent(component: T) {
    ComponentManager.addComponent(this, component)
}

inline fun <reified T : Component> Entity.getComponent(): T? {
    return ComponentManager.getComponent(this, T::class.java)
}

inline fun <reified T : Component> Entity.removeComponent() {
    ComponentManager.removeComponent(this, T::class.java)
}

inline fun <reified T : Component> Entity.hasComponent(): Boolean {
    return ComponentManager.hasComponent(this, T::class.java)
}

/**
 * Update a component and notify observers in one call.
 * Example:
 * ```
 * entity.updateComponent<PositionComponent> { copy(x = x + dx, y = y + dy) }
 * ```
 */
inline fun <reified T : Component> Entity.updateComponent(block: T.() -> T) {
    val component = this.getComponent<T>() ?: return
    ComponentManager.updateComponent(this, component.block())
} 