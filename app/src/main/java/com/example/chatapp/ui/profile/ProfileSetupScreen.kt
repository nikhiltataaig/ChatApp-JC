package com.example.chatapp.ui.profile


import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil3.compose.AsyncImage
import com.example.chatapp.CommonUiEvent
import com.example.chatapp.ui.home.HomeScreenEvent
import com.example.chatapp.ui.login.LoginScreenViewModel
import com.example.chatapp.utils.Constant.PROFILE_CONTINUE_BUTTON
import com.example.chatapp.utils.Constant.PROFILE_IMAGE_BOX
import com.example.chatapp.utils.Constant.PROFILE_NAME_FIELD

@Composable
fun ProfileSetupScreen(

    navController: NavController,
    profileViewModel: ProfileViewModel
) {

    val uiState by
    profileViewModel.uiState
        .collectAsStateWithLifecycle()
    val lifecycleOwner = LocalLifecycleOwner.current
    val showLoader = remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {

        profileViewModel.uiEvents.flowWithLifecycle(
            lifecycleOwner.lifecycle,
            Lifecycle.State.CREATED
        ).collect { event ->
            showLoader.value = false
            when (event) {

                is CommonUiEvent.Navigate -> {
                    navController.navigate(
                        event.route
                    )
                }

                is CommonUiEvent.ShowLoader ->{
                    showLoader.value = true
                }

                is CommonUiEvent.PopBackStack -> {
                    navController.popBackStack()
                }

                else -> Unit
            }
        }
    }

    val imagePicker =
        rememberLauncherForActivityResult(
            ActivityResultContracts.GetContent()
        ) { uri ->

            uri?.let {

                profileViewModel.onEvent(
                    ProfileUiEvent.ImageSelected(it)
                )
            }
        }


    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {

        Text(
            text = "Set Up Your Profile",
            style = MaterialTheme.typography.headlineMedium
        )


        Spacer(
            modifier = Modifier.height(24.dp)
        )


        Box(
            modifier = Modifier
                .size(120.dp)
                .clip(CircleShape)
                .background(
                    MaterialTheme.colorScheme.surfaceVariant
                )
                .testTag(PROFILE_IMAGE_BOX)
                .clickable(
                    enabled = !showLoader.value
                ) {

                    imagePicker.launch("image/*")
                },
            contentAlignment = Alignment.Center
        ) {

            if (uiState.imageUri != null) {

                AsyncImage(
                    model = uiState.imageUri,
                    contentDescription = "Profile picture",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )

            } else {

                Text(
                    text = "Add Photo"
                )
            }
        }


        Spacer(
            modifier = Modifier.height(24.dp)
        )


        OutlinedTextField(
            value = uiState.name,
            onValueChange = {

                profileViewModel.onEvent(
                    ProfileUiEvent.NameChanged(it)
                )
            },
            label = {
                Text("Your Name")
            },
            modifier = Modifier.fillMaxWidth().testTag(PROFILE_NAME_FIELD),
            singleLine = true,
            enabled = !showLoader.value
        )


        Spacer(
            modifier = Modifier.height(20.dp)
        )


        Button(
            onClick = {
                profileViewModel.onEvent(
                    ProfileUiEvent.SaveProfileClicked
                )
            },
            modifier = Modifier.fillMaxWidth().testTag(PROFILE_CONTINUE_BUTTON),
            enabled = !showLoader.value
        ) {

            if (showLoader.value) {

                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp)
                )

            } else {

                Text(
                    text = "Continue"
                )
            }
        }


        uiState.errorMessage?.let { error ->

            Spacer(
                modifier = Modifier.height(12.dp)
            )

            Text(
                text = error,
                color = MaterialTheme.colorScheme.error
            )
        }
    }
}
