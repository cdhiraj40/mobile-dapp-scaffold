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
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.example.solanamobiledappscaffold.R
import com.example.solanamobiledappscaffold.common.Constants.formatAddress
import com.example.solanamobiledappscaffold.databinding.FragmentHomeBinding
import com.example.solanamobiledappscaffold.domain.use_case.basic_storage.BasicPublicKeyStorageUseCase
import com.example.solanamobiledappscaffold.presentation.ui.extensions.showSnackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    private val viewModel: HomeViewModel by viewModels()

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

        binding.walletBtn.text = viewModel.getWalletButtonText(requireContext())

        val animDrawable = binding.root.background as AnimationDrawable
        animDrawable.setEnterFadeDuration(10)
        animDrawable.setExitFadeDuration(1000)
        animDrawable.start()

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.walletBtn.setOnClickListener {
            // TODO: open modal showing two things, copy and disconnect
            viewModel.interactWallet(intentSender)
        }

        // action based on the button text
        binding.airdropBtn.setOnClickListener {
            viewModel.uiState.value.wallet?.let {
                viewModel.requestAirdrop()
            } ?: view.showSnackbar(
                "Connect a wallet first!"
            )
        }

        observeViewModel()
    }

    private fun observeViewModel() {
        lifecycleScope.launch {
            with(viewModel) {
                uiState.collect { uiState ->
                    uiState.wallet?.let {
                        connectWallet(it.publicKey)
                    } ?: disconnectWallet()

                    uiState.balance.let {
                        binding.balanceTv.text = String.format(
                            resources.getString(R.string.wallet_balance),
                            it
                        )
                    }

                    // TODO: show snackbar, extension
//                    uiState.error.let {
//                        binding.errors.text = it
//                    }
                }
            }
        }
    }


    private fun connectWallet(publicKey: String) {
        binding.walletBtn.text = formatAddress(publicKey)
        binding.walletBtn.setTextColor(
            ContextCompat.getColor(requireContext(), R.color.black)
        )

        binding.walletBtn.setBackgroundColor(
            ContextCompat.getColor(requireContext(), R.color.solana_green)
        )

        binding.walletBtn.iconTint =
            ColorStateList.valueOf(ContextCompat.getColor(requireContext(), R.color.teal))

        binding.airdropBtn.setBackgroundColor(
            ContextCompat.getColor(requireContext(), R.drawable.text_background as Int)
        )
    }

    private fun disconnectWallet() {
        binding.walletBtn.text = getString(R.string.select_wallet)
        binding.walletBtn.setTextColor(
            ContextCompat.getColor(requireContext(), R.color.white)
        )
        binding.walletBtn.setBackgroundColor(
            ContextCompat.getColor(requireContext(), R.color.black)
        )

        binding.walletBtn.iconTint =
            ColorStateList.valueOf(ContextCompat.getColor(requireContext(), R.color.red))

        binding.airdropBtn.setBackgroundColor(
            ContextCompat.getColor(requireContext(), R.color.dark_gray)
        )
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
