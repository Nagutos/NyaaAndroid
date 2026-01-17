package com.nagutos.nyaaandroid.ui.components

import android.graphics.Color
import android.webkit.WebSettings
import android.webkit.WebView
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView

@Composable
fun HtmlDescriptionView(
    htmlContent: String,
    modifier: Modifier = Modifier
) {
    // CSS injecté pour forcer le style de l'application
    // 1. color: #e0e0e0 -> Texte quasi blanc pour le mode sombre
    // 2. img { max-width: 100% } -> Empêche les images de dépasser de l'écran (scroll horizontal)
    // 3. a { color: ... } -> Rend les liens visibles
    val customCss = """
        <style>
            @font-face {
                font-family: 'Roboto';
                src: local('Roboto');
            }
            body {
                color: #e3e3e3; 
                background-color: transparent;
                font-family: 'Roboto', sans-serif;
                font-size: 15px;
                line-height: 1.6;
                margin: 0;
                padding: 0;
                word-wrap: break-word;
            }
            a { color: #64b5f6; text-decoration: none; font-weight: bold; }
            
            /* Gestion des IMAGES : Elles s'adaptent à la largeur */
            img { 
                max-width: 100% !important; 
                height: auto; 
                display: block; 
                margin: 8px auto; 
                border-radius: 8px;
            }
            
            /* Gestion des TABLEAUX Nyaa */
            table { 
                width: 100% !important; 
                border-collapse: collapse; 
                margin: 10px 0;
                border: 1px solid #444;
            }
            th, td {
                border: 1px solid #444;
                padding: 8px;
                text-align: left;
                font-size: 13px;
            }
            th { background-color: #333; }
            
            /* Citations et Code */
            blockquote {
                border-left: 4px solid #64b5f6;
                margin: 0;
                padding-left: 16px;
                color: #b0b0b0;
            }
        </style>
    """.trimIndent()

    // On encapsule le contenu brut venant de Nyaa dans une vraie page HTML
    val htmlData = """
        <!DOCTYPE html>
        <html>
        <head>
            <meta name="viewport" content="width=device-width, initial-scale=1.0">
            $customCss
        </head>
        <body>
            $htmlContent
        </body>
        </html>
    """.trimIndent()

    AndroidView(
        modifier = modifier,
        factory = { context ->
            WebView(context).apply {
                // Fond transparent pour se fondre dans ton app
                setBackgroundColor(Color.TRANSPARENT)

                settings.apply {
                    defaultTextEncodingName = "utf-8"
                    loadWithOverviewMode = true
                    useWideViewPort = false
                    // Sécurité : on désactive le JS sauf si nécessaire
                    javaScriptEnabled = false
                }
            }
        },
        update = { webView ->
            // loadDataWithBaseURL permet de charger le HTML brut correctement
            webView.loadDataWithBaseURL(null, htmlData, "text/html", "utf-8", null)
        }
    )
}