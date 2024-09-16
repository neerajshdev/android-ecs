package com.codeasur.exp.basic

import kotlinx.coroutines.flow.StateFlow

data class Stack <Key>(
    val context: StateContext,
    val childConfig: ChildConfig<Key>.() -> Unit,
) {
    val backStack = BackStack(childConfig)

    fun push(element: State) {
    }
}



class ChildConfig<Target>() {
    fun target(target: Target) {}
}

class BackStack<T>(config: ChildConfig<T>.() -> Unit) : NavigationModel {
    override val elements: StateFlow<List<NavigationModel.Element>>
        get() = TODO("Not yet implemented")


    init {
        val childConfig = ChildConfig<T>().apply(config)
    }

    override fun apply(operation: Operation) {
        TODO("Not yet implemented")
    }

    override fun removeDestroyed() {
        TODO("Not yet implemented")
    }
}