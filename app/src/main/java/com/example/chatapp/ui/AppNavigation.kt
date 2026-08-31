package com.example.chatapp.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.example.chatapp.AppRoutes
import com.example.chatapp.ui.auth.AuthViewModel
import com.example.chatapp.ui.auth.SignupScreen
import com.example.chatapp.ui.chat.ChatScreen
import com.example.chatapp.ui.chat.ChatViewModel
import com.example.chatapp.ui.home.HomeScreen
import com.example.chatapp.ui.home.HomeViewModel
import com.example.chatapp.ui.login.LoginScreen
import com.example.chatapp.ui.login.LoginScreenViewModel
import com.example.chatapp.ui.profile.ProfileSetupScreen
import com.example.chatapp.ui.profile.ProfileViewModel


@Composable
fun AppNavigation(
    modifier: Modifier
) {
    val navController =
        rememberNavController()

    NavHost(
        navController = navController,
        startDestination = AppRoutes.LoginRoute
    ) {
        composable<AppRoutes.LoginRoute>{

            val loginViewModel: LoginScreenViewModel = hiltViewModel()

            LoginScreen(
                modifier = modifier,
                loginViewModel = loginViewModel,
                navController = navController
            )


        }

        composable<AppRoutes.SignupRoute> {

            val authViewModel: AuthViewModel = hiltViewModel()

            SignupScreen(
                modifier = modifier,
                navController = navController,
                authViewModel = authViewModel
            )
        }

        composable<AppRoutes.SetupProfileRoute> {

            val profileViewModel: ProfileViewModel = hiltViewModel()


            ProfileSetupScreen(
               navController = navController,
                profileViewModel = profileViewModel

            )
        }

        composable<AppRoutes.HomeRoute> {

            val homeViewModel: HomeViewModel =
                hiltViewModel()
            HomeScreen(
                homeViewModel,
                onEvent = homeViewModel::onEvent,
                navController
            )
        }
        composable<AppRoutes.ChatRoute>
         { backStackEntry ->
            val args = backStackEntry.toRoute<AppRoutes.ChatRoute>()
            val viewModel: ChatViewModel = hiltViewModel()


            ChatScreen(
                args,
                viewModel= viewModel,
                navController = navController
            )
        }

    }
}