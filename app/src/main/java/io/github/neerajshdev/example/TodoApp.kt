package io.github.neerajshdev.example

import io.github.neerajshdev.ecs.Component
import io.github.neerajshdev.ecs.ComponentAddedEvent
import io.github.neerajshdev.ecs.ComponentRemovedEvent
import io.github.neerajshdev.ecs.ComponentUpdatedEvent
import io.github.neerajshdev.ecs.Entity
import io.github.neerajshdev.ecs.Event
import io.github.neerajshdev.ecs.ISystem
import io.github.neerajshdev.ecs.World
import io.github.neerajshdev.ecs.addComponent
import io.github.neerajshdev.ecs.getComponent
import io.github.neerajshdev.ecs.updateComponent
import io.github.neerajshdev.ecs.ComponentManager
import io.github.aakira.napier.Napier
import java.time.LocalDateTime

// Todo Item Components - all with immutable fields (val instead of var)
data class TodoTextComponent(val text: String) : Component

data class TodoCompletedComponent(val completed: Boolean = false) : Component

data class TodoPriorityComponent(val priority: Int) : Component // 1-High, 2-Medium, 3-Low

data class TodoDueDateComponent(val dueDate: LocalDateTime?) : Component

data class TodoCategoryComponent(val category: String) : Component

data class TodoCreatedDateComponent(val createdDate: LocalDateTime = LocalDateTime.now()) :
    Component

data class TodoUIStateComponent(
    val expanded: Boolean = false,
    val editMode: Boolean = false,
) : Component

// UI-related Components - all with immutable fields (val instead of var)
data class UIVisibleComponent(val visible: Boolean = true) : Component

data class UIFilterComponent(
    val showCompleted: Boolean = true,
    val showActive: Boolean = true,
    val priorityFilter: Int? = null,
    val categoryFilter: String? = null,
) : Component

// Todo App Events
data class CreateTodoEvent(
    val text: String,
    val priority: Int = 2,
    val category: String = "General",
) : Event

data class DeleteTodoEvent(val entity: Entity) : Event

data class ToggleTodoEvent(val entity: Entity) : Event

data class UpdateTodoTextEvent(val entity: Entity, val newText: String) : Event

data class SetTodoPriorityEvent(val entity: Entity, val priority: Int) : Event

data class SetTodoCategoryEvent(val entity: Entity, val category: String) : Event

data class SetTodoDueDateEvent(val entity: Entity, val dueDate: LocalDateTime?) : Event

data class UIToggleExpandTodoEvent(val entity: Entity) : Event

data class UIEnterEditModeEvent(val entity: Entity) : Event

data class UIExitEditModeEvent(val entity: Entity) : Event

// Filter Events
data class SetFilterEvent(
    val showCompleted: Boolean? = null,
    val showActive: Boolean? = null,
    val priorityFilter: Int? = null,
    val categoryFilter: String? = null,
) : Event

// System that manages todo creation and updates
class TodoManagementSystem : ISystem {

    override fun getInterestedEvents() =
        listOf(
            CreateTodoEvent::class,
            DeleteTodoEvent::class,
            ToggleTodoEvent::class,
            UpdateTodoTextEvent::class,
            SetTodoPriorityEvent::class,
            SetTodoCategoryEvent::class,
            SetTodoDueDateEvent::class
        )

