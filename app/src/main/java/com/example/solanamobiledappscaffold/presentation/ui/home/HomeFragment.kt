package com.example.solanamobiledappscaffold.presentation.ui.home

import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.drawable.AnimationDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.GuardedBy
import androidx.core.content.ContextCompat
import androidx.core.widget.TextViewCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.example.solanamobiledappscaffold.R
import com.example.solanamobiledappscaffold.databinding.FragmentHomeBinding
import com.example.solanamobiledappscaffold.domain.use_case.basic_storage.BasicPublicKeyStorageUseCase
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    private val viewModel: HomeViewModel by viewModels()

    private lateinit var basicPublicKeyStorageUseCase: BasicPublicKeyStorageUseCase

    private val activityResultLauncher: ActivityResultLauncher<Intent> =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            intentSender.onActivityComplete()
        }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)

        basicPublicKeyStorageUseCase = BasicPublicKeyStorageUseCase(requireContext())
        if (basicPublicKeyStorageUseCase.publicKey != null) {
            setConnected()
        } else {
            setDisconnected()
        }

        //
        binding.connectWallet.setOnClickListener {
            if (binding.connectWallet.text.equals(getString(R.string.connect_wallet))) {
                viewModel.connectWallet(intentSender)
            } else {
//            viewModel.requestAirdrop()
            }
        }

        val animDrawable = binding.root.background as AnimationDrawable
        animDrawable.setEnterFadeDuration(10)
        animDrawable.setExitFadeDuration(1000)
        animDrawable.start()

        observeViewModel()
        return binding.root
    }

    private fun observeViewModel() {
        lifecycleScope.launch {
            with(viewModel) {
                uiState.collect { uiState ->
                    uiState.wallet?.let {
                        if (basicPublicKeyStorageUseCase.publicKey == null) {
                            basicPublicKeyStorageUseCase.savePublicKey(
                                it.publicKey,
                            )
                        }
                        setConnected()
                    }
                }
            }
        }
    }

    private fun setConnected() {
        binding.connectionStatus.text = getString(R.string.connected)
        TextViewCompat.setCompoundDrawableTintList(
            binding.connectionStatus,
            ColorStateList.valueOf(ContextCompat.getColor(requireContext(), R.color.teal))
        )

        binding.connectWallet.text = getString(R.string.airdrop_sol)
    }

    private fun setDisconnected() {
        binding.connectionStatus.text = getString(R.string.not_connected)
        TextViewCompat.setCompoundDrawableTintList(
            binding.connectionStatus,
            ColorStateList.valueOf(ContextCompat.getColor(requireContext(), R.color.red))
        )

        binding.connectWallet.text = getString(R.string.connect_wallet)
    }

    private val intentSender = object : HomeViewModel.StartActivityForResultSender {
        @GuardedBy("this")
        private var callback: (() -> Unit)? = null

        override fun startActivityForResult(
            intent: Intent,
            onActivityCompleteCallback: () -> Unit,
        ) {
            synchronized(this) {
                check(callback == null) {
                    "Received an activity start request while another is pending"
                }
                callback = onActivityCompleteCallback
            }
            activityResultLauncher.launch(intent)
        }

        fun onActivityComplete() {
            synchronized(this) {
                callback?.let { it() }
                callback = null
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
