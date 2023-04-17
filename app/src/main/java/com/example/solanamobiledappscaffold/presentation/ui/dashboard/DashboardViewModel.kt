package com.example.solanamobiledappscaffold.presentation.ui.dashboard

import android.content.ActivityNotFoundException
import android.net.Uri
import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.solanamobiledappscaffold.common.Resource
import com.example.solanamobiledappscaffold.domain.model.Message
import com.example.solanamobiledappscaffold.domain.model.Wallet
import com.example.solanamobiledappscaffold.domain.use_case.basic_storage.BasicWalletStorageUseCase
import com.example.solanamobiledappscaffold.domain.use_case.solana_rpc.authorize_wallet.AuthorizeWalletUseCase
import com.example.solanamobiledappscaffold.domain.use_case.solana_rpc.sign_message.SignMessageUseCase
import com.example.solanamobiledappscaffold.domain.use_case.solana_rpc.sign_transaction.SendTransactionUseCase
import com.example.solanamobiledappscaffold.domain.use_case.solana_rpc.transactions_usecase.GetLatestBlockhashUseCase
import com.example.solanamobiledappscaffold.domain.utils.toBase64
import com.example.solanamobiledappscaffold.presentation.utils.StartActivityForResultSender
import com.solana.Solana
import com.solana.api.sendRawTransaction
import com.solana.core.PublicKey
import com.solana.core.SerializeConfig
import com.solana.core.Transaction
import com.solana.mobilewalletadapter.clientlib.protocol.MobileWalletAdapterClient
import com.solana.mobilewalletadapter.clientlib.scenario.LocalAssociationIntentCreator
import com.solana.mobilewalletadapter.clientlib.scenario.LocalAssociationScenario
import com.solana.mobilewalletadapter.clientlib.scenario.Scenario
import com.solana.networking.HttpNetworkingRouter
import com.solana.networking.RPCEndpoint
import com.solana.programs.MemoProgram
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
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
    private val walletStorageUseCase: BasicWalletStorageUseCase,
    private val getLatestBlockhashUseCase: GetLatestBlockhashUseCase,
    private val signMessageUseCase: SignMessageUseCase,
    private val sendTransactionUseCase: SendTransactionUseCase,
) : ViewModel() {
    private val _uiState = MutableStateFlow(DashboardState())
    val uiState = _uiState.asStateFlow()

    private val _solana = MutableLiveData<Solana>()

    private val mobileWalletAdapterClientSem =
        Semaphore(1) // allow only a single MWA connection at a time

    init {
        _solana.value = Solana(HttpNetworkingRouter(RPCEndpoint.devnetSolana))
        if (walletStorageUseCase.publicKey58 != null && walletStorageUseCase.publicKey64 != null) {
            _uiState.value.wallet = Wallet(
                publicKey58 = walletStorageUseCase.publicKey58.toString(),
                publicKey64 = walletStorageUseCase.publicKey64.toString(),
                walletStorageUseCase.balance,
            )
        } else {
            _uiState.value.wallet = null
        }
    }

    fun signMessage(sender: StartActivityForResultSender) = viewModelScope.launch {
        localAssociateAndExecute(sender) { client ->
            when (val result = authorizeWalletUseCase(client)) {
                is Resource.Success -> {
                    Log.d(TAG, "Wallet connected: ${result.data}")

                    _uiState.update {
                        it.copy(
                            wallet = result.data,
                        )
                    }

                    walletStorageUseCase.saveWallet(
                        Wallet(
                            result.data!!.publicKey58,
                            result.data.publicKey64,
                            result.data.balance,
                        ),
                    )

                    val messages = Array(1) {
                        "Say Hello to Solana Mobile dApp Scaffold!".toByteArray()
                    }

                    when (
                        val message = signMessageUseCase(
                            client,
                            messages,
                            arrayOf(walletStorageUseCase.publicKey64!!.toBase64()),
                        )
                    ) {
                        is Resource.Success -> {
                            Message(message.data!!.signedMessage).let { signPayloadResult ->
                                _uiState.update {
                                    it.copy(
                                        signedMessage = Message(
                                            signedMessage = signPayloadResult.signedMessage,
                                        ).toString(),
                                    )
                                }
                            }
                        }

                        is Resource.Loading -> {
                        }

                        is Resource.Error -> {
                        }
                    }
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

    private suspend fun getLatestBlockhash(solana: Solana): Resource<String> {
        var blockHash: Resource<String> = Resource.Loading()
        getLatestBlockhashUseCase(solana).collect { result ->
            blockHash = when (result) {
                is Resource.Success -> {
                    result
                }
                is Resource.Loading -> {
                    result
                }
                is Resource.Error -> {
                    result
                }
            }
        }
        return blockHash
    }

    fun signTransaction(sender: StartActivityForResultSender) = viewModelScope.launch {
        _solana.value?.let { solana ->
            withContext(viewModelScope.coroutineContext + Dispatchers.IO) {
                getLatestBlockhash(solana).let { blockHash ->
                    run {
                        when (blockHash) {
                            is Resource.Success -> {
                                Log.d(TAG, "Blockhash: ${blockHash.data}")
                                localAssociateAndExecute(sender) { client ->
                                    when (val result = authorizeWalletUseCase(client)) {
                                        is Resource.Success -> {
                                            Log.d(TAG, "Wallet connected: ${result.data}")

                                            _uiState.update {
                                                it.copy(
                                                    wallet = result.data,
                                                )
                                            }

                                            walletStorageUseCase.saveWallet(
                                                Wallet(
                                                    result.data!!.publicKey58,
                                                    result.data.publicKey64,
                                                    result.data.balance,
                                                ),
                                            )

                                            val instruction = MemoProgram.writeUtf8(
                                                PublicKey(walletStorageUseCase.publicKey58!!),
                                                "Say Hello to Solana Mobile dApp Scaffold!",
                                            )

                                            val transaction = Transaction()
                                            transaction.setRecentBlockHash(blockHash.data!!)
                                            transaction.addInstruction(instruction)
                                            transaction.feePayer =
                                                PublicKey(walletStorageUseCase.publicKey58!!)

                                            val transactions = Array(1) {
                                                transaction.serialize(
                                                    config = SerializeConfig(
                                                        requireAllSignatures = false,
                                                    ),
                                                )
                                            }

                                            when (
                                                val message = sendTransactionUseCase(
                                                    client,
                                                    transactions,
                                                )
                                            ) {
                                                is Resource.Success -> {
                                                    com.example.solanamobiledappscaffold.domain.model.Transaction(
                                                        message.data!!.signedTransaction,
                                                    ).let { transaction ->

                                                        // TODO: convert to usecase
                                                        solana.api.sendRawTransaction(message.data.signedTransaction)
                                                            .onSuccess { transactionID ->
                                                                Log.d(
                                                                    TAG,
                                                                    "Transaction sent: $transactionID",
                                                                )
                                                                _uiState.update {
                                                                    it.copy(
                                                                        transactionID = transactionID,
                                                                    )
                                                                }
                                                            }
                                                            .onFailure {
                                                                Log.d(
                                                                    TAG,
                                                                    it.localizedMessage
                                                                        ?: it.message.toString(),
                                                                )
                                                            }

                                                        Log.d(
                                                            TAG,
                                                            "Transaction: ${
                                                                com.example.solanamobiledappscaffold.domain.model.Transaction(
                                                                    signedTransaction = transaction.signedTransaction,
                                                                )
                                                            }",
                                                        )
                                                    }
                                                }

                                                is Resource.Loading -> {
                                                }

                                                is Resource.Error -> {
                                                    Log.e(TAG, message.message.toString())
                                                }
                                            }
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
                            is Resource.Loading -> {
                            }
                            is Resource.Error -> {
                                Log.e(TAG, "Fetch blockhash failed")
                                _uiState.value = DashboardState(
                                    error = blockHash.message
                                        ?: "An unexpected error occurred",
                                    isLoading = false,
                                )
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
        private const val TAG = "DashboardViewModel"
        private const val LOCAL_ASSOCIATION_START_TIMEOUT_MS = 60000L
        private const val LOCAL_ASSOCIATION_CLOSE_TIMEOUT_MS = 5000L
        private const val LOCAL_ASSOCIATION_CANCEL_AFTER_WALLET_CLOSED_TIMEOUT_MS = 5000L
    }
}
