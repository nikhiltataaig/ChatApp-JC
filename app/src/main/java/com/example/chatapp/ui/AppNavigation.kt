package com.example.chatapp.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.chatapp.ui.auth.AuthViewModel
import com.example.chatapp.ui.auth.LoginScreen
import com.example.chatapp.ui.auth.SignupScreen
import com.example.chatapp.ui.chat.ChatScreen
import com.example.chatapp.ui.chat.ChatUiEvent
import com.example.chatapp.ui.chat.ChatViewModel
import com.example.chatapp.ui.home.HomeScreen
import com.example.chatapp.ui.home.HomeScreenUiEvent
import com.example.chatapp.ui.home.HomeViewModel
import com.example.chatapp.ui.profile.ProfileSetupScreen
import com.example.chatapp.ui.profile.ProfileViewModel


@Composable
fun AppNavigation(
    modifier: Modifier
) {

    val navController =
        rememberNavController()

    val authViewModel: AuthViewModel = hiltViewModel()
    val profileViewModel: ProfileViewModel = hiltViewModel()


    val authUiState by
    authViewModel.uiState.collectAsStateWithLifecycle()

    NavHost(
        navController = navController,
        startDestination = "login"
    ) {

        composable("login") {

            LoginScreen(
                modifier = modifier,
                uiState = authUiState,

                onEvent = authViewModel::onEvent,

                onSignupClick = {

                    navController.navigate(
                        "signup"
                    )
                }
            )

            LaunchedEffect(
                authUiState.isLoggedIn
            ) {

                if (authUiState.isLoggedIn) {

                    navController.navigate(
                        "home"
                    ) {

                        popUpTo("login") {
                            inclusive = true
                        }
                    }
                }
            }
        }

        composable("signup") {

            SignupScreen(
                modifier = modifier,
                uiState = authUiState,

                onEvent = authViewModel::onEvent,

                onLoginClick = {

                    navController.popBackStack()
                },

                onSignupSuccess = {

                    navController.navigate(
                        "profile"
                    ) {

                        popUpTo("signup") {
                            inclusive = true
                        }
                    }
                }
            )
        }

        composable("profile") {



            val profileUiState by
            profileViewModel.uiState
                .collectAsStateWithLifecycle()

            ProfileSetupScreen(
                modifier = modifier,
                uiState = profileUiState,

                onEvent =
                    profileViewModel::onEvent,

                onProfileSaved = {

                    navController.navigate(
                        "home"
                    ) {

                        popUpTo("profile") {
                            inclusive = true
                        }
                    }
                }
            )
        }

        composable("home") {

            val homeViewModel: HomeViewModel =
                hiltViewModel()

            val uiState by homeViewModel.uiState.collectAsStateWithLifecycle()
            LaunchedEffect(Unit) {

                homeViewModel.uiEvent.collect { event ->

                    when (event) {

                        is HomeScreenUiEvent.NavigateToChat -> {

                            navController.navigate(
                                "chat/${event.chatId}/${event.userId}"
                            )
                        }

                        HomeScreenUiEvent.NavigateToNewChat -> {
                            navController.navigate("new_chat")
                        }

                        HomeScreenUiEvent.NavigateToLogin -> {
                            navController.navigate("login")
                        }

                        is HomeScreenUiEvent.ShowError -> {
                            // Handle error
                        }

                        else -> Unit
                    }
                }
            }
            HomeScreen(
                uiState = uiState,
                onEvent = homeViewModel::onEvent
            )
        }
        composable(
            route = "chat/{chatId}/{userId}"
        ) { backStackEntry ->

            val chatId =
                backStackEntry.arguments?.getString("chatId")
                    ?: return@composable

            val userId =
                backStackEntry.arguments?.getString("userId")
                    ?: return@composable

            val userName =
                backStackEntry.arguments?.getString("userName")
                    ?: ""

            val profileImageUrl =
                backStackEntry.arguments?.getString("profileImageUrl")
                    ?: ""

            val viewModel: ChatViewModel =
                hiltViewModel()

            val uiState by viewModel.uiState
                .collectAsStateWithLifecycle()

            LaunchedEffect(chatId) {
                viewModel.initialize(
                    chatId,
                    receiverId = userId
                )
            }

            LaunchedEffect(Unit) {

                viewModel.uiEvent.collect { event ->

                    when (event) {

                        ChatUiEvent.BackClicked -> {
                            navController.popBackStack()
                        }


                        else -> {}
                    }
                }
            }

            ChatScreen(
                userName = userName,
                profileImageUrl = profileImageUrl,
                uiState = uiState,
                onEvent = viewModel::onEvent
            )
        }

    }
}