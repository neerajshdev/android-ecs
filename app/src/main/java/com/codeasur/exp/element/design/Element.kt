package com.codeasur.exp.element.design

import androidx.compose.runtime.Composable


abstract class Element<S, E> {
    abstract val view: View<S, E>
    abstract val state: S
    abstract val eventSink: EventSink<E>

    @Composable
    open fun Render() {
        view.Render(state, eventSink)
    }
}


fun interface EventSink<E> {
    fun onEvent(e: E)
}

interface View<S, E> {
    @Composable
    fun Render(state: S, eventSink: EventSink<E>)
}


