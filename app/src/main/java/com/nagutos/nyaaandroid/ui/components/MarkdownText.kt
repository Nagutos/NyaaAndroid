package com.nagutos.nyaaandroid.ui.components

import android.text.Layout
import android.text.Spannable
import android.text.Spanned
import android.view.View
import android.widget.TextView
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.nagutos.nyaaandroid.R
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import io.noties.markwon.AbstractMarkwonPlugin
import io.noties.markwon.Markwon
import io.noties.markwon.SoftBreakAddsNewLinePlugin
import io.noties.markwon.core.MarkwonTheme
import io.noties.markwon.ext.strikethrough.StrikethroughPlugin
import io.noties.markwon.ext.tables.TablePlugin
import io.noties.markwon.ext.tables.TableTheme
import io.noties.markwon.html.HtmlPlugin
import android.graphics.text.LineBreaker
import android.text.TextPaint
import io.noties.markwon.image.coil.CoilImagesPlugin
import io.noties.markwon.image.AsyncDrawable
import io.noties.markwon.linkify.LinkifyPlugin
import coil.imageLoader
import coil.request.Disposable
import coil.request.ImageRequest
import androidx.core.text.method.LinkMovementMethodCompat
import android.text.style.ClickableSpan
import io.noties.markwon.MarkwonConfiguration
import io.noties.markwon.LinkResolverDef
import io.noties.markwon.image.ImageSizeResolverDef
import io.noties.markwon.image.AsyncDrawableSpan
import me.saket.telephoto.zoomable.coil.ZoomableAsyncImage
import me.saket.telephoto.zoomable.rememberZoomableImageState
import me.saket.telephoto.zoomable.rememberZoomableState

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

    // Keyed on colorScheme so switching theme (e.g. Light <-> AMOLED) rebuilds Markwon with
    // the new table/heading colors instead of keeping the ones captured at first composition.
    val markwon = remember(context, colorScheme) {
        val tableTheme = TableTheme.Builder()
            .tableBorderColor(colorScheme.outline.toArgb())
            .tableBorderWidth(1)
            .tableCellPadding(4)
            .tableHeaderRowBackgroundColor(colorScheme.surfaceVariant.toArgb())
            .tableEvenRowBackgroundColor(colorScheme.surface.toArgb())
            .tableOddRowBackgroundColor(colorScheme.surfaceVariant.toArgb())
            .build()

        Markwon.builder(context)
            // Nyaa renders its markdown with markdown-it using `breaks:true`, so a single
            // newline becomes a <br>. Markwon (commonmark) would collapse it to a space and
            // flatten the layout. This plugin restores the soft-break-as-newline behavior.
            .usePlugin(SoftBreakAddsNewLinePlugin.create())
            .usePlugin(HtmlPlugin.create())
            .usePlugin(StrikethroughPlugin.create())
            .usePlugin(TablePlugin.create(tableTheme))
            .usePlugin(LinkifyPlugin.create())
            // Give each markdown image a sized placeholder so Markwon reserves space up
            // front (less layout jump on load) and the image fades in instead of popping.
            .usePlugin(CoilImagesPlugin.create(object : CoilImagesPlugin.CoilStore {
                override fun load(drawable: AsyncDrawable): ImageRequest {
                    return ImageRequest.Builder(context)
                        .data(drawable.destination)
                        .crossfade(true)
                        .placeholder(R.drawable.markdown_image_placeholder)
                        .error(R.drawable.markdown_image_placeholder)
                        .build()
                }

                override fun cancel(disposable: Disposable) {
                    disposable.dispose()
                }
            }, context.imageLoader))
            .usePlugin(object : AbstractMarkwonPlugin() {
                override fun configureTheme(builder: MarkwonTheme.Builder) {
                    builder
                        .headingBreakHeight(0)
                        .headingTextSizeMultipliers(floatArrayOf(1.4f, 1.3f, 1.2f, 1.1f, 1f, 1f))
                        .build()
                }

                override fun configureConfiguration(builder: MarkwonConfiguration.Builder) {
                    builder.imageSizeResolver(ImageSizeResolverDef())
                    // Descriptions and comments are untrusted. Only follow web and magnet
                    // links when tapped; silently ignore dangerous schemes (intent:, file:,
                    // content:, javascript:, ...) that could trigger unexpected actions.
                    val default = LinkResolverDef()
                    builder.linkResolver { view, link ->
                        when (android.net.Uri.parse(link).scheme?.lowercase()) {
                            "http", "https", "magnet" -> default.resolve(view, link)
                            else -> Unit
                        }
                    }
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
                    // Selectable text (long-press copy) + reliable link clicks inside the
                    // scrolling LazyColumn. LinkMovementMethodCompat fixes the touch-interception
                    // bug the plain LinkMovementMethod has in Compose scrollable containers.
                    setTextIsSelectable(true)
                    movementMethod = LinkMovementMethodCompat.getInstance()
                    hyphenationFrequency = Layout.HYPHENATION_FREQUENCY_NONE
                    breakStrategy = LineBreaker.BREAK_STRATEGY_SIMPLE
                }
            },
            update = { textView ->
                // Re-apply colors here too so a theme change recolors the base text/links,
                // not only the Markwon spans (factory runs once, update runs on recomposition).
                textView.setTextColor(colorScheme.onSurface.toArgb())
                textView.setLinkTextColor(colorScheme.primary.toArgb())
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
        val context = LocalContext.current
        // Telephoto handles pinch-zoom, double-tap-to-zoom, pan, fling and sub-sampling of
        // large images out of the box — no need to reimplement gesture math ourselves.
        val zoomableState = rememberZoomableState()

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.95f)),
            contentAlignment = Alignment.Center
        ) {
            ZoomableAsyncImage(
                model = ImageRequest.Builder(context)
                    .data(url)
                    .crossfade(true)
                    .build(),
                contentDescription = stringResource(R.string.cd_zoom),
                state = rememberZoomableImageState(zoomableState),
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                // A single tap dismisses only when the image is at its resting (un-zoomed) scale.
                onClick = {
                    val zoom = zoomableState.zoomFraction
                    if (zoom == null || zoom == 0f) onDismiss()
                }
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
                    text = stringResource(R.string.action_close),
                    color = Color.White,
                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp)
                )
            }
        }
    }
}


