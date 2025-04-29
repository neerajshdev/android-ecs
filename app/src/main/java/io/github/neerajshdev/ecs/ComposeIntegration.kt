package io.github.neerajshdev.ecs

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableIntStateOf
import mu.KotlinLogging

/**
 * Utilities for integrating ECS with Jetpack Compose
 */
object ComposeIntegration {
    // Tag for logging
    private val logger = KotlinLogging.logger("ComposeIntegration")

    /**
     * Creates an observer for a specific component type on a specific entity
     * Returns a MutableState<T> that will be updated whenever the component changes
     */
    @Composable
    fun <T : Component> observeComponent(
        entity: Entity, componentType: Class<T>,
    ): MutableState<T?> {
        // Get initial component value
        val componentState = remember {
            mutableStateOf(ComponentManager.getComponent(entity, componentType))
        }

        // Create and register an event-based observer system
        DisposableEffect(entity.id.toString() + componentType.name) {
            val system = object : ISystem {
                override fun getInterestedEvents() = listOf(
                    ComponentAddedEvent::class,
                    ComponentUpdatedEvent::class,
                    ComponentRemovedEvent::class
                )

                @Suppress("UNCHECKED_CAST")
                override fun processEvent(event: Event) {
                    when (event) {
                        is ComponentAddedEvent -> {
                            if (event.entity.id == entity.id &&
                                event.component.javaClass == componentType
                            ) {
                                componentState.value = event.component as T
                                logger.info(
                                    "Component added: ${componentType.simpleName} on entity ${entity.id}"
                                )
                            }
                        }

                        is ComponentUpdatedEvent -> {
                            if (event.entity.id == entity.id &&
                                event.newComponent.javaClass == componentType
                            ) {
                                componentState.value = event.newComponent as T
                                logger.info(
                                    "Component updated: ${componentType.simpleName} on entity ${entity.id}"
                                )
                            }
                        }

                        is ComponentRemovedEvent -> {
                            if (event.entity.id == entity.id &&
                                event.component.javaClass == componentType
                            ) {
                                componentState.value = null
                                logger.info(
                                    "Component removed: ${componentType.simpleName} from entity ${entity.id}"
                                )
                            }
                        }
                    }
                }
            }

            // Register system
            World.addSystem(system)

            // Cleanup when component is no longer in composition
            onDispose {
                World.removeSystem(system)
            }
        }

        return componentState
    }

    /**
     * Creates an observer for all entities with a specific component type
     * Returns a MutableState<List<Entity>> that will be updated whenever entities change
     */
    @Composable
    fun <T : Component> observeEntitiesWith(componentType: Class<T>): MutableState<List<Entity>> {
        // Get initial list of entities
        val entitiesState = remember {
            mutableStateOf(ComponentManager.getEntitiesWithComponent(componentType))
        }

        // Setup an update trigger
        val updateTrigger = remember { mutableIntStateOf(0) }

        // Create and register an event-based observer system
        DisposableEffect(componentType.name) {
            val componentSystem = object : ISystem {
                override fun getInterestedEvents() = listOf(
                    ComponentAddedEvent::class,
                    ComponentRemovedEvent::class
                )

                override fun processEvent(event: Event) {
                    when (event) {
                        is ComponentAddedEvent -> {
                            if (event.component.javaClass == componentType) {
                                // Trigger update when component is added
                                updateTrigger.intValue = updateTrigger.intValue + 1
                            }
                        }

                        is ComponentRemovedEvent -> {
                            if (event.component.javaClass == componentType) {
                                // Trigger update when component is removed
                                updateTrigger.intValue = updateTrigger.intValue + 1
                            }
                        }
                    }
                }
            }

            // Also watch for entity creation/destruction
            val entitySystem = object : ISystem {
                override fun getInterestedEvents() = listOf(
                    EntityCreatedEvent::class,
                    EntityDestroyedEvent::class
                )

                override fun processEvent(event: Event) {
                    // Trigger update when entities are created or destroyed
                    updateTrigger.intValue = updateTrigger.intValue + 1
                }
            }

            World.addSystem(componentSystem)
            World.addSystem(entitySystem)

            // Cleanup when component is no longer in composition
            onDispose {
                World.removeSystem(componentSystem)
                World.removeSystem(entitySystem)
            }
        }

        // Update list when trigger changes
        LaunchedEffect(updateTrigger.intValue) {
            entitiesState.value = ComponentManager.getEntitiesWithComponent(componentType)
        }

        return entitiesState
    }

    /**
     * Reified extension function for convenience
     */
    @Composable
    inline fun <reified T : Component> Entity.observeComponent(): MutableState<T?> {
        return observeComponent(this, T::class.java)
    }

    /**
     * Reified extension function for convenience
     */
    @Composable
    inline fun <reified T : Component> observeEntitiesWith(): MutableState<List<Entity>> {
        return observeEntitiesWith(T::class.java)
    }
} 