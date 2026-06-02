package kabir.paisa

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import kabir.paisa.nav.PaisaNavHost
import kabir.paisa.ui.theme.PaisaTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            PaisaTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    PaisaNavHost()
                }
            }
        }
    }
}
