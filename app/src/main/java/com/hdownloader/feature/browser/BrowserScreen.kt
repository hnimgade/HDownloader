package com.hdownloader.feature.browser

import android.content.Intent
import android.graphics.Bitmap
import android.webkit.DownloadListener
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.ArrowForward
import androidx.compose.material.icons.rounded.Bookmark
import androidx.compose.material.icons.rounded.BookmarkBorder
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Share
import androidx.compose.material.icons.rounded.Tab
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions

private const val HOME_URL = "file:///android_asset/browser_home.html"

/**
 * Built-in browser backed by a real [WebView]. Media links (video/audio/archives)
 * are intercepted before navigation and handed to the download pipeline, and
 * explicit downloads reported through [DownloadListener] are queued as well.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BrowserScreen(
    viewModel: BrowserViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    val lifecycleOwner = LocalLifecycleOwner.current
    val currentViewModel by rememberUpdatedState(viewModel)

    var urlInput by remember { mutableStateOf(uiState.urlInput) }
    var webView by remember { mutableStateOf<WebView?>(null) }

    val pageClient = remember {
        object : WebViewClient() {
            override fun shouldOverrideUrlLoading(view: WebView, request: WebResourceRequest): Boolean {
                val url = request.url.toString()
                if (BrowserUrlResolver.isMediaUrl(url)) {
                    currentViewModel.onDownloadDetected(url, null, null)
                    return true
                }
                return false
            }

            @Deprecated("Deprecated in Java")
            override fun shouldOverrideUrlLoading(view: WebView, url: String): Boolean {
                if (BrowserUrlResolver.isMediaUrl(url)) {
                    currentViewModel.onDownloadDetected(url, null, null)
                    return true
                }
                return false
            }

            override fun onPageStarted(view: WebView, url: String?, favicon: Bitmap?) {
                currentViewModel.onPageStarted(url.orEmpty())
            }

            override fun onPageFinished(view: WebView, url: String?) {
                currentViewModel.onPageFinished(url.orEmpty(), view.title)
                currentViewModel.onNavigationStateChanged(view.canGoBack(), view.canGoForward())
            }
        }
    }

    val chromeClient = remember {
        object : WebChromeClient() {
            override fun onProgressChanged(view: WebView, newProgress: Int) {
                currentViewModel.onProgressChanged(newProgress)
            }

            override fun onReceivedTitle(view: WebView, title: String?) {
                currentViewModel.onTitleChanged(title.orEmpty())
            }
        }
    }

    val downloadListener = remember {
        DownloadListener { url, _, contentDisposition, mimeType, _ ->
            currentViewModel.onDownloadDetected(url, contentDisposition, mimeType)
        }
    }

    val message = uiState.message
    LaunchedEffect(message) {
        message?.let {
            snackbarHostState.showSnackbar(it)
            currentViewModel.onMessageShown()
        }
    }

    LaunchedEffect(uiState.currentUrl) {
        if (uiState.currentUrl.isNotEmpty()) {
            urlInput = uiState.currentUrl
        }
    }

    fun loadUrl(url: String) {
        val resolved = BrowserUrlResolver.resolve(url)
        if (resolved.isEmpty()) return
        urlInput = resolved
        currentViewModel.navigate(resolved)
        webView?.loadUrl(resolved)
    }

    BackHandler(enabled = webView?.canGoBack() == true) {
        webView?.goBack()
    }

    DisposableEffect(lifecycleOwner, webView) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_RESUME -> webView?.onResume()
                Lifecycle.Event.ON_PAUSE -> webView?.onPause()
                else -> Unit
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            webView?.destroy()
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            BrowserTopBar(
                urlInput = urlInput,
                onUrlChange = { urlInput = it },
                onNavigate = { loadUrl(urlInput) },
                canGoBack = uiState.canGoBack,
                canGoForward = uiState.canGoForward,
                isLoading = uiState.isLoading,
                progress = uiState.progress,
                isBookmarked = uiState.isBookmarked,
                onBack = { webView?.goBack() },
                onForward = { webView?.goForward() },
                onReload = { webView?.reload() },
                onHome = { loadUrl(HOME_URL) },
                onToggleBookmark = viewModel::toggleBookmark,
                onShare = {
                    val shareUrl = uiState.currentUrl.ifBlank { null }
                    if (shareUrl != null) {
                        val shareIntent = Intent(Intent.ACTION_SEND).apply {
                            type = "text/plain"
                            putExtra(Intent.EXTRA_TEXT, shareUrl)
                        }
                        runCatching {
                            context.startActivity(Intent.createChooser(shareIntent, null))
                        }
                    }
                },
            )
        },
        bottomBar = {
            BrowserBottomBar(
                downloadsQueued = viewModel.downloadsQueued.collectAsStateWithLifecycle().value,
            )
        },
    ) { innerPadding ->
        if (webView == null) {
            LoadingPlaceholder(modifier = Modifier.padding(innerPadding))
        }
        AndroidView(
            factory = { ctx ->
                WebView(ctx).apply {
                    settings.javaScriptEnabled = true
                    settings.domStorageEnabled = true
                    settings.mediaPlaybackRequiresUserGesture = false
                    settings.mixedContentMode = WebSettings.MIXED_CONTENT_COMPATIBILITY_MODE
                    settings.setSupportZoom(true)
                    settings.builtInZoomControls = true
                    settings.displayZoomControls = false
                    webViewClient = pageClient
                    webChromeClient = chromeClient
                    setDownloadListener(downloadListener)
                    loadUrl(HOME_URL)
                }.also { webView = it }
            },
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun BrowserTopBar(
    urlInput: String,
    onUrlChange: (String) -> Unit,
    onNavigate: () -> Unit,
    canGoBack: Boolean,
    canGoForward: Boolean,
    isLoading: Boolean,
    progress: Int,
    isBookmarked: Boolean,
    onBack: () -> Unit,
    onForward: () -> Unit,
    onReload: () -> Unit,
    onHome: () -> Unit,
    onToggleBookmark: () -> Unit,
    onShare: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            BrowserToolbarButton(
                icon = Icons.AutoMirrored.Rounded.ArrowBack,
                description = "Back",
                enabled = canGoBack,
                onClick = onBack,
            )
            BrowserToolbarButton(
                icon = Icons.Rounded.ArrowForward,
                description = "Forward",
                enabled = canGoForward,
                onClick = onForward,
            )
            BrowserToolbarButton(
                icon = Icons.Rounded.Refresh,
                description = "Refresh",
                onClick = onReload,
            )
            BrowserToolbarButton(
                icon = Icons.Rounded.Home,
                description = "Home",
                onClick = onHome,
            )

            OutlinedTextField(
                value = urlInput,
                onValueChange = onUrlChange,
                placeholder = { Text("Search or enter URL") },
                singleLine = true,
                shape = RoundedCornerShape(24.dp),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Go),
                keyboardActions = KeyboardActions(onGo = { onNavigate() }),
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 4.dp),
            )

            IconButton(onClick = onNavigate) {
                Icon(
                    imageVector = Icons.Rounded.Search,
                    contentDescription = "Go",
                    tint = MaterialTheme.colorScheme.primary,
                )
            }
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            BrowserToolbarButton(
                icon = Icons.Rounded.Tab,
                description = "Tabs",
                onClick = { },
            )
            BrowserToolbarButton(
                icon = if (isBookmarked) Icons.Rounded.Bookmark else Icons.Rounded.BookmarkBorder,
                description = "Bookmark",
                onClick = onToggleBookmark,
            )
            BrowserToolbarButton(
                icon = Icons.Rounded.Share,
                description = "Share",
                onClick = onShare,
            )
            Spacer(modifier = Modifier.weight(1f))
        }
        if (isLoading) {
            LinearProgressIndicator(
                progress = { progress / 100f },
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

@Composable
private fun BrowserToolbarButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    description: String,
    enabled: Boolean = true,
    onClick: () -> Unit,
) {
    IconButton(onClick = onClick, enabled = enabled) {
        Icon(
            imageVector = icon,
            contentDescription = description,
            tint = if (enabled) {
                MaterialTheme.colorScheme.onSurfaceVariant
            } else {
                MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.38f)
            },
        )
    }
}

@Composable
private fun BrowserBottomBar(downloadsQueued: Int) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface)
            .padding(vertical = 6.dp, horizontal = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = "H Downloader Browser",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        if (downloadsQueued > 0) {
            Text(
                text = "$downloadsQueued queued",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary,
            )
        }
    }
}

@Composable
private fun LoadingPlaceholder(modifier: Modifier = Modifier) {
    Box(modifier = modifier.fillMaxSize()) {
        Text(
            text = "Loading browser...",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.align(Alignment.Center),
        )
    }
}
