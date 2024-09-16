package com.codeasur.exp.basic

typealias SavedState = Map<String, Any>

class StateContext(
    val parent: ParentState?,
    val savedState: SavedState?,
    val stateLocals : List<StateLocal>,
) {

    interface StateLocal
    companion object {
        fun default() = StateContext(null, null, emptyList())
    }
}