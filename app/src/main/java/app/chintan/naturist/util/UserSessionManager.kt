package app.chintan.naturist.util

import android.app.Activity
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions

class UserSessionManager(activity: Activity) {

    // Configure Google Sign In
    private val gso: GoogleSignInOptions =
        GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken("Web client OAuth 2.0")
            .requestEmail()
            .build()

    val googleSignInClient: GoogleSignInClient = GoogleSignIn.getClient(activity, gso)

}