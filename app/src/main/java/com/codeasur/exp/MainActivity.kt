package com.codeasur.exp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.codeasur.exp.element.design.ExampleCode
import com.codeasur.exp.example.Application

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val app = Application()
            app.Render()
        }
    }
}


@Preview
@Composable
private fun CounterPreview() {
    val element = ExampleCode.CounterElement()
    Surface {
        element.Render()
    }
}








