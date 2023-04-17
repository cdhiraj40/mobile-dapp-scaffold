package com.example.solanamobiledappscaffold.presentation.ui.dashboard

import android.content.Intent
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
import com.example.solanamobiledappscaffold.common.Constants.getSolanaExplorerUrl
import com.example.solanamobiledappscaffold.databinding.FragmentDashboardBinding
import com.example.solanamobiledappscaffold.presentation.ui.extensions.copyToClipboard
import com.example.solanamobiledappscaffold.presentation.ui.extensions.openInBrowser
import com.example.solanamobiledappscaffold.presentation.ui.extensions.showSnackbar
import com.example.solanamobiledappscaffold.presentation.ui.extensions.showSnackbarWithAction
import com.example.solanamobiledappscaffold.presentation.utils.StartActivityForResultSender
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class DashboardFragment : Fragment() {

    private var _binding: FragmentDashboardBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    private val viewModel: DashboardViewModel by viewModels()

    private val activityResultLauncher: ActivityResultLauncher<Intent> =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            intentSender.onActivityComplete()
        }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentDashboardBinding.inflate(inflater, container, false)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        checkWalletConnected({
            enableWallet()
        }, {
            disableWallet()
        })

        binding.signMsgBtn.setOnClickListener {
            checkWalletConnected(view) {
                viewModel.signMessage(intentSender)
            }
        }

        binding.sendTransactionBtn.setOnClickListener {
            checkWalletConnected(view) {
                viewModel.signTransaction(intentSender)
            }
        }

        binding.sendVersionedTransactionBtn.setOnClickListener {
            requireView().showSnackbar("Coming soon! :)")
        }

        observeViewModel()
    }

    private fun checkWalletConnected(view: View, action: () -> Unit) {
        viewModel.uiState.value.wallet?.publicKey58?.let {
            action.invoke()
        } ?: view.showSnackbar("Connect a wallet first!")
    }

    private fun checkWalletConnected(
        positiveAction: () -> Unit,
        negativeAction: () -> Unit,
    ) {
        viewModel.uiState.value.wallet?.publicKey58?.let {
            positiveAction.invoke()
        } ?: negativeAction.invoke()
    }

    private fun enableWallet() {
        binding.signMsgBtn.setTextColor(
            ContextCompat.getColor(requireContext(), R.color.black),
        )

        binding.sendTransactionBtn.setTextColor(
            ContextCompat.getColor(requireContext(), R.color.black),
        )

        binding.signMsgBtn.setBackgroundColor(
            ContextCompat.getColor(requireContext(), R.color.solana_green),
        )

        binding.sendTransactionBtn.setBackgroundColor(
            ContextCompat.getColor(requireContext(), R.color.solana_green),
        )
    }

    private fun disableWallet() {
        binding.signMsgBtn.setTextColor(
            ContextCompat.getColor(requireContext(), R.color.white),
        )

        binding.sendTransactionBtn.setTextColor(
            ContextCompat.getColor(requireContext(), R.color.white),
        )

        binding.signMsgBtn.setBackgroundColor(
            ContextCompat.getColor(requireContext(), R.color.dark_gray),
        )

        binding.sendTransactionBtn.setBackgroundColor(
            ContextCompat.getColor(requireContext(), R.color.dark_gray),
        )
    }

    private fun observeViewModel() {
        lifecycleScope.launch {
            with(viewModel) {
                uiState.collect { uiState ->
                    uiState.signedMessage?.let {
                        requireView().showSnackbarWithAction("Signed message: $it") {
                            requireContext().copyToClipboard(text = it)
                            requireView().showSnackbar("Copied to clipboard")
                        }
                    }

                    uiState.transactionID?.let {
                        requireView().showSnackbarWithAction("Transaction Signature: $it", "View") {
                            requireContext().openInBrowser(getSolanaExplorerUrl(it))
                        }
                    }
                }
            }
        }
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
