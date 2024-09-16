package com.codeasur.exp.element.design

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update

class ExampleCode {

    class CounterElement : Element<StateFlow<CounterState>, CounterEvent>() {
        override val view = CounterView()

        private val _state = MutableStateFlow(CounterState())
        override val state: StateFlow<CounterState> = _state

        override val eventSink: EventSink<CounterEvent> = EventSink { e: CounterEvent ->
            _state.update {
                when (e) {
                    is CounterEvent.Increment -> it.copy(count = it.count + 1)
                    is CounterEvent.Decrement -> it.copy(count = it.count - 1)
                }
            }
        }
    }

    data class CounterState(
        val count: Int = 0,
    )

    sealed class CounterEvent {
        data object Increment : CounterEvent()
        data object Decrement : CounterEvent()
    }

    class CounterView : View<StateFlow<CounterState>, CounterEvent> {
        @Composable
        override fun Render(state: StateFlow<CounterState>, eventSink: EventSink<CounterEvent>) {
            val counterState = state.collectAsState()
            val count = counterState.value.count

            Column(
                Modifier
                    .fillMaxSize()
                    .wrapContentSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {

                Button({
                    eventSink.onEvent(CounterEvent.Increment)
                }) {
                    Text(text = "Increment")
                }

                Text(text = "Count: $count")

                Button({
                    eventSink.onEvent(CounterEvent.Decrement)
                }) {
                    Text(text = "Decrement")
                }
            }
        }
    }

}