    override fun processEvent(event: Event) {
        when (event) {
            is CreateTodoEvent -> {
                val todoEntity = World.createEntity()

                // Add required components
                todoEntity.addComponent(TodoTextComponent(event.text))
                todoEntity.addComponent(TodoCompletedComponent(false))
                todoEntity.addComponent(TodoPriorityComponent(event.priority))
                todoEntity.addComponent(TodoCategoryComponent(event.category))
                todoEntity.addComponent(TodoDueDateComponent(null))
                todoEntity.addComponent(TodoCreatedDateComponent())
                todoEntity.addComponent(TodoUIStateComponent())
                todoEntity.addComponent(UIVisibleComponent())

                Napier.i("Created new Todo: '${event.text}'")
            }

            is DeleteTodoEvent -> {
                World.destroyEntity(event.entity)
                Napier.i("Deleted Todo")
            }

            is ToggleTodoEvent -> {
                val entity = event.entity
                val completedComponent = entity.getComponent<TodoCompletedComponent>()

                if (completedComponent != null) {
                    // Toggle completed state using immutable pattern
                    entity.updateComponent<TodoCompletedComponent> { copy(completed = !completed) }
                }
            }

            is UpdateTodoTextEvent -> {
                val entity = event.entity
                val textComponent = entity.getComponent<TodoTextComponent>()

                if (textComponent != null) {
                    // Update text using immutable pattern
                    entity.updateComponent<TodoTextComponent> { copy(text = event.newText) }

                    Napier.i("Updated Todo text: '${event.newText}'")
                }
            }

            is SetTodoPriorityEvent -> {
                val entity = event.entity
                val priorityComponent = entity.getComponent<TodoPriorityComponent>()

                if (priorityComponent != null) {
                    // Update priority using immutable pattern
                    entity.updateComponent<TodoPriorityComponent> {
                        copy(priority = event.priority)
                    }

                    Napier.i("Updated Todo priority to: ${event.priority}")
                }
            }

            is SetTodoCategoryEvent -> {
                val entity = event.entity
                val categoryComponent = entity.getComponent<TodoCategoryComponent>()

                if (categoryComponent != null) {
                    // Update category using immutable pattern
                    entity.updateComponent<TodoCategoryComponent> {
                        copy(category = event.category)
                    }

                    Napier.i("Updated Todo category to: '${event.category}'")
                }
            }

            is SetTodoDueDateEvent -> {
                val entity = event.entity
                val dueDateComponent = entity.getComponent<TodoDueDateComponent>()

                if (dueDateComponent != null) {
                    // Update due date using immutable pattern
                    entity.updateComponent<TodoDueDateComponent> { copy(dueDate = event.dueDate) }

                    Napier.i("Updated Todo due date to: ${event.dueDate}")
                }
            }
        }
    }
}

// System that handles UI state for todo items
class TodoUISystem : ISystem {

    override fun getInterestedEvents() =
        listOf(
            UIToggleExpandTodoEvent::class,
            UIEnterEditModeEvent::class,
            UIExitEditModeEvent::class
        )

    override fun processEvent(event: Event) {
        when (event) {
            is UIToggleExpandTodoEvent -> {
                val entity = event.entity

                // Check if this entity is in our matching set
                if (ComponentManager.getEntitiesWithComponent(TodoUIStateComponent::class.java).contains(entity)) {
                    val uiStateComponent = entity.getComponent<TodoUIStateComponent>()

                    if (uiStateComponent != null) {
                        // Toggle expanded state using immutable pattern
                        entity.updateComponent<TodoUIStateComponent> { copy(expanded = !expanded) }

                        // Get updated component
                        val updatedComponent = entity.getComponent<TodoUIStateComponent>()
                        Napier.i("Toggled Todo expanded state: ${updatedComponent?.expanded}")
                    }
                }
            }

            is UIEnterEditModeEvent -> {
                val entity = event.entity

                // Check if this entity is in our matching set
                if (ComponentManager.getEntitiesWithComponent(TodoUIStateComponent::class.java).contains(entity)) {
                    val uiStateComponent = entity.getComponent<TodoUIStateComponent>()

                    if (uiStateComponent != null) {
                        // Enter edit mode using immutable pattern
                        entity.updateComponent<TodoUIStateComponent> {
                            copy(editMode = true, expanded = true)
                        }

                        Napier.i("Todo entered edit mode")
                    }
                }
            }

            is UIExitEditModeEvent -> {
                val entity = event.entity

                // Check if this entity is in our matching set
                if (ComponentManager.getEntitiesWithComponent(TodoUIStateComponent::class.java).contains(entity)) {
                    val uiStateComponent = entity.getComponent<TodoUIStateComponent>()

                    if (uiStateComponent != null) {
                        // Exit edit mode using immutable pattern
                        entity.updateComponent<TodoUIStateComponent> { copy(editMode = false) }

                        Napier.i("Todo exited edit mode")
                    }
                }
            }
        }
    }
}

// System that handles filtering todos for display
class TodoFilterSystem : ISystem {

    private val filterEntity: Entity by lazy {
        // Create the filter entity if it doesn't exist
        val entities = World.createEntity()
        entities.addComponent(UIFilterComponent())
        entities
    }

