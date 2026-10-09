package com.example.ui.components

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import com.example.ui.theme.SyntaxComment
import com.example.ui.theme.SyntaxFunction
import com.example.ui.theme.SyntaxKeyword
import com.example.ui.theme.SyntaxNumber
import com.example.ui.theme.SyntaxPunctuation
import com.example.ui.theme.SyntaxString
import com.example.ui.theme.SyntaxType

object CodeSyntaxHighlighter {

    private val KEYWORDS = setOf(
        "import", "package", "class", "interface", "fun", "val", "var", "override",
        "void", "const", "final", "extends", "implements", "return", "if", "else",
        "while", "for", "switch", "case", "default", "break", "continue", "new",
        "this", "super", "null", "true", "false", "async", "await", "yield",
        "try", "catch", "finally", "throw", "throws", "public", "private", "protected",
        "static", "data", "sealed", "enum", "late", "required", "as", "is", "in"
    )

    private val TYPES = setOf(
        "String", "Int", "Double", "Boolean", "List", "Map", "Set", "Widget",
        "StatelessWidget", "StatefulWidget", "BuildContext", "MaterialApp", "Scaffold",
        "AppBar", "Text", "Center", "Column", "Row", "Container", "Icon", "Icons",
        "Color", "Colors", "ThemeData", "ComponentActivity", "Bundle", "Modifier",
        "State", "Future", "Stream", "void", "dynamic", "Object", "Any"
    )

    fun highlight(code: String, language: String): AnnotatedString {
        return buildAnnotatedString {
            append(code)

            // Highlight comments
            val commentRegex = Regex("(//.*$)|(/\\*[\\s\\S]*?\\*/)", RegexOption.MULTILINE)
            for (match in commentRegex.findAll(code)) {
                addStyle(
                    SpanStyle(color = SyntaxComment, fontStyle = androidx.compose.ui.text.font.FontStyle.Italic),
                    match.range.first,
                    match.range.last + 1
                )
            }

            // Highlight strings
            val stringRegex = Regex("(\"[^\"]*\")|('[^']*')")
            for (match in stringRegex.findAll(code)) {
                addStyle(
                    SpanStyle(color = SyntaxString),
                    match.range.first,
                    match.range.last + 1
                )
            }

            // Highlight words (Keywords & Types)
            val wordRegex = Regex("\\b[A-Za-z_][A-Za-z0-9_]*\\b")
            for (match in wordRegex.findAll(code)) {
                val word = match.value
                val start = match.range.first
                val end = match.range.last + 1

                if (KEYWORDS.contains(word)) {
                    addStyle(
                        SpanStyle(color = SyntaxKeyword, fontWeight = FontWeight.Bold),
                        start,
                        end
                    )
                } else if (TYPES.contains(word) || word.firstOrNull()?.isUpperCase() == true) {
                    addStyle(
                        SpanStyle(color = SyntaxType, fontWeight = FontWeight.SemiBold),
                        start,
                        end
                    )
                }
            }

            // Highlight numbers
            val numberRegex = Regex("\\b[0-9]+(\\.[0-9]+)?\\b")
            for (match in numberRegex.findAll(code)) {
                addStyle(
                    SpanStyle(color = SyntaxNumber),
                    match.range.first,
                    match.range.last + 1
                )
            }
        }
    }
}
