---
title: "Thinking about navigation logic"
---

## Idea of the framework

Basic Idea of the framework is to represent the whole application state in a tree of states (
Business Logic components).
Including the navigation state of the application.

```kotlin
import android.widget.TextView.SavedState

fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContent {
        val stackConfig = stackConfig() { config: Config -> 
            when(config) { 
                is Config.A -> {  }
                is Config.B -> {  }
            }
        }


        // stack builder 
        stack() { // ParentScope 
            default {
                
            }
            
            child<ArgClass> {
                Counter(onBackClick = { navigation.pop() })
            }
        }
    }
}


```

## How to define the child screen state

For each ui component we define a state that implements the State interface.
We create a CounterMessage for Ui to send the event with call to update function.
We handle the event message in update and update the Counter.

```kotlin


class Counter : State<CounterMessage> {
    private val _count = MutableStateFlow(0)
    val count: StateFlow<Int> = _count

    sealed interface CounterMessage {
        data object Increment : CounterMessage
        data object Decrement : CounterMessage
    }

    override fun update(message: CounterMessage) {
        when (message) {
            CounterMessage.Increment -> _count.value++
            CounterMessage.Decrement -> _count.value--
            else -> {}
        }
    }
}


@Route(Counter::class)
data class CounterRoute(val initialState: Int)


@UiContent(Counter::class)
@Composable
fun CounterUi(modifier: Modifier, counter: Counter) {
    
}

```

