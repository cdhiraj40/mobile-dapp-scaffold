package com.example.solanamobiledappscaffold.presentation.ui.dashboard

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.solanamobiledappscaffold.common.Resource
import com.example.solanamobiledappscaffold.domain.use_case.authorize_wallet.AuthorizeWalletUseCase
import com.solana.mobilewalletadapter.clientlib.protocol.MobileWalletAdapterClient
import com.solana.mobilewalletadapter.clientlib.scenario.LocalAssociationIntentCreator
import com.solana.mobilewalletadapter.clientlib.scenario.LocalAssociationScenario
import com.solana.mobilewalletadapter.clientlib.scenario.Scenario
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit
import java.util.concurrent.CancellationException
import java.util.concurrent.ExecutionException
import java.util.concurrent.TimeUnit
import java.util.concurrent.TimeoutException
import javax.inject.Inject

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val authorizeWalletUseCase: AuthorizeWalletUseCase,
) : ViewModel() {
    private val _uiState = MutableStateFlow(DashboardState())
    val uiState = _uiState.asStateFlow()

    private val mobileWalletAdapterClientSem =
        Semaphore(1) // allow only a single MWA connection at a time

    fun connectWallet(sender: StartActivityForResultSender) = viewModelScope.launch {
        localAssociateAndExecute(sender) { client ->

            when (val result = authorizeWalletUseCase(client)) {
                is Resource.Success -> {
                    Log.d(TAG, "Wallet connected: ${result.data}")
                    _uiState.value = DashboardState(
                        wallet = result.data,
                        isLoading = false,
                    )
                }
                is Resource.Loading -> {
                    _uiState.value = DashboardState(
                        isLoading = true,
                    )
                }
                is Resource.Error -> {
                    Log.e(TAG, "Authorization failed")
                    _uiState.value = DashboardState(
                        error = result.message
                            ?: "An unexpected error occurred",
                        isLoading = false,
                    )
                }
            }
        }
    }

    private suspend fun <T> localAssociateAndExecute(
        sender: StartActivityForResultSender,
        uriPrefix: Uri? = null,
        action: suspend (MobileWalletAdapterClient) -> T?,
    ): T? = coroutineScope {
        return@coroutineScope mobileWalletAdapterClientSem.withPermit {
            val localAssociation = LocalAssociationScenario(Scenario.DEFAULT_CLIENT_TIMEOUT_MS)

            val associationIntent = LocalAssociationIntentCreator.createAssociationIntent(
                uriPrefix,
                localAssociation.port,
                localAssociation.session,
            )
            try {
                sender.startActivityForResult(associationIntent) {
                    viewModelScope.launch {
                        // Ensure this coroutine will wrap up in a timely fashion when the launched
                        // activity completes
                        delay(LOCAL_ASSOCIATION_CANCEL_AFTER_WALLET_CLOSED_TIMEOUT_MS)
                        this@coroutineScope.cancel()
                    }
                }
            } catch (e: ActivityNotFoundException) {
                Log.e(TAG, "Failed to start intent=$associationIntent", e)
//                Toast.makeText(sender as Context, "msg_wallet_not_found", Toast.LENGTH_LONG).show()
                return@withPermit null
            }

            return@withPermit withContext(Dispatchers.IO) {
                try {
                    val mobileWalletAdapterClient = try {
                        runInterruptible {
                            localAssociation.start()
                                .get(LOCAL_ASSOCIATION_START_TIMEOUT_MS, TimeUnit.MILLISECONDS)
                        }
                    } catch (e: InterruptedException) {
                        Log.w(TAG, "Interrupted while waiting for local association to be ready")
                        return@withContext null
                    } catch (e: TimeoutException) {
                        Log.e(TAG, "Timed out waiting for local association to be ready")
                        return@withContext null
                    } catch (e: ExecutionException) {
                        Log.e(TAG, "Failed establishing local association with wallet", e.cause)
                        return@withContext null
                    } catch (e: CancellationException) {
                        Log.e(TAG, "Local association was cancelled before connected", e)
                        return@withContext null
                    }

                    // NOTE: this is a blocking method call, appropriate in the Dispatchers.IO context
                    action(mobileWalletAdapterClient)
                } finally {
                    // running in Dispatchers.IO; blocking is appropriate
                    @Suppress("BlockingMethodInNonBlockingContext")
                    localAssociation.close()
                        .get(LOCAL_ASSOCIATION_CLOSE_TIMEOUT_MS, TimeUnit.MILLISECONDS)
                }
            }
        }
    }

    interface StartActivityForResultSender {
        fun startActivityForResult(
            intent: Intent,
            onActivityCompleteCallback: () -> Unit,
        ) // throws ActivityNotFoundException
    }

    companion object {
        private const val TAG = "DashboardViewModel"
        private const val LOCAL_ASSOCIATION_START_TIMEOUT_MS = 60000L
        private const val LOCAL_ASSOCIATION_CLOSE_TIMEOUT_MS = 5000L
        private const val LOCAL_ASSOCIATION_CANCEL_AFTER_WALLET_CLOSED_TIMEOUT_MS = 5000L
    }
}