/**
 * Rewrite an image URL to go through Jetpack Photon (`i0.wp.com`), the same proxy Nyaa's web
 * UI uses. The device then only ever connects to `wp.com` over HTTPS instead of leaking its
 * IP to (and trusting the transport of) whatever third-party host a description points at.
 *
 * Example: `https://host.example/a.jpg` -> `https://i0.wp.com/host.example/a.jpg?ssl=1`.
 * `ssl=1` tells Photon the origin is HTTPS. Already-proxied, relative and `data:` URLs, and
 * unknown schemes are returned unchanged.
 */
fun proxifyImageUrl(rawUrl: String): String {
    val url = rawUrl.trim()
    if (url.isEmpty() || url.startsWith("data:", ignoreCase = true)) return url

    val (originIsHttps, hostAndPath) = when {
        url.startsWith("https://", ignoreCase = true) -> true to url.substring("https://".length)
        url.startsWith("http://", ignoreCase = true) -> false to url.substring("http://".length)
        url.startsWith("//") -> true to url.substring(2)
        else -> return url // relative or unknown scheme: leave untouched
    }

    // Don't double-wrap something already served by the wp.com proxy.
    if (Regex("^i[0-3]\\.wp\\.com/").containsMatchIn(hostAndPath)) return url

    val sslSuffix = if (originIsHttps) (if (hostAndPath.contains("?")) "&ssl=1" else "?ssl=1") else ""
    return "https://i0.wp.com/$hostAndPath$sslSuffix"
}

/**
 * Cleans up Nyaa's markdown before handing it to Markwon.
 *
 * Verified against real nyaa.si descriptions: table rows are already separated by real
 * newlines (stored as &#10; and decoded by Jsoup's wholeText()), NOT glued together. Cells
 * are frequently empty (e.g. "| Anime Time | |"), so we must NOT try to "split" rows on a
 * "| |" pattern — that corrupts empty cells and breaks the whole table. We only supply the
 * two things Markwon's GFM TablePlugin needs but Nyaa's raw text may lack:
 *   1. a blank line BEFORE a table block (isolation) — handled in the loop below;
 *   2. images inside table cells rendered vertically so they fit on mobile.
 * (breaks:true, i.e. soft-break-as-newline, is handled by SoftBreakAddsNewLinePlugin above.)
 */
fun sanitizeNyaaMarkdown(input: String): String {
    if (input.isBlank()) return ""

    // Flatten [![alt](img)](link) -> ![alt](img) so the image renders instead of a bare link
    val textWithoutImageLinks = input.replace(Regex("\\[\\!\\[(.*?)\\]\\((.*?)\\)\\]\\((.*?)\\)"), "![$1]($2)")

    // Route every description image through the same image proxy Nyaa's web UI uses, so the
    // device never connects directly to an arbitrary third-party host (which would leak the
    // user's IP and could load over plain HTTP). Only image URLs are rewritten, never links.
    val textWithProxiedImages = textWithoutImageLinks.replace(Regex("!\\[(.*?)\\]\\((.*?)\\)")) { match ->
        "![${match.groupValues[1]}](${proxifyImageUrl(match.groupValues[2])})"
    }

    val lines = textWithProxiedImages.split("\n")
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
                // TEXT TABLE: GFM requires a blank line BEFORE the block, then contiguous
                // rows. Isolate the block without inserting blank lines between the rows.
                if (finalOutput.isNotEmpty() && finalOutput.last().isNotBlank()) {
                    finalOutput.add("")
                }
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