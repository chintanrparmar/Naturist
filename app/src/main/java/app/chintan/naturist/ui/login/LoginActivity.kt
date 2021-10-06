package app.chintan.naturist.ui.login

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View.GONE
import android.view.View.VISIBLE
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import app.chintan.naturist.MainActivity
import app.chintan.naturist.R
import app.chintan.naturist.databinding.ActivityLoginBinding
import app.chintan.naturist.model.UserRole
import app.chintan.naturist.util.Constants.ROLE_KEY
import app.chintan.naturist.util.FirebaseUserManager
import app.chintan.naturist.util.State
import app.chintan.naturist.util.UserSessionManager
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.common.api.ApiException
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class LoginActivity : AppCompatActivity() {

    @Inject
    lateinit var prefDataStore: DataStore<Preferences>

    private lateinit var binding: ActivityLoginBinding

    private val loginViewModel: LoginViewModel by viewModels()

    private lateinit var userSessionManager: UserSessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        userSessionManager = UserSessionManager(this)


        setUpOnClick()
        setUpObserver()
    }


    private fun setUpOnClick() {
        binding.googleSignInBt.setOnClickListener { checkRoleAndSignIn() }
    }

    private fun setUpObserver() {
        loginViewModel.firebaseAuthLiveData.observe(this, Observer {
            when (it) {
                is State.Success -> updateUserRole()
                is State.Error -> Toast.makeText(applicationContext,
                    "Login Failed",
                    Toast.LENGTH_SHORT).show()
                is State.Loading -> binding.progressBar.visibility = VISIBLE
            }

        })

        loginViewModel.updateUserRoleLiveData.observe(this, Observer {
            when (it) {
                is State.Success -> {
                    goToHomeUI()
                    binding.progressBar.visibility = GONE
                }
                is State.Error -> {
                    Toast.makeText(applicationContext, "Login Failed", Toast.LENGTH_SHORT).show()
                    binding.progressBar.visibility = GONE
                }
                is State.Loading -> binding.progressBar.visibility = VISIBLE
            }

        })
    }

    private fun saveRoleLocally(role: UserRole) {
        lifecycleScope.launch(Dispatchers.IO) {
            prefDataStore.edit { userDetails ->
                userDetails[ROLE_KEY] = role.name
            }
        }
    }

    private fun checkRoleAndSignIn() {
        if (binding.userRoleRg.checkedRadioButtonId == -1) {
            Toast.makeText(applicationContext, "Please select a role", Toast.LENGTH_SHORT).show()
            return
        }
        signIn()
    }


    private val signInLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { res ->
        this.onSignInResult(res)
    }

    private fun signIn() {
        //signIn Intent
        val signInIntent = userSessionManager.googleSignInClient.signInIntent
        signInLauncher.launch(signInIntent)
    }

    private fun onSignInResult(res: ActivityResult) {
        // Result returned from launching the Intent
        if (res.resultCode == Activity.RESULT_OK) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(res.data)
            try {
                // Google Sign In was successful, authenticate with Firebase
                val account = task.getResult(ApiException::class.java)!!
                Log.d(TAG, "firebaseAuthWithGoogle:" + account.id)
                loginViewModel.firebaseAuthWithGoogle(account.idToken!!)
            } catch (e: ApiException) {
                // Google Sign In failed, update UI appropriately
                Log.w(TAG, "Google sign in failed", e)

            }
        }

    }


    override fun onStart() {
        super.onStart()
        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUserManager.getCurrentUser()?.let {
            goToHomeUI()
        }
    }

    private fun updateUserRole() {
        when (binding.userRoleRg.checkedRadioButtonId) {
            R.id.adminRb -> {
                loginViewModel.updateUserRole(UserRole.ADMIN)
                saveRoleLocally(UserRole.ADMIN)
            }
            R.id.userRb -> {
                loginViewModel.updateUserRole(UserRole.USER)
                saveRoleLocally(UserRole.USER)
            }
        }
    }

    private fun goToHomeUI() {
        startActivity(Intent(this@LoginActivity, MainActivity::class.java))
        finish()
    }

    companion object {
        private const val TAG = "LoginActivity"
    }
}