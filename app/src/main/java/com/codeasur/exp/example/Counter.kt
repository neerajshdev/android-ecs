package com.codeasur.exp.example

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Button
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.dp
import com.codeasur.exp.basic.Block
import com.codeasur.exp.basic.State
import com.codeasur.exp.basic.Ui
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow


class CounterBlock(
    override val state: Counter = Counter(),
) : Block<Counter>(state, CounterUi)


class Counter : State {
    private val _count = MutableStateFlow(0)
    val count: StateFlow<Int> = _count

    fun increment() {
        _count.value++
    }

    fun decrement() {
        _count.value--
    }
}


object CounterUi : Ui<Counter> {

    @Composable
    override fun Render(state: Counter) {
        val count by state.count.collectAsState()
        Surface {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement =
                Arrangement.spacedBy(16.dp)
            ) {

                Button(state::increment) {
                    Text("+")
                }

                Text(text = "Count: $count")

                Button(state::decrement) {
                    Text("-")
                }
            }
        }
    }
}
