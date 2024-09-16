package com.codeasur.exp.example

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.codeasur.exp.basic.Block
import com.codeasur.exp.basic.Ui

class HomeBlock: Block<HomeState>(HomeState(), HomeUi)

class HomeState

object HomeUi : Ui<HomeState> {
    @Composable
    override fun Render(state: HomeState) {
        Box(Modifier.fillMaxSize()) {
            Button({}) {
                Text("Counter")
            }
        }
    }
}