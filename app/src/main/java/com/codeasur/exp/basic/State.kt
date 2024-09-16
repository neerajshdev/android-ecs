package com.codeasur.exp.basic

interface State {
    fun create(param: Any) {}
    fun restore(savedState: SavedState) {}
    fun save(): SavedState {
        return emptyMap()
    }
}

interface ParentState : State {
}







