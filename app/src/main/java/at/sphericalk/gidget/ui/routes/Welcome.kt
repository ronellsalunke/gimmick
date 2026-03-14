package at.sphericalk.gidget.ui.routes

import android.content.Intent
import android.net.Uri
import androidx.activity.ComponentActivity
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import at.sphericalk.gidget.AuthActivity
import at.sphericalk.gidget.BuildConfig
import at.sphericalk.gidget.LocalActivity
import at.sphericalk.gidget.R
import at.sphericalk.gidget.dataStore
import at.sphericalk.gidget.model.ApiResult
import at.sphericalk.gidget.ui.composables.Loading
import at.sphericalk.gidget.utils.Constants

enum class WelcomeScreenState {
    Success,
    Loading,
    Error,
    Initial,
}

@Composable
fun Welcome(viewModel: FeedViewModel, navigateToFeed: () -> Unit) {
    val (screenState, updateScreenState) = remember { mutableStateOf(WelcomeScreenState.Initial) }
    val activity = LocalActivity.current
    val oauthLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            val intent = result.data
            val resultCode = result.resultCode
            if (intent == null) {
                updateScreenState(WelcomeScreenState.Error)
                return@rememberLauncherForActivityResult
            }

            val uri = intent.data
            if (uri == null) {
                updateScreenState(WelcomeScreenState.Error)
                return@rememberLauncherForActivityResult
            }

            if (uri.toString().startsWith(Constants.REDIRECT_URI)) {
                getAccessToken(
                    viewModel,
                    uri,
                    activity as ComponentActivity,
                    updateScreenState,
                    navigateToFeed
                )
            }
        }

    Scaffold(
        topBar = { GimmickTopBar(stringResource(id = R.string.app_name)) },
        content = { innerPadding ->
            Box(modifier = Modifier.padding(innerPadding)) {
                GimmickWelcomeScreen(
                    welcomeScreenState = screenState,
                    activityResultLauncher = oauthLauncher,
                )
            }
        }
    )
}

@Composable
fun GimmickWelcomeScreen(
    welcomeScreenState: WelcomeScreenState,
    activityResultLauncher: ManagedActivityResultLauncher<Intent, ActivityResult>
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(30.dp),
            modifier = Modifier.padding(24.dp)
        ) {
            Image(
                painter = painterResource(id = R.drawable.ic_gimmick_icon),
                contentDescription = stringResource(id = R.string.gh_icon_desc)
            )
            Text(
                text = stringResource(id = R.string.welcome_msg),
                style = MaterialTheme.typography.h6
            )
            // TODO (sphericalkat): add gimmick logo / icon
            if (welcomeScreenState == WelcomeScreenState.Loading) {
                Loading()
            } else {
                GimmickLoginButton(
                    signInText = stringResource(R.string.sign_in),
                    signInIcon = painterResource(R.drawable.github_icon),
                    activityResultLauncher = activityResultLauncher
                )
            }
        }
    }
}

@Composable
fun GimmickLoginButton(
    signInText: String,
    signInIcon: Painter,
    activityResultLauncher: ManagedActivityResultLauncher<Intent, ActivityResult>,
    textColor: Color = Color.Black,
) {
    val context = LocalContext.current

    Button(
        onClick = {
            val intent =
                Intent(context, AuthActivity::class.java).apply { putExtra("REDIRECT_INTENT", true) }
            activityResultLauncher.launch(intent)
        },
        modifier = Modifier.fillMaxWidth(),
    ) {
        Icon(
            painter = signInIcon,
            contentDescription = null
        )
        Spacer(Modifier.size(ButtonDefaults.IconSpacing))
        Text(text = signInText, color = textColor)
    }
}

@Composable
fun GimmickTopBar(title: String, backgroundColor: Color = MaterialTheme.colors.background) {
    TopAppBar(
        elevation = 0.dp,
        modifier = Modifier
            .background(backgroundColor)
            .statusBarsPadding(),
        backgroundColor = backgroundColor,
        title = {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(text = title)
            }
        },
    )
}

private fun getAccessToken(
    viewModel: FeedViewModel,
    uri: Uri,
    activity: ComponentActivity,
    updateScreenState: (WelcomeScreenState) -> Unit,
    navigateToFeed: () -> Unit,
) {
    viewModel.getAccessToken(
        BuildConfig.CLIENT_ID,
        BuildConfig.CLIENT_SECRET,
        uri.getQueryParameter("code").toString(),
        Constants.REDIRECT_URI,
        activity.dataStore
    ).observe(activity) {
        when (it) {
            is ApiResult.Error -> {
                updateScreenState(WelcomeScreenState.Error)
            }
            is ApiResult.Success -> {
                updateScreenState(WelcomeScreenState.Success)
                navigateToFeed()
            }
            ApiResult.Loading -> {
                updateScreenState(WelcomeScreenState.Loading)
            }
        }
    }
}
