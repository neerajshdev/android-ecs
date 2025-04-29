package io.github.neerajshdev.example

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import io.github.neerajshdev.ecs.ComposeIntegration.observeComponent
import io.github.neerajshdev.ecs.Entity
import io.github.neerajshdev.ecs.World
import io.github.neerajshdev.ecs.addComponent
import io.github.neerajshdev.ecs.getComponent
import io.github.neerajshdev.ecs.updateComponent
import io.github.neerajshdev.ecs.ComposeIntegration
import io.github.aakira.napier.Napier


/**
 * Filter bar using components and observers
 */
@Composable
fun FilterBar() {
    // Get or create filter entity
    val filterEntities = ComposeIntegration.observeEntitiesWith(UIFilterComponent::class.java)

    // Create filter entity if it doesn't exist
    LaunchedEffect(filterEntities.value.isEmpty()) {
        if (filterEntities.value.isEmpty()) {
            val entity = World.createEntity()
            entity.addComponent(UIFilterComponent())
            Napier.d("Created filter entity", tag = "TodoECS")
        }
    }

    // Get the first filter entity if available
    val filterEntity = filterEntities.value.firstOrNull() ?: return

    // Observe the filter component
    val filterComponentState = filterEntity.observeComponent<UIFilterComponent>()
    val filterComponent = filterComponentState.value ?: return

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 16.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Filters",
                style = MaterialTheme.typography.titleMedium
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(
                        checked = filterComponent.showCompleted,
                        onCheckedChange = { checked ->
                            // Update the component using immutable pattern
                            filterEntity.updateComponent<UIFilterComponent> {
                                copy(showCompleted = checked)
                            }
                        }
                    )
                    Text("Completed")
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(
                        checked = filterComponent.showActive,
                        onCheckedChange = { checked ->
                            filterEntity.updateComponent<UIFilterComponent> {
                                copy(showActive = checked)
                            }
                        }
                    )
                    Text("Active")
                }
            }

            // Priority filter
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                FilterChip(
                    selected = filterComponent.priorityFilter == 1,
                    onClick = {
                        val newPriority = if (filterComponent.priorityFilter == 1) null else 1
                        filterEntity.updateComponent<UIFilterComponent> {
                            copy(priorityFilter = newPriority)
                        }
                    },
                    label = { Text("High") }
                )

                FilterChip(
                    selected = filterComponent.priorityFilter == 2,
                    onClick = {
                        val newPriority = if (filterComponent.priorityFilter == 2) null else 2
                        filterEntity.updateComponent<UIFilterComponent> {
                            copy(priorityFilter = newPriority)
                        }
                    },
                    label = { Text("Medium") }
                )

                FilterChip(
                    selected = filterComponent.priorityFilter == 3,
                    onClick = {
                        val newPriority = if (filterComponent.priorityFilter == 3) null else 3
                        filterEntity.updateComponent<UIFilterComponent> {
                            copy(priorityFilter = newPriority)
                        }
                    },
                    label = { Text("Low") }
                )
            }
        }
    }
}

/**
 * Todo list that observes entities with TodoTextComponent
 */
@Composable
fun TodoList() {
    val todosState = ComposeIntegration.observeEntitiesWith(TodoTextComponent::class.java)

    LazyColumn {
        items(todosState.value) {
            val visibleComponent = remember(it.id) {
                it.getComponent<UIVisibleComponent>()
            }

            if (visibleComponent?.visible == true) {
                TodoItem(it)
            }
        }
    }
}

/**
 * A single todo item that directly observes component changes
 */
