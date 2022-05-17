package it.polito.timebanking

import android.app.Activity
import android.app.ProgressDialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.auth.api.identity.BeginSignInRequest
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.SignInButton
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase

class LoginActivity : AppCompatActivity() {

    private lateinit var googleSignInClient: GoogleSignInClient
    private lateinit var signInRequest: BeginSignInRequest
    private var signIn = 2
    private val mAuth = Firebase.auth
    private lateinit var progressDialog: ProgressDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        FirebaseApp.initializeApp(this)
        progressDialog =
            ProgressDialog(this, androidx.transition.R.style.AlertDialog_AppCompat_Light)
        signInRequest = BeginSignInRequest.builder()
            .setGoogleIdTokenRequestOptions(
                BeginSignInRequest.GoogleIdTokenRequestOptions.builder()
                    .setSupported(true)
                    .setServerClientId(getString(R.string.firebaseClientId))
                    .setFilterByAuthorizedAccounts(true)
                    .build()
            )
            .build()
    }

    override fun onResume() {
        super.onResume()
        googleSignInClient = GoogleSignIn.getClient(
            this, GoogleSignInOptions.Builder
                (GoogleSignInOptions.DEFAULT_SIGN_IN).requestIdToken(
                getString(R.string.firebaseClientId)
            ).requestEmail()
                .build()
        )
        findViewById<SignInButton>(R.id.buttonSignIn).setOnClickListener {
            val imm = getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
            val view = currentFocus
            if (view != null) {
                imm.hideSoftInputFromWindow(view.windowToken, 0)
            }
            signIn()
        }
    }

    private fun signIn() {
        try {
            progressDialog.isIndeterminate = true
            progressDialog.setMessage("Authentication, please wait...")
            progressDialog.show()
        } catch (t: Throwable) {
            t.printStackTrace()
        }
        android.os.Handler().postDelayed(
            {
                startActivityForResult(googleSignInClient.signInIntent, signIn)
            }, 1500
        )
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        when (requestCode) {
            signIn -> {
                handleSignInResult(GoogleSignIn.getSignedInAccountFromIntent(data))
            }
        }
    }

    private fun handleSignInResult(task: Task<GoogleSignInAccount>) {
        try {
            fireBaseAuthWithGoogle(task.getResult(ApiException::class.java))
        } catch (e: ApiException) {
            e.printStackTrace()
            try {
                progressDialog.dismiss()
            } catch (t: Throwable) {
                Log.d("test", "SignIn result failed with code ${e.statusCode}")
                Toast.makeText(
                    this,
                    "Authentication failed with code ${e.statusCode}",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    private fun fireBaseAuthWithGoogle(account: GoogleSignInAccount) {
        mAuth.signInWithCredential(GoogleAuthProvider.getCredential(account.idToken, null))
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    Toast.makeText(
                        this,
                        "Welcome, ${mAuth.currentUser!!.displayName}",
                        Toast.LENGTH_LONG
                    ).show()
                    getSharedPreferences("group21.lab4.PREFERENCES", MODE_PRIVATE).edit()
                        .putString("email", account.email).apply()
                    startActivity(Intent(this, MainActivity::class.java))
                    progressDialog.dismiss()
                    finish()
                } else {
                    Log.d("test", "Sign in failed")
                }
            }
    }
}