    override fun getInterestedEvents() = listOf(SetFilterEvent::class)

    override fun initialize() {
        // Create the filter entity when the system is initialized
        filterEntity
    }

    override fun processEvent(event: Event) {
        if (event is SetFilterEvent) {
            val filterComponent = filterEntity.getComponent<UIFilterComponent>() ?: return

            // Update filter settings using immutable pattern
            filterEntity.updateComponent<UIFilterComponent> {
                copy(
                    showCompleted = event.showCompleted ?: showCompleted,
                    showActive = event.showActive ?: showActive,
                    priorityFilter = event.priorityFilter ?: priorityFilter,
                    categoryFilter = event.categoryFilter ?: categoryFilter
                )
            }

            // Get updated filter component
            val updatedFilter = filterEntity.getComponent<UIFilterComponent>() ?: return

            Napier.i(
                "Updated filters: " +
                        "showCompleted=${updatedFilter.showCompleted}, " +
                        "showActive=${updatedFilter.showActive}, " +
                        "priority=${updatedFilter.priorityFilter}, " +
                        "category=${updatedFilter.categoryFilter}"
            )

            // Fetch all todo entities and apply filters
            val allTodos = ComponentManager.getEntitiesWithComponent(TodoTextComponent::class.java)
            applyFilters(allTodos)
        }
    }

    private fun applyFilters(todoEntities: List<Entity>) {
        val filterComponent = filterEntity.getComponent<UIFilterComponent>() ?: return

        for (entity in todoEntities) {
            val completedComponent = entity.getComponent<TodoCompletedComponent>() ?: continue
            val priorityComponent = entity.getComponent<TodoPriorityComponent>() ?: continue
            val categoryComponent = entity.getComponent<TodoCategoryComponent>() ?: continue
            val visibleComponent = entity.getComponent<UIVisibleComponent>() ?: continue

            // Check completion status filter
            val visibleByCompletion =
                when {
                    completedComponent.completed -> filterComponent.showCompleted
                    else -> filterComponent.showActive
                }

            // Check priority filter
            val visibleByPriority =
                filterComponent.priorityFilter == null ||
                        priorityComponent.priority == filterComponent.priorityFilter

            // Check category filter
            val visibleByCategory =
                filterComponent.categoryFilter == null ||
                        categoryComponent.category == filterComponent.categoryFilter

            // Update visibility using immutable pattern
            entity.updateComponent<UIVisibleComponent> {
                copy(visible = visibleByCompletion && visibleByPriority && visibleByCategory)
            }
        }
    }
}

class TodoDebuger : ISystem {
    override fun getInterestedEvents() =
        listOf(
            ComponentUpdatedEvent::class,
            ComponentAddedEvent::class,
            ComponentRemovedEvent::class
        )

    override fun processEvent(event: Event) {
        when (event) {
            is ComponentUpdatedEvent -> {
                Napier.i(
                    "Component updated on entity: ${event.entity} from ${event.previousComponent} to ${event.newComponent}"
                )
            }

            is ComponentAddedEvent -> {
                Napier.i(
                    "Component added to entity: ${event.entity} with component: ${event.component}"
                )
            }

            is ComponentRemovedEvent -> {
                Napier.i(
                    "Component removed from entity: ${event.entity} with component: ${event.component}"
                )
            }
        }
    }
}

// Optional main function for testing outside of Android context
fun main() {
    // Initialize the ECS world
    World.initialize(4)

    // Add systems
    World.addSystem(TodoManagementSystem())
    World.addSystem(TodoUISystem())
    World.addSystem(TodoFilterSystem())
    World.addSystem(TodoDebuger())

    World.notifyEvent(CreateTodoEvent("Learn Kotlin"))
    World.notifyEvent(CreateTodoEvent("Learn ECS"))
    World.notifyEvent(CreateTodoEvent("Learn Android"))
    World.notifyEvent(CreateTodoEvent("Learn Compose"))

    // Show all todos
    println("\n=== Final Todo State ===")
    World.notifyEvent(
        SetFilterEvent(
            showCompleted = true,
            showActive = true,
            priorityFilter = null,
            categoryFilter = null
        )
    )

    // sleep
    Thread.sleep(2000)

    // Cleanup
    World.shutdown()
}
