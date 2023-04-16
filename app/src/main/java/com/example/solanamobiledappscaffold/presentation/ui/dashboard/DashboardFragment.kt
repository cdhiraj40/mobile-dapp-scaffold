package com.example.solanamobiledappscaffold.presentation.ui.dashboard

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.GuardedBy
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.example.solanamobiledappscaffold.databinding.FragmentDashboardBinding
import com.example.solanamobiledappscaffold.presentation.ui.extensions.showSnackbar
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

        binding.signMsgBtn.setOnClickListener {
            checkWalletConnected(view) {
                viewModel.signMessage(intentSender)
            }
        }

        binding.sendTransactionBtn.setOnClickListener {
            checkWalletConnected(view) {
            }
        }

        binding.sendVersionedTransactionBtn.setOnClickListener {
            checkWalletConnected(view) {
            }
        }

        observeViewModel()
    }

    private fun checkWalletConnected(view: View, action: () -> Unit) {
        viewModel.uiState.value.wallet?.publicKey58?.let {
            action.invoke()
        } ?: view.showSnackbar("Connect a wallet first!")
    }

    private fun observeViewModel() {
        lifecycleScope.launch {
            with(viewModel) {
                uiState.collect { uiState ->
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
