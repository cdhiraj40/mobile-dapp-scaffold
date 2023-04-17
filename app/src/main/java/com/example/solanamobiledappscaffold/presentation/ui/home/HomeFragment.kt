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
import com.example.solanamobiledappscaffold.common.Constants.TWITTER_SHARE_URL
import com.example.solanamobiledappscaffold.common.Constants.formatAddress
import com.example.solanamobiledappscaffold.databinding.FragmentHomeBinding
import com.example.solanamobiledappscaffold.presentation.ui.extensions.copyToClipboard
import com.example.solanamobiledappscaffold.presentation.ui.extensions.openInBrowser
import com.example.solanamobiledappscaffold.presentation.ui.extensions.showSnackbar
import com.example.solanamobiledappscaffold.presentation.utils.StartActivityForResultSender
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

        val animDrawable = binding.root.background as AnimationDrawable
        animDrawable.setEnterFadeDuration(10)
        animDrawable.setExitFadeDuration(1000)
        animDrawable.start()

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.walletBtn.text = viewModel.getWalletButtonText(requireContext())

        binding.walletBtn.setOnClickListener {
            // TODO: open modal showing two things, copy and disconnect
            viewModel.interactWallet(intentSender)
        }

        // action based on the button text
        binding.airdropBtn.setOnClickListener {
            viewModel.uiState.value.wallet?.let {
                viewModel.requestAirdrop()
            } ?: view.showSnackbar(
                "Connect a wallet first!",
            )
        }

        binding.copyBtn.setOnClickListener {
            requireContext().copyToClipboard(
                "Wallet address",
                viewModel.uiState.value.wallet?.publicKey58 ?: "",
            ).let {
                view.showSnackbar("Copied to clipboard!")
            }
        }

        binding.buildDappsBtn.setOnClickListener {
            requireContext().openInBrowser(TWITTER_SHARE_URL)
        }

        observeViewModel()
    }

    override fun onResume() {
        super.onResume()

        viewModel.getBalance()
    }

    private fun observeViewModel() {
        lifecycleScope.launch {
            with(viewModel) {
                uiState.collect { uiState ->
                    uiState.wallet?.let {
                        connectWallet(it.publicKey58)
                    } ?: run {
                        clearWallet()
                        disconnectWallet()
                    }

                    uiState.balance.let {
                        binding.balanceTv.text = String.format(
                            resources.getString(R.string.wallet_balance),
                            it,
                        )
                    }

                    // TODO: show snackbar, extension
//                    uiState.error.let {
//                        requireView().showSnackbar(
//                            it,
//                        )
//                    }
                }
            }
        }
    }

    private fun connectWallet(publicKey: String) {
        // show the copy button
        binding.copyBtn.visibility = View.VISIBLE

        binding.walletBtn.text = formatAddress(publicKey)
        binding.walletBtn.setTextColor(
            ContextCompat.getColor(requireContext(), R.color.black),
        )

        binding.walletBtn.setBackgroundColor(
            ContextCompat.getColor(requireContext(), R.color.solana_green),
        )

        binding.walletBtn.iconTint =
            ColorStateList.valueOf(ContextCompat.getColor(requireContext(), R.color.teal))

        binding.airdropBtn.setBackgroundColor(
            ContextCompat.getColor(requireContext(), R.drawable.text_background as Int),
        )
    }

    private fun disconnectWallet() {
        // hide the copy button
        binding.copyBtn.visibility = View.GONE

        binding.walletBtn.text = getString(R.string.select_wallet)
        binding.walletBtn.setTextColor(
            ContextCompat.getColor(requireContext(), R.color.white),
        )
        binding.walletBtn.setBackgroundColor(
            ContextCompat.getColor(requireContext(), R.color.black),
        )

        binding.walletBtn.iconTint =
            ColorStateList.valueOf(ContextCompat.getColor(requireContext(), R.color.red))

        binding.airdropBtn.setBackgroundColor(
            ContextCompat.getColor(requireContext(), R.color.dark_gray),
        )
    }

    private val intentSender = object : StartActivityForResultSender {
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
