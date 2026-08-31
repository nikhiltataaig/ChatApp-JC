package com.example.chatapp.ui.chat

import android.Manifest
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Image
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil3.compose.AsyncImage
import com.example.chatapp.AppRoutes
import com.example.chatapp.CommonUiEvent
import com.example.chatapp.ui.components.MessageInput
import com.example.chatapp.ui.components.MessageItem
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    args: AppRoutes.ChatRoute,
   viewModel: ChatViewModel,
    navController: NavController
) {

    val showLoader  = remember { mutableStateOf(false) }
    val uiState by viewModel.uiState
        .collectAsStateWithLifecycle()


    val lifecycleOwner = LocalLifecycleOwner.current
    val context = LocalContext.current
    val  chatId = args.chatId.orEmpty()
    val userId = args.userId.orEmpty()
    val userName = args.userName.orEmpty()
    val profileImageUrl = args.profileImageUrl.orEmpty()

    LaunchedEffect(chatId) {
        viewModel.initialize(
            chatId,
            receiverId = userId
        )
    }

    LaunchedEffect(Unit) {

        viewModel.uiEvent.flowWithLifecycle(lifecycleOwner.lifecycle, Lifecycle.State.STARTED ).collect { event ->

            showLoader.value = false;
            when (event) {

                is CommonUiEvent.PopBackStack -> {
                    navController.popBackStack()
                }
                is CommonUiEvent.ShowLoader -> {
                    showLoader.value = true
                }
                is CommonUiEvent.Navigate -> {
                    navController.navigate(event.route)
                }
                is CommonUiEvent.DoNothing->{

                    showLoader.value = false

                }
                is CommonUiEvent.ShowToast -> {
                    Toast.makeText(context , event.msg , Toast.LENGTH_SHORT ).show()
                }
                else-> Unit
            }
        }
    }

    val listState = rememberLazyListState()

    /*
     * Automatically scroll to the latest message
     */
    LaunchedEffect(uiState.messages.size) {

        if (uiState.messages.isNotEmpty()) {

            listState.animateScrollToItem(
                uiState.messages.lastIndex
            )
        }
    }


    /*
     * URI used by the camera.
     */
    var cameraUri by remember {
        mutableStateOf<Uri?>(null)
    }

    /*
     * Create a temporary URI where
     * the camera will save the image.
     */
    fun createCameraUri(): Uri {

        val directory = File(
            context.cacheDir,
            "camera"
        )

        directory.mkdirs()

        val file = File.createTempFile(
            "chat_image_",
            ".jpg",
            directory
        )

        return FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )
    }

    /*
     * Gallery image picker.
     */
    val imagePicker =
        rememberLauncherForActivityResult(
            contract = ActivityResultContracts.GetContent()
        ) { uri ->

            uri?.let {

                viewModel.onEvent(
                    ChatUiEvent.ImageSelected(it)
                )
            }
        }

    /*
     * Camera launcher.
     */
    val cameraLauncher =
        rememberLauncherForActivityResult(
            contract = ActivityResultContracts.TakePicture()
        ) { success ->

            if (success) {

                cameraUri?.let { uri ->

                    viewModel.onEvent(
                        ChatUiEvent.ImageSelected(uri)
                    )
                }
            }
        }

    /*
     * Camera permission launcher.
     */
    val cameraPermissionLauncher =
        rememberLauncherForActivityResult(
            contract =
                ActivityResultContracts.RequestPermission()
        ) { granted ->

            if (granted) {

                val uri = createCameraUri()

                cameraUri = uri

                cameraLauncher.launch(uri)
            }
        }

    Scaffold(

        topBar = {

            TopAppBar(

                navigationIcon = {

                    IconButton(
                        onClick = {

                            viewModel.onEvent(
                                ChatUiEvent.BackClicked
                            )
                        }
                    ) {

                        Icon(
                            imageVector =
                                Icons.Default.ArrowBack,
                            contentDescription =
                                "Back"
                        )
                    }
                },

                title = {

                    Row(
                        verticalAlignment =
                            Alignment.CenterVertically
                    ) {

                        AsyncImage(
                            model =
                                profileImageUrl,

                            contentDescription =
                                "Profile picture",

                            modifier =
                                Modifier
                                    .size(40.dp)
                                    .clip(
                                        CircleShape
                                    ),

                            contentScale =
                                ContentScale.Crop
                        )

                        Spacer(
                            modifier =
                                Modifier.width(12.dp)
                        )

                        Text(
                            text = userName
                        )
                    }
                }
            )
        }

    ) { paddingValues ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {

            /*
             * Messages
             */
            LazyColumn(

                modifier =
                    Modifier.weight(1f),

                state =
                    listState,

                contentPadding =
                    PaddingValues(12.dp),

                verticalArrangement =
                    Arrangement.spacedBy(8.dp)
            ) {

                items(
                    items = uiState.messages,

                    key = {
                        it.messageId
                    }
                ) { message ->

                    MessageItem(
                        message = message,

                        onClick = {

                            viewModel.onEvent(
                                ChatUiEvent.MessageClicked(
                                    message
                                )
                            )
                        }
                    )
                }
            }

            /*
             * Show upload progress while
             * image is being uploaded.
             */
            if (uiState.isSending) {

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(
                            horizontal = 16.dp,
                            vertical = 4.dp
                        ),

                    verticalAlignment =
                        Alignment.CenterVertically
                ) {

                    CircularProgressIndicator(
                        modifier =
                            Modifier.size(18.dp)
                    )

                    Spacer(
                        modifier =
                            Modifier.width(8.dp)
                    )

                    Text(
                        text = "Sending image..."
                    )
                }
            }

            /*
             * Message input + image/camera buttons.
             */
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {

                /*
                 * Gallery
                 */
                IconButton(
                    onClick = {
                        imagePicker.launch("image/*")
                    },
                    enabled = !uiState.isSending
                ) {

                    Icon(
                        imageVector = Icons.Default.Image,
                        contentDescription = "Select image"
                    )
                }

                /*
                 * Camera
                 */
                IconButton(
                    onClick = {

                        cameraPermissionLauncher.launch(
                            Manifest.permission.CAMERA
                        )
                    },
                    enabled = !uiState.isSending
                ) {

                    Icon(
                        imageVector = Icons.Default.CameraAlt,
                        contentDescription = "Take photo"
                    )
                }

                /*
                 * Text input
                 */
                MessageInput(
                    modifier = Modifier.weight(1f),

                    text = uiState.messageText,

                    onTextChanged = {
                        viewModel.onEvent(
                            ChatUiEvent.MessageTextChanged(it)
                        )
                    },

                    onSend = {
                        viewModel.onEvent(
                            ChatUiEvent.SendMessageClicked
                        )
                    }
                )
            }
        }
    }
}