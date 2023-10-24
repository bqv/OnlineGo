package io.zenandroid.onlinego.utils

import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.content.ContextWrapper
import android.util.Log
import android.view.KeyEvent
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.ui.platform.ComposeView
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.setViewTreeLifecycleOwner
import androidx.savedstate.setViewTreeSavedStateRegistryOwner
import kotlin.coroutines.EmptyCoroutineContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.cancellable
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.scan
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.material.TextField
import androidx.compose.material.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import io.zenandroid.onlinego.ui.theme.OnlineGoTheme

class LogcatPopup(
    context: AppCompatActivity,
) : Dialog(context) {
    init {
        setTitle("Logcat")
        setCancelable(true)
        val view = logcatView(this, context).apply {
            setViewTreeLifecycleOwner(context)
            setViewTreeSavedStateRegistryOwner(context)
        }
        setContentView(view)
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent): Boolean {
        when (keyCode) {
            KeyEvent.KEYCODE_VOLUME_UP -> dismiss()
            KeyEvent.KEYCODE_VOLUME_DOWN ->
                Runtime.getRuntime().exec("logcat -c").waitFor()
            else -> return super.onKeyDown(keyCode, event)
        }
        return true
    }
}

private fun logcatView(
    dialog: Dialog,
    lifecycleOwner: LifecycleOwner
): View = ComposeView(dialog.context).apply {
    val logcatState = logcatFlow(lifecycleOwner.lifecycleScope)

    setContent {
        val state by rememberStateWithLifecycle(logcatState)

        OnlineGoTheme {
            logcatScrollable(state)
        }
    }
}

@Composable
fun logcatView(scope: CoroutineScope? = null) {
    val scope = scope ?: LocalContext.current.lifecycleOwner!!.lifecycleScope
    val logcatFlow = logcatFlow(scope)
    val state by rememberStateWithLifecycle(logcatFlow)

    logcatScrollable(state)
}

private val Context.lifecycleOwner: LifecycleOwner?
    get() {
        var context: Context? = this

        while (context != null && context !is LifecycleOwner) {
            val baseContext = (context as? ContextWrapper?)?.baseContext
            context = if (baseContext == context) null else baseContext
        }

        return if (context is LifecycleOwner) context else null
    }

@Composable
private fun logcatScrollable(logcat: List<String>) {
    Column(
        modifier = Modifier
            .fillMaxHeight(0.95f)
    ) {
        val logState = rememberLazyListState()
        var pendingScroll by remember { mutableStateOf<Int?>(null) }
        LaunchedEffect(pendingScroll) {
            pendingScroll?.let {
                logState.scrollToItem(it)
                pendingScroll = null
            }
        }

        var search by remember { mutableStateOf(TextFieldValue()) }
        var follow by remember { mutableStateOf(true) }
        if (follow) {
            LaunchedEffect(logcat.size) {
                logState.scrollToItem(logcat.size)
            }
        }
        Surface(
            color = MaterialTheme.colors.surface,
            modifier = Modifier
                .fillMaxWidth()
        ) {
            Column {
                Row {
                    TextButton(onClick = {
                        logcat.withIndex().filter {
                            search.text.isNotEmpty() && it.value.contains(search.text)
                        }.map { it.index }.lastOrNull {
                            it < logState.firstVisibleItemIndex
                        }.let {
                            pendingScroll = it ?: 0
                            follow = false
                        }
                    }) {
                        Text(
                            text = "<",
                            color = MaterialTheme.colors.primary,
                            textAlign = TextAlign.Center,
                            modifier = Modifier
                        )
                    }
                    TextField(
                        value = search,
                        onValueChange = { search = it },
                        maxLines = 1,
                        placeholder = { Text("Search") },
                        colors = TextFieldDefaults.textFieldColors(
                            textColor = MaterialTheme.colors.primary
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .scale(scaleY = 0.8f, scaleX = 1f),
                    )
                    TextButton(onClick = {
                        logcat.withIndex().filter {
                            search.text.isNotEmpty() && it.value.contains(search.text)
                        }.map { it.index }.firstOrNull {
                            it > logState.firstVisibleItemIndex
                        }.let {
                            pendingScroll = it ?: run {
                                follow = true
                                logcat.size
                            }
                        }
                    }) {
                        Text(
                            text = ">",
                            color = MaterialTheme.colors.primary,
                            textAlign = TextAlign.Center,
                            modifier = Modifier
                        )
                    }
                }
                Box(
                    modifier = Modifier
                        .weight(1f)
                ) {
                    SelectionContainer {
                        LazyColumn(
                            state = logState,
                        ) {
                            items(logcat) { line ->
                                val searchHit = search.text.isNotEmpty() && line.contains(search.text)
                                Text(
                                    text = line,
                                    style = MaterialTheme.typography.h5,
                                    color = if (searchHit) MaterialTheme.colors.secondary
                                            else MaterialTheme.colors.onSurface,
                                    modifier = Modifier
                                )
                            }
                        }
                    }
                }
                TextButton(onClick = { follow = !follow }) {
                    Text(
                        text = if (follow) "Unfollow" else "Follow",
                        style = MaterialTheme.typography.h6,
                        color = MaterialTheme.colors.primary,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                    )
                }
            }
        }
    }
}

private fun logcatFlow(scope: CoroutineScope, clear: Boolean = false): StateFlow<List<String>> = logcatLineFlow(clear)
    .scan(emptyList<String>()) { log, line -> log + line }
    .flowOn(Dispatchers.IO)
    .stateIn(scope, SharingStarted.WhileSubscribed(), emptyList())

private fun logcatLineFlow(clear: Boolean): Flow<String> = flow {
    if (clear) {
        Runtime.getRuntime().exec("logcat -c").waitFor()
    }
    Runtime.getRuntime().exec("logcat")
        .inputStream
        .bufferedReader()
        .useLines { lines ->
            lines.forEach { emit(it) }
        }
}
    .cancellable()
    .onStart { Log.d("Logcat", "Logcat started") }
    .onCompletion { Log.d("Logcat", "Logcat finished") }
    .filter { !it.contains("Quality : Skipped:") } // noise
