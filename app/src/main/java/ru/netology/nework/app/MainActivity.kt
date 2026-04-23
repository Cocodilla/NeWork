package ru.netology.nework.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import dagger.hilt.android.AndroidEntryPoint
import ru.netology.nework.navigation.NeWorkNavGraph
import ru.netology.nework.ui.theme.NeWorkTheme

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            NeWorkTheme {
                NeWorkNavGraph()
            }
        }
    }
}
