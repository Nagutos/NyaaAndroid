package com.nagutos.nyaaandroid.ui.components

import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import org.json.JSONObject

@Composable
fun NyaaMarkdownEngine(
    rawMarkdown: String,
    modifier: Modifier = Modifier
) {
    // On utilise JSONObject.quote pour transformer ta string en format JS
    // parfaitement sécurisé (gère les sauts de ligne, quotes, etc.)
    val escapedMarkdown = JSONObject.quote(rawMarkdown)

    val htmlTemplate = """
        <!DOCTYPE html>
        <html>
        <head>
            <meta name="viewport" content="width=device-width, initial-scale=1.0">
            <script src="https://cdnjs.cloudflare.com/ajax/libs/markdown-it/13.0.1/markdown-it.min.js"></script>
            <style>
                body { 
                    font-family: -apple-system, system-ui, sans-serif;
                    font-size: 14px; line-height: 1.5; color: #212529;
                    background-color: transparent !important; margin: 0; padding: 10px;
                }
                /* Styles des tables Nyaa (issus de leur code Bootstrap) */
                .table { 
                    width: 100%; max-width: 100%; margin-bottom: 1rem; 
                    border-collapse: collapse; display: table !important; 
                }
                .table-bordered { border: 1px solid #dee2e6; }
                .table-bordered th, .table-bordered td { 
                    border: 1px solid #dee2e6; padding: 0.75rem; vertical-align: middle; 
                }
                .table-striped tbody tr:nth-of-type(odd) { background-color: #f6f8fa; }
                th { font-weight: bold; background-color: #f8f9fa; text-align: center; }
                td { text-align: center; }
                
                img { max-width: 100%; height: auto; display: block; margin: 10px 0; }
                .scroll-wrapper { overflow-x: auto; width: 100%; }
            </style>
        </head>
        <body>
            <div class="scroll-wrapper">
                <div id="target"></div>
            </div>

            <script>
                // 1. La fonction de décodage EXACTE de ton code Nyaa
                function htmlDecode(input) {
                    var e = document.createElement('div');
                    e.innerHTML = input;
                    return e.childNodes.length === 0 ? "" : e.childNodes[0].nodeValue;
                }

                // 2. Options EXACTES de Nyaa
                var markdownOptions = {
                    html: false,
                    breaks: true,
                    linkify: true,
                    typographer: true
                };

                // 3. Initialisation identique
                var md = window.markdownit(markdownOptions);

                // 4. Règle de table IDENTIQUE à Nyaa
                md.renderer.rules.table_open = function() {
                    return '<table class="table table-striped table-bordered" style="width: auto;">';
                };

                // 5. Rendu Final
                try {
                    // On récupère la donnée injectée par Kotlin
                    var rawInput = $escapedMarkdown;
                    
                    // On applique le décodage DOM comme Nyaa
                    var decoded = htmlDecode(rawInput);
                    
                    // RÉPARATION CRUCIALE : Nyaa utilise "| |" pour séparer les lignes de tableau.
                    // On doit les transformer en vrais retours à la ligne \n pour le parser.
                    var fixed = decoded.replace(/\|\s*\|/g, "|\n|");
                    
                    // On s'assure qu'un tableau est toujours isolé par des lignes vides
                    fixed = fixed.replace(/([^\n])\n\|/g, "$1\n\n|");

                    document.getElementById('target').innerHTML = md.render(fixed);
                } catch (e) {
                    document.getElementById('target').innerText = "Erreur rendu : " + e.message;
                }
            </script>
        </body>
        </html>
    """.trimIndent()

    AndroidView(
        // On force une hauteur minimale pour que la WebView ne soit pas écrasée
        modifier = modifier.heightIn(min = 600.dp, max = 3000.dp),
        factory = { context ->
            WebView(context).apply {
                setBackgroundColor(0)
                webViewClient = WebViewClient()
                settings.apply {
                    javaScriptEnabled = true
                    domStorageEnabled = true
                }
            }
        },
        update = { webView ->
            // On utilise l'URL de Nyaa comme base pour charger les images correctement
            webView.loadDataWithBaseURL("https://nyaa.si", htmlTemplate, "text/html", "utf-8", null)
        }
    )
}