@Composable
fun TodoItem(entity: Entity) {
    // Observe regular components directly 
    val textComponentState = entity.observeComponent<TodoTextComponent>()
    val completedComponentState = entity.observeComponent<TodoCompletedComponent>()
    val uiStateComponentState = entity.observeComponent<TodoUIStateComponent>()
    val priorityComponentState = entity.observeComponent<TodoPriorityComponent>()

    // Get other components
    val categoryComponent = remember(entity.id) {
        entity.getComponent<TodoCategoryComponent>()
    }

    // Safely extract values, return if any required component is missing
    val textComponent = textComponentState.value ?: return
    val completedComponent = completedComponentState.value ?: return
    val uiStateComponent = uiStateComponentState.value ?: return
    val priorityComponent = priorityComponentState.value ?: return

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                // Checkbox for completion
                Checkbox(
                    checked = completedComponent.completed,
                    onCheckedChange = {
                        entity.updateComponent<TodoCompletedComponent> {
                            copy(completed = it)
                        }
                    }
                )

                // Text content
                Text(
                    text = textComponent.text,
                    style = MaterialTheme.typography.bodyLarge,
                    textDecoration = if (completedComponent.completed)
                        TextDecoration.LineThrough else null,
                    modifier = Modifier.weight(1f)
                )

                // Expand button
                Button(
                    onClick = {
                        entity.updateComponent<TodoUIStateComponent> {
                            copy(expanded = !expanded)
                        }
                    }
                ) {
                    Text(if (uiStateComponent.expanded) "Collapse" else "Expand")
                }
            }

            // Expanded details
            if (uiStateComponent.expanded) {
                if (uiStateComponent.editMode) {
                    // Edit mode
                    var text by remember { mutableStateOf(textComponent.text) }
                    var priority by remember { mutableStateOf(priorityComponent.priority) }

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp)
                    ) {
                        OutlinedTextField(
                            value = text,
                            onValueChange = { text = it },
                            label = { Text("Task") },
                            modifier = Modifier.fillMaxWidth()
                        )

                        // Priority selection
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            RadioButton(
                                selected = priority == 1,
                                onClick = { priority = 1 }
                            )
                            Text("High", Modifier.align(Alignment.CenterVertically))

                            RadioButton(
                                selected = priority == 2,
                                onClick = { priority = 2 }
                            )
                            Text("Medium", Modifier.align(Alignment.CenterVertically))

                            RadioButton(
                                selected = priority == 3,
                                onClick = { priority = 3 }
                            )
                            Text("Low", Modifier.align(Alignment.CenterVertically))
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End
                        ) {
                            Button(
                                onClick = {
                                    entity.updateComponent<TodoUIStateComponent> {
                                        copy(editMode = false)
                                    }
                                },
                                modifier = Modifier.padding(end = 8.dp)
                            ) {
                                Text("Cancel")
                            }

                            Button(
                                onClick = {
                                    entity.updateComponent<TodoTextComponent> {
                                        copy(text = text)
                                    }
                                    entity.updateComponent<TodoPriorityComponent> {
                                        copy(priority = priority)
                                    }
                                    entity.updateComponent<TodoUIStateComponent> {
                                        copy(editMode = false)
                                    }
                                }
                            ) {
                                Text("Save")
                            }
                        }
                    }
                } else {
                    // View mode
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 40.dp, top = 8.dp)
                    ) {
                        Text(
                            text = "Category: ${categoryComponent?.category ?: "None"}",
                            style = MaterialTheme.typography.bodyMedium
                        )

                        Text(
                            text = "Priority: ${
                                when (priorityComponent.priority) {
                                    1 -> "High"
                                    2 -> "Medium"
                                    else -> "Low"
                                }
                            }",
                            style = MaterialTheme.typography.bodyMedium
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End
                        ) {
                            Button(
                                onClick = {
                                    entity.updateComponent<TodoUIStateComponent> {
                                        copy(editMode = true)
                                    }
                                },
                                modifier = Modifier.padding(end = 8.dp)
                            ) {
                                Text("Edit")
                            }

                            Button(
                                onClick = {
                                    World.destroyEntity(entity)
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.error
                                )
                            ) {
                                Text("Delete")
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AddTodoButton() {
    var showDialog by remember { mutableStateOf(false) }

    FloatingActionButton(
        onClick = { showDialog = true }
    ) {
        Icon(
            imageVector = Icons.Default.Add,
            contentDescription = null
        )
    }

    if (showDialog) {
        var text by remember { mutableStateOf("") }
        var priority by remember { mutableIntStateOf(2) }
        var category by remember { mutableStateOf("General") }

        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text("Add Todo") },
            text = {
                Column {
                    OutlinedTextField(
                        value = text,
                        onValueChange = { text = it },
                        label = { Text("Task") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = category,
                        onValueChange = { category = it },
                        label = { Text("Category") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp)
                    )

                    Text(
                        text = "Priority:",
                        style = MaterialTheme.typography.labelLarge,
                        modifier = Modifier.padding(top = 8.dp)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        RadioButton(
                            selected = priority == 1,
                            onClick = { priority = 1 }
                        )
                        Text("High", Modifier.align(Alignment.CenterVertically))

                        RadioButton(
                            selected = priority == 2,
                            onClick = { priority = 2 }
                        )
                        Text("Medium", Modifier.align(Alignment.CenterVertically))

                        RadioButton(
                            selected = priority == 3,
                            onClick = { priority = 3 }
                        )
                        Text("Low", Modifier.align(Alignment.CenterVertically))
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (text.isNotBlank()) {
                            World.notifyEvent(
                                CreateTodoEvent(
                                    text = text,
                                    priority = priority,
                                    category = category
                                )
                            )
                            showDialog = false
                        }
                    }
                ) {
                    Text("Add")
                }
            },
            dismissButton = {
                Button(
                    onClick = { showDialog = false }
                ) {
                    Text("Cancel")
                }
            }
        )
    }
}


/**
 * Main Composable for the Todo App using the ECS API
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TodoApp() {
    // Initialize ECS World
    DisposableEffect(Unit) {
        World.notifyEvent(
            CreateTodoEvent(
                text = "Learn Kotlin",
                priority = 1,
                category = "General"
            )
        )
        World.notifyEvent(
            CreateTodoEvent(
                text = "Learn Compose",
                priority = 2,
                category = "General"
            )
        )
        World.notifyEvent(
            CreateTodoEvent(
                text = "Learn ECS",
                priority = 3,
                category = "General"
            )
        )

        // Initialize systems
        World.initialize(4)
        World.addSystem(TodoManagementSystem())
        World.addSystem(TodoUISystem())
        World.addSystem(TodoFilterSystem())
        World.addSystem(TodoDebuger())

        onDispose {
            World.shutdown()
        }
    }

    // Material theme for the app
    MaterialTheme {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Todo App with ECS") },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                )
            },
            floatingActionButton = {
                AddTodoButton()
            }
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp)
            ) {
                FilterBar()
                TodoList()
            }
        }
    }
}