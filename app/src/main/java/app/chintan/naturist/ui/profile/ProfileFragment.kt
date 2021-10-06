package app.chintan.naturist.ui.profile

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import app.chintan.naturist.databinding.FragmentProfileBinding
import app.chintan.naturist.ui.login.LoginActivity
import app.chintan.naturist.util.FirebaseUserManager
import app.chintan.naturist.util.UserSessionManager
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null

    private lateinit var userSessionManager: UserSessionManager

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        userSessionManager = UserSessionManager(requireActivity())
        setUpOnClick()
    }

    private fun setUpOnClick() {
        binding.logoutBt.setOnClickListener { signOut() }
    }

    private fun signOut() {
        // Firebase sign out
        FirebaseUserManager.getAuth().signOut()

        // Google sign out
        userSessionManager.googleSignInClient.signOut()

        startActivity(Intent(requireActivity(), LoginActivity::class.java))
        requireActivity().finish()
    }
}