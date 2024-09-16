package com.codeasur.exp.basic

import androidx.compose.runtime.Composable


abstract class Block<State> (
    open val state: State,
    private val ui: Ui<State>
) {
    @Composable
    fun Render() {
        ui.Render(state)
    }
}


interface Ui<State> {
    @Composable
    fun Render(state: State)
}