package kabir.paisa.data

import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

object AuthRepository {

    private val auth: FirebaseAuth get() = FirebaseAuth.getInstance()
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private val _userEmail = MutableStateFlow(auth.currentUser?.email)
    val userEmail: StateFlow<String?> = _userEmail.asStateFlow()

    val isSignedIn: Boolean get() = auth.currentUser != null

    init {
        auth.addAuthStateListener { fa ->
            val user = fa.currentUser
            _userEmail.value = user?.email
            if (user != null) onSignedIn(user) else PaisaRepository.detach()
        }
        // Cover the case where the listener missed an already-restored session.
        auth.currentUser?.let(::onSignedIn)
    }

    suspend fun signIn(email: String, password: String): Result<Unit> = runCatching {
        auth.signInWithEmailAndPassword(email, password).await()
        Unit
    }

    suspend fun signUp(email: String, password: String): Result<Unit> = runCatching {
        auth.createUserWithEmailAndPassword(email, password).await()
        Unit
    }

    fun signOut() = auth.signOut()

    private fun onSignedIn(user: FirebaseUser) {
        scope.launch {
            runCatching { bootstrapUserDoc(user.uid, user.email.orEmpty()) }
            PaisaRepository.attachToUser(user.uid)
        }
    }

    private suspend fun bootstrapUserDoc(uid: String, email: String) {
        val userRef = FirebaseFirestore.getInstance().collection("users").document(uid)
        val snap = userRef.get().await()
        if (!snap.exists()) {
            userRef.set(
                User(
                    name = "",
                    email = email,
                    bank = "",
                    startingBalance = 0.0,
                    currency = "INR",
                    createdAt = Timestamp.now(),
                )
            ).await()
        }
    }
}
