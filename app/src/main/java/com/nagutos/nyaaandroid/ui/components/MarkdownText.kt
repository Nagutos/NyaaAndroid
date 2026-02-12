package com.nagutos.nyaaandroid.ui.components

import android.text.Layout
import android.text.Spannable
import android.text.Spanned
import android.view.View
import android.widget.TextView
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import io.noties.markwon.AbstractMarkwonPlugin
import io.noties.markwon.Markwon
import io.noties.markwon.core.MarkwonTheme
import io.noties.markwon.ext.strikethrough.StrikethroughPlugin
import io.noties.markwon.ext.tables.TablePlugin
import io.noties.markwon.ext.tables.TableTheme
import io.noties.markwon.html.HtmlPlugin
import android.graphics.text.LineBreaker
import android.text.TextPaint
import io.noties.markwon.image.coil.CoilImagesPlugin
import io.noties.markwon.linkify.LinkifyPlugin
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import io.noties.markwon.MarkwonConfiguration
import io.noties.markwon.image.ImageSizeResolverDef
import io.noties.markwon.image.AsyncDrawableSpan

// Enable the display of images in tables in descriptions

@Composable
fun MarkdownText(
    markdown: String,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val colorScheme = MaterialTheme.colorScheme

    var selectedImageUrl by remember { mutableStateOf<String?>(null) }

    val onImageClick = rememberUpdatedState { url: String ->
        selectedImageUrl = url
    }

    val markwon = remember(context) {
        val tableTheme = TableTheme.Builder()
            .tableBorderColor(colorScheme.outline.toArgb())
            .tableBorderWidth(1)
            .tableCellPadding(4)
            .tableHeaderRowBackgroundColor(colorScheme.surfaceVariant.toArgb())
            .tableEvenRowBackgroundColor(colorScheme.surface.toArgb())
            .tableOddRowBackgroundColor(colorScheme.surfaceVariant.toArgb())
            .build()

        Markwon.builder(context)
            .usePlugin(HtmlPlugin.create())
            .usePlugin(StrikethroughPlugin.create())
            .usePlugin(TablePlugin.create(tableTheme))
            .usePlugin(LinkifyPlugin.create())
            .usePlugin(CoilImagesPlugin.create(context))
            .usePlugin(object : AbstractMarkwonPlugin() {
                override fun configureTheme(builder: MarkwonTheme.Builder) {
                    builder
                        .headingBreakHeight(0)
                        .headingTextSizeMultipliers(floatArrayOf(1.4f, 1.3f, 1.2f, 1.1f, 1f, 1f))
                        .build()
                }

                override fun configureConfiguration(builder: MarkwonConfiguration.Builder) {
                    builder.imageSizeResolver(ImageSizeResolverDef())
                }

                override fun afterSetText(textView: TextView) {
                    val spannable = textView.text as? Spannable ?: return

                    val spans = spannable.getSpans(0, spannable.length, AsyncDrawableSpan::class.java)

                    for (span in spans) {
                        val start = spannable.getSpanStart(span)
                        val end = spannable.getSpanEnd(span)

                        val existing = spannable.getSpans(start, end, ClickableSpan::class.java)
                        if (existing.isEmpty()) {
                            spannable.setSpan(object : ClickableSpan() {
                                override fun onClick(widget: View) {
                                    onImageClick.value(span.drawable.destination)
                                }

                                override fun updateDrawState(ds: TextPaint) {
                                    ds.isUnderlineText = false
                                }
                            }, start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
                        }
                    }
                }
            })
            .build()
    }

    Box(modifier = modifier) {
        AndroidView(
            modifier = Modifier.fillMaxWidth(),
            factory = { ctx ->
                TextView(ctx).apply {
                    setTextColor(colorScheme.onSurface.toArgb())
                    setLinkTextColor(colorScheme.primary.toArgb())
                    textSize = 14f
                    movementMethod = LinkMovementMethod.getInstance()
                    hyphenationFrequency = Layout.HYPHENATION_FREQUENCY_NONE
                    breakStrategy = LineBreaker.BREAK_STRATEGY_SIMPLE
                }
            },
            update = { textView ->
                val fixedMarkdown = sanitizeNyaaMarkdown(markdown)
                markwon.setMarkdown(textView, fixedMarkdown)
            }
        )
        selectedImageUrl?.let { url ->
            FullScreenImageDialog(url = url, onDismiss = { selectedImageUrl = null })
        }
    }
}

@Composable
fun FullScreenImageDialog(url: String, onDismiss: () -> Unit) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.9f))
                .clickable { onDismiss() },
            contentAlignment = Alignment.Center
        ) {
            AsyncImage(
                model = url,
                contentDescription = "Agrandissement",
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                contentScale = ContentScale.Fit
            )

            Surface(
                onClick = onDismiss,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 48.dp),
                shape = RoundedCornerShape(24.dp),
                color = Color.DarkGray.copy(alpha = 0.8f)
            ) {
                Text(
                    text = "Fermer",
                    color = Color.White,
                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp)
                )
            }
        }
    }
}


/**
 * Cleans and repairs Nyaa's Markdown structure (entity and pipe management)
 */
fun sanitizeNyaaMarkdown(input: String): String {
    if (input.isBlank()) return ""

    val textWithoutImageLinks = input.replace(Regex("\\[\\!\\[(.*?)\\]\\((.*?)\\)\\]\\((.*?)\\)"), "![$1]($2)")


    val lines = textWithoutImageLinks.split("\n")
    val finalOutput = mutableListOf<String>()
    var i = 0

    while (i < lines.size) {
        val line = lines[i].trim()

        if (line.startsWith("|")) {
            val tableRows = mutableListOf<String>()
            var hasImage = false
            var j = i

            while (j < lines.size && lines[j].trim().startsWith("|")) {
                val row = lines[j].trim()
                tableRows.add(row)
                if (row.contains("![")) hasImage = true
                j++
            }

            if (hasImage) {
                // IF IMAGE: Transform vertically (break the table)
                finalOutput.add("")
                tableRows.forEach { row ->
                    if (!row.contains("---")) {
                        row.split("|")
                            .map { it.trim() }
                            .filter { it.isNotEmpty() }
                            .forEach { cell ->
                                finalOutput.add(cell)
                                finalOutput.add("")
                            }
                    }
                }
            } else {
                // IF TEXT: Do NOTHING.
                tableRows.forEach { finalOutput.add(it) }
            }
            i = j
        } else {
            finalOutput.add(lines[i])
            i++
        }
    }
    return finalOutput.joinToString("\n")
}