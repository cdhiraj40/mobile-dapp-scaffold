package com.example.solanamobiledappscaffold.presentation.ui.home

import android.content.ActivityNotFoundException
import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.solanamobiledappscaffold.R
import com.example.solanamobiledappscaffold.common.Constants
import com.example.solanamobiledappscaffold.common.Constants.formatBalance
import com.example.solanamobiledappscaffold.common.Resource
import com.example.solanamobiledappscaffold.domain.model.Wallet
import com.example.solanamobiledappscaffold.domain.use_case.basic_storage.BasicWalletStorageUseCase
import com.example.solanamobiledappscaffold.domain.use_case.solana_rpc.authorize_wallet.AuthorizeWalletUseCase
import com.example.solanamobiledappscaffold.domain.use_case.solana_rpc.transactions_usecase.BalanceUseCase
import com.example.solanamobiledappscaffold.domain.use_case.solana_rpc.transactions_usecase.RequestAirdropUseCase
import com.example.solanamobiledappscaffold.presentation.utils.StartActivityForResultSender
import com.solana.Solana
import com.solana.core.PublicKey
import com.solana.mobilewalletadapter.clientlib.protocol.MobileWalletAdapterClient
import com.solana.mobilewalletadapter.clientlib.scenario.LocalAssociationIntentCreator
import com.solana.mobilewalletadapter.clientlib.scenario.LocalAssociationScenario
import com.solana.mobilewalletadapter.clientlib.scenario.Scenario
import com.solana.networking.Commitment
import com.solana.networking.HttpNetworkingRouter
import com.solana.networking.RPCEndpoint
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit
import java.math.BigDecimal
import java.util.concurrent.CancellationException
import java.util.concurrent.ExecutionException
import java.util.concurrent.TimeUnit
import java.util.concurrent.TimeoutException
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val authorizeWalletUseCase: AuthorizeWalletUseCase,
    private val requestAirdropUseCase: RequestAirdropUseCase,
    private val balanceUseCase: BalanceUseCase,
    private val walletStorageUseCase: BasicWalletStorageUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeState())
    val uiState = _uiState.asStateFlow()

    private val _solana = MutableLiveData<Solana>()

    private val mobileWalletAdapterClientSem =
        Semaphore(1) // allow only a single MWA connection at a time

    init {
        _solana.value = Solana(HttpNetworkingRouter(RPCEndpoint.devnetSolana))
        if (walletStorageUseCase.publicKey58 != null && walletStorageUseCase.publicKey64 != null) {
            _uiState.value.wallet = Wallet(
                walletStorageUseCase.publicKey58.toString(),
                walletStorageUseCase.publicKey64.toString(),
            )
            getBalance()
        } else {
            _uiState.value.wallet = null
        }
    }

    fun interactWallet(sender: StartActivityForResultSender) {
        if (walletStorageUseCase.publicKey58 == null) {
            connectWallet(sender)
        } else {
            clearWallet()
        }
    }

    private fun connectWallet(sender: StartActivityForResultSender) = viewModelScope.launch {
        localAssociateAndExecute(sender) { client ->

            when (val result = authorizeWalletUseCase(client)) {
                is Resource.Success -> {
                    Log.d(TAG, "Wallet connected: ${result.data}")
                    _uiState.value = HomeState(
                        wallet = result.data,
                        isLoading = false,
                    )

                    walletStorageUseCase.saveWallet(
                        result.data!!,
                    )

                    getBalance()
                }
                is Resource.Loading -> {
                    _uiState.value = HomeState(
                        isLoading = true,
                    )
                }
                is Resource.Error -> {
                    Log.e(TAG, "Authorization failed")
                    _uiState.value = HomeState(
                        error = result.message
                            ?: "An unexpected error occurred",
                        isLoading = false,
                    )
                }
            }
        }
    }

    fun getBalance() {
        viewModelScope.launch {
            _solana.value?.let {
                withContext(Dispatchers.IO) {
                    walletStorageUseCase.publicKey58?.let { publicKey ->
                        balanceUseCase(
                            it,
                            PublicKey(publicKey),
                            Commitment.CONFIRMED,
                        ).collect { result ->
                            when (result) {
                                is Resource.Success -> {
                                    // save balance to storage
                                    walletStorageUseCase.updateBalance(
                                        result.data!!.toString(),
                                    )

                                    _uiState.update {
                                        it.copy(
                                            balance = formatBalance(result.data),
                                        )
                                    }
                                }

                                is Resource.Error -> {
                                    _uiState.update {
                                        it.copy(
                                            error = result.message
                                                ?: "An unexpected error occurred!",
                                        )
                                    }
                                }

                                is Resource.Loading -> {
                                    _uiState.update {
                                        it.copy(
                                            isLoading = true,
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    fun getWalletButtonText(context: Context): String {
        val publicKey = walletStorageUseCase.publicKey58
        return if (publicKey != null) {
            Constants.formatAddress(publicKey.toString())
        } else {
            context.getString(R.string.select_wallet)
        }
    }

    fun clearWallet() {
        walletStorageUseCase.clearWallet()
        _uiState.update {
            it.copy(
                wallet = null,
                balance = BigDecimal(0),
            )
        }
    }

    fun requestAirdrop() {
        viewModelScope.launch {
            _solana.value?.let {
                withContext(Dispatchers.IO) {
                    walletStorageUseCase.publicKey58?.let { publicKey ->
                        requestAirdropUseCase(
                            it,
                            PublicKey(publicKey),
                        ).collect { result ->
                            when (result) {
                                is Resource.Success -> {
                                    getBalance()
                                }
                                is Resource.Error -> {
                                    _uiState.update {
                                        it.copy(
                                            error = result.message
                                                ?: "An unexpected error occurred!",
                                        )
                                    }
                                }
                                is Resource.Loading -> {
                                    _uiState.update {
                                        it.copy(
                                            isLoading = true,
                                        )
                                    }
                                }
                            }
                        }
                    }
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

    companion object {
        private const val TAG = "HomeViewModel"
        private const val LOCAL_ASSOCIATION_START_TIMEOUT_MS = 60000L
        private const val LOCAL_ASSOCIATION_CLOSE_TIMEOUT_MS = 5000L
        private const val LOCAL_ASSOCIATION_CANCEL_AFTER_WALLET_CLOSED_TIMEOUT_MS = 5000L
    }
}
