package at.sphericalk.gidget.ui.routes

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material.AlertDialog
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import androidx.navigation.NavController
import at.sphericalk.gidget.R
import at.sphericalk.gidget.ui.composables.MarkdownText
import at.sphericalk.gidget.utils.timeAgo
import at.sphericalk.gidget.utils.toColor
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest

@Composable
fun Release(navController: NavController, viewModel: FeedViewModel) {
    Scaffold(
        topBar = {
            TopAppBar(
                elevation = 0.dp,
                modifier = Modifier
                    .background(MaterialTheme.colors.background)
                    .windowInsetsPadding(WindowInsets.statusBars),
                backgroundColor = MaterialTheme.colors.background,
                title = {
                    Box(
                        contentAlignment = Alignment.TopStart,
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Text(text = "Release")
                    }
                },
                navigationIcon = {
                    IconButton(onClick = {
                        viewModel.selectedEvent.value = null
                        navController.popBackStack()
                    }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = MaterialTheme.colors.onSurface
                        )
                    }
                }
            )
        },
    ) {
        val event = viewModel.selectedEvent.value
        if (event?.payload != null) {
            val openDialog = remember { mutableStateOf(false) }
            Column(
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                Text(text = event.repo.name, style = MaterialTheme.typography.h6)

                Text(
                    text = event.payload.release?.name!!,
                    style = MaterialTheme.typography.h4
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.ic_tag),
                        contentDescription = "Tag",
                        colorFilter = ColorFilter.tint("#8b949e".toColor())
                    )
                    Text(
                        text = event.payload.release.tag_name,
                        modifier = Modifier.padding(start = 8.dp)
                    )

                    Image(
                        painter = painterResource(id = R.drawable.ic_branch),
                        contentDescription = "Branch",
                        colorFilter = ColorFilter.tint("#8b949e".toColor()),
                        modifier = Modifier.padding(start = 24.dp)
                    )
                    Text(
                        text = event.payload.release.target_commitish,
                        modifier = Modifier.padding(start = 8.dp)
                    )
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.Start,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Image(
                        painter = rememberAsyncImagePainter(
                            ImageRequest.Builder(androidx.compose.ui.platform.LocalContext.current)
                                .data(data = event.actor.avatar_url)
                                .crossfade(true)
                                .apply(block = fun ImageRequest.Builder.() {
                                    transformations(coil.transform.CircleCropTransformation())
                                })
                                .build()
                        ),
                        contentDescription = event.actor.login,
                        modifier = Modifier.size(32.dp, 32.dp),
                    )
                    Text(
                        text = "${event.actor.login} released this ${event.created_at.timeAgo()}",
                        modifier = Modifier.padding(start = 16.dp),
                    )
                }

//                Button(onClick = { openDialog.value = true }, modifier = Modifier.fillMaxWidth()) {
//                    Text(text = "View changelog")
//                }

                MarkdownText(
                    markdown = "${event.payload.release.body}\n\n",
                    fontResource = R.font.manrope_regular,
                )
                if (openDialog.value) {
                    AlertDialog(
                        modifier = Modifier.padding(24.dp),
                        backgroundColor = MaterialTheme.colors.surface,
                        onDismissRequest = { openDialog.value = false },
                        buttons = {
                            Row(
                                Modifier
                                    .fillMaxWidth()
                                    .padding(top = 16.dp), horizontalArrangement = Arrangement.End
                            ) {
                                Button(
                                    modifier = Modifier.padding(16.dp),
                                    onClick = { openDialog.value = false },
                                    colors = ButtonDefaults.outlinedButtonColors(backgroundColor = Color.Transparent),
                                    elevation = ButtonDefaults.elevation(
                                        defaultElevation = 0.dp,
                                        pressedElevation = 0.dp
                                    )
                                ) {
                                    Text(text = "OK")
                                }
                            }
                        },
                        text = {
                            MarkdownText(
                                markdown = event.payload.release.body,
                                fontResource = R.font.manrope_regular
                            )
                        },
                        properties = DialogProperties(
                            dismissOnClickOutside = true,
                            dismissOnBackPress = true
                        )
                    )
                }
            }

        }
    }
}