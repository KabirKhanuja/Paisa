package kabir.paisa

import android.app.Application
import com.google.firebase.FirebaseApp

class PaisaApp : Application() {
    override fun onCreate() {
        super.onCreate()
        // Initialize Firebase. If google-services.json is missing or misconfigured, this
        // is a no-op-with-warning rather than a crash on most builds.
        runCatching { FirebaseApp.initializeApp(this) }
    }
}
