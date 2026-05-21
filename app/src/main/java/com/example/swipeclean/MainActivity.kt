package com.example.swipeclean

import android.app.Activity
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.lifecycleScope
import com.example.swipeclean.ui.AppRootViewModel
import com.example.swipeclean.ui.bin.BinDeletionRequest
import com.example.swipeclean.ui.bin.BinDeletionResult
import com.example.swipeclean.ui.bin.IntentSenderDispatcher
import com.example.swipeclean.ui.navigation.Destinations
import com.example.swipeclean.ui.navigation.SwipeCleanNavGraph
import com.example.swipeclean.ui.permission.MediaPermissionGate
import com.example.swipeclean.ui.theme.SwipeCleanTheme
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject lateinit var intentSenderDispatcher: IntentSenderDispatcher

    private val rootViewModel: AppRootViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val rootState by rootViewModel.state.collectAsStateWithLifecycle()
            SwipeCleanTheme(themeMode = rootState.themeMode) {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    val lastRequest = remember { mutableStateOf<BinDeletionRequest?>(null) }

                    val launcher = rememberLauncherForActivityResult(
                        ActivityResultContracts.StartIntentSenderForResult()
                    ) { result ->
                        val request = lastRequest.value ?: return@rememberLauncherForActivityResult
                        val confirmed = result.resultCode == Activity.RESULT_OK
                        lifecycleScope.launch {
                            intentSenderDispatcher.publishResult(
                                BinDeletionResult(
                                    tag = request.tag,
                                    mediaIds = request.mediaIds,
                                    confirmed = confirmed
                                )
                            )
                        }
                    }

                    LaunchedEffect(Unit) {
                        intentSenderDispatcher.requests.collect { request ->
                            lastRequest.value = request
                            launcher.launch(IntentSenderRequest.Builder(request.intentSender).build())
                        }
                    }

                    if (rootState.loading) return@Scaffold

                    if (!rootState.onboardingComplete) {
                        SwipeCleanNavGraph(startDestination = Destinations.ONBOARDING)
                    } else {
                        MediaPermissionGate(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(innerPadding)
                        ) {
                            SwipeCleanNavGraph(startDestination = Destinations.HOME)
                        }
                    }
                }
            }
        }
    }
}
