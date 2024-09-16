package com.codeasur.exp.example

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import com.codeasur.exp.basic.Block
import com.codeasur.exp.basic.Ui
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.serialization.Serializable

class Application : Block<Stack>(
    state = Stack(Screen.Home(HomeBlock())),
    ui = AppUi
)


sealed interface Screen {
    data class Home(val child: HomeBlock) : Screen
    data class CounterScreen(val child: CounterBlock) : Screen
}

@Serializable
data class Stack(
    private var list: List<Screen> = emptyList(),
) {
    constructor(initialScreen: Screen) : this(listOf(initialScreen))

    private val _activeScreen = MutableStateFlow(list.first())
    val activeScreen = _activeScreen

    fun push(screen: Screen) {
        list += screen
        _activeScreen.value = screen
    }

    fun pop() {
        list = list.dropLast(1)
        _activeScreen.value = list.last()
    }
}


object AppUi : Ui<Stack> {
    @Composable
    override fun Render(state: Stack) {
        val screen = state.activeScreen.collectAsState().value

        when (screen) {
            is Screen.Home -> screen.child.Render()
            is Screen.CounterScreen -> screen.child.Render()
        }
    }
}





