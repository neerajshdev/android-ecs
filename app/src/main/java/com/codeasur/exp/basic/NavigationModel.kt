package com.codeasur.exp.basic

import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map

interface NavigationModel: State {
    val elements : StateFlow<List<Element>>

    companion object {
        const val KEY_ELEMENTS = "elements"
    }

    class Element(
        val child: Child,
        val transitionInfo: TransitionInfo?,
    ): State {

        override fun save(): SavedState {
            return mapOf(
                "child" to when(child) {
                    is Child.Suspended -> mapOf("type" to child.type, "savedState" to child.savedState)
                    is Child.Created -> mapOf(
                        "type" to child.type,
                        "savedState" to child.state.save()
                    )
                },
            )
        }
    }


    sealed class Child: State {

        companion object {
            fun from(savedState: SavedState): Child {
                return Child.Suspended(
                    savedState["type"] as String,
                    savedState["savedState"] as SavedState
                )
            }
        }

        class Suspended(
            val type: String,
            val savedState: SavedState
        ): Child() {
            override fun save(): SavedState {
                return mapOf(
                    "type" to type,
                    "savedState" to savedState
                )
            }
        }

        class Created(
            val type: String,
            val state: State
        ): Child() {
            override fun save(): SavedState {
                return mapOf(
                    "type" to type,
                    "savedState" to state.save()
                )
            }
        }
    }

    class TransitionInfo(
        val currentProperties: Properties,
        val targetProperties: Properties,
        val progress: Float,
    )

    fun apply(operation: Operation)
    fun removeDestroyed()

    override fun save(): SavedState {
        return mapOf(
            KEY_ELEMENTS to elements.map {
            }
        )
    }

    override fun restore(savedState: SavedState) {
        savedState[KEY_ELEMENTS]?.let {

        }
    }
}

interface Operation {
    fun run(navModel: NavigationModel)
}