package com.example.chatapp.ui.auth

import android.util.Log
import android.widget.Toast
import androidx.compose.runtime.Composable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.chatapp.AppRoutes
import com.example.chatapp.CommonUiEvent
import com.example.chatapp.utils.Constant.CONFIRM_PASSWORD_TEXT_FIELD
import com.example.chatapp.utils.Constant.EMAIL_TEXT_FIELD
import com.example.chatapp.utils.Constant.PASSWORD_TEXT_FIELD
import com.example.chatapp.utils.Constant.SIGNUP_BUTTON
import kotlinx.coroutines.launch

@Composable
fun SignupScreen(
    navController: NavController,
    authViewModel: AuthViewModel,
    modifier: Modifier
) {

    val showLoader = remember  { mutableStateOf(false) }

    val lifecycleOwner = androidx.lifecycle.compose.LocalLifecycleOwner.current
    val context  = LocalContext.current
    val uiState by authViewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        authViewModel.uiEvent.flowWithLifecycle(lifecycleOwner.lifecycle, Lifecycle.State.CREATED).collect{ event ->
            showLoader.value = false;
            when(event){
            is CommonUiEvent.Navigate ->{
            if (event.route is AppRoutes.SetupProfileRoute) {
                navController.navigate(event.route){
                    popUpTo(AppRoutes.LoginRoute){
                        inclusive = true
                    }
                }
            }else{
                navController.navigate(event.route)
            }

        }
            is CommonUiEvent.ShowToast ->
            Toast.makeText(context,event.msg, Toast.LENGTH_SHORT).show()
            is CommonUiEvent.ShowLoader ->{ showLoader.value = true}
            is CommonUiEvent.DoNothing -> {
                Log.d("SignupScreen","DoNothing Called")
                showLoader.value = false
            }

            else -> {}
        }

        }

    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement =
            Arrangement.Center
    ) {

        Text(
            text = "Create Account",
            style = MaterialTheme.typography.headlineLarge
        )

        Spacer(
            modifier = Modifier.height(32.dp)
        )

        OutlinedTextField(
            value = uiState.email,
            onValueChange = {
                authViewModel.onEvent(
                    AuthEvent.EmailChanged(it)
                )
            },
            label = {
                Text("Email")
            },
            modifier = Modifier.fillMaxWidth().testTag(EMAIL_TEXT_FIELD),
            singleLine = true,
            enabled = !showLoader.value
        )

        Spacer(
            modifier = Modifier.height(12.dp)
        )

        OutlinedTextField(
            value = uiState.password,
            onValueChange = {
                authViewModel.onEvent(
                    AuthEvent.PasswordChanged(it)
                )
            },
            label = {
                Text("Password")
            },
            visualTransformation =
                PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth().testTag(PASSWORD_TEXT_FIELD),
            singleLine = true,
            enabled = !showLoader.value
        )

        Spacer(
            modifier = Modifier.height(12.dp)
        )

        OutlinedTextField(
            value = uiState.confirmPassword,
            onValueChange = {
                authViewModel.onEvent(
                    AuthEvent.ConfirmPasswordChanged(it)
                )
            },
            label = {
                Text("Confirm Password")
            },
            visualTransformation =
                PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth().testTag(CONFIRM_PASSWORD_TEXT_FIELD),
            singleLine = true,
            enabled = !showLoader.value
        )

        Spacer(
            modifier = Modifier.height(20.dp)
        )

        Button(
            onClick = {
                authViewModel.onEvent(
                    AuthEvent.SignupClicked
                )
            },
            modifier = Modifier.fillMaxWidth().testTag(SIGNUP_BUTTON),
            enabled = !showLoader.value
        ) {

            if (showLoader.value ) {

                CircularProgressIndicator()

            } else {

                Text("Create Account")
            }
        }

        TextButton(
            onClick = { authViewModel.onEvent(AuthEvent.LoginClicked) },
            enabled = !showLoader.value
        ) {

            Text(
                "Already have an account? Login"
            )
        }

        uiState.errorMessage?.let { error ->

            Text(
                text = error,
                color = MaterialTheme.colorScheme.error
            )
        }
    }
}
