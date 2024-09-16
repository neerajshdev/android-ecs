package com.codeasur.exp.nav.stack

import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import com.codeasur.exp.basic.ParentState
import com.codeasur.exp.basic.State
import kotlinx.coroutines.flow.MutableStateFlow
import kotlin.reflect.KClass

fun interface UiContent<in S : State> {
    @Composable
    operator fun invoke(modifier: Modifier, state: S)
}


/*@Composable
fun stack(block: StackConfig.() -> Unit): Stack = remember {
    Stack(StackConfig().apply(block))
}*/


/*class Stack(
    config: StackConfig,
) : ParentState {

    class UiPlusState(
        val uiContent: UiContent<State>,
        val state: State,
    ) : State

    private val contentMap = config.contents.toMap()
    private val children: MutableStateFlow<List<UiPlusState>> = MutableStateFlow(emptyList())


    private fun content(state: KClass<*>): UiContent<State> {
        return contentMap[state] ?: throw RuntimeException("No UI content found for $state")
    }

    fun push(state: State) {
        children.value += UiPlusState(
            content(state::class),
            state
        )
    }

    fun pop() {
        children.value = children.value.dropLast(1)
    }

    fun replaceTop(item: State) {
        children.value = children.value.dropLast(1) + UiPlusState(content(item::class), item)
    }

    override fun toString(): String {
        return "Stack(children=$children)"
    }
}*/


class StackConfig {
    val contents = mutableListOf<Pair<KClass<*>, UiContent<State>>>()

    inline fun <reified T : State> route(content: UiContent<T>) {
        @Suppress("UNCHECKED_CAST")
        contents.add(T::class to content as UiContent<State>)
    }
}


/*
@Composable
fun StackUI(modifier: Modifier = Modifier, stack: Stack) {
    val stateList by stack.childStates.collectAsState()

    Box(modifier) {
        for (state in stateList) {
            state.uiContent(Modifier, state)
        }
    }
}*/
