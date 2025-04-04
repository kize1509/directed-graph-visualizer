package org.example.view

import javafx.concurrent.Worker
import javafx.scene.web.WebEngine
import org.example.model.Graph
import org.example.renderer.MermaidRenderer
import java.io.File

class WebViewManager(private val webEngine: WebEngine) {

    private val resourceFile = File("src/main/resources/static/index.html")
    private val renderer = MermaidRenderer()
    private var isWebViewReady = false
    private var statusCallback: (String, Boolean) -> Unit = { _, _ -> }


    fun setStatusCallback(callback: (String, Boolean) -> Unit) {
        this.statusCallback = callback
    }


    fun initialize() {
        webEngine.loadWorker.stateProperty().addListener { _, _, newState ->
            if (newState == Worker.State.SUCCEEDED) {
                updateStatus("WebView loaded successfully", false)
                isWebViewReady = true
                setupJavaScriptBridge()
            } else if (newState == Worker.State.FAILED) {
                updateStatus("Failed to load WebView", true)
            }
        }

        webEngine.load(getMermaidHtml())
    }


    private fun setupJavaScriptBridge() {
        webEngine.executeScript("""
            console.log = function(message) {
                java.lang.System.out.println("JS Console: " + message);
            };
            console.error = function(message) {
                java.lang.System.err.println("JS Error: " + message);
            };
        """.toString())

        try {
            val mermaidExists = webEngine.executeScript("typeof mermaid !== 'undefined'") as Boolean
            println("Mermaid library available: $mermaidExists")

            if (mermaidExists) {
                val emptyGraph = "flowchart LR\nA[No graph defined]"
                val success = webEngine.executeScript("renderGraph(`$emptyGraph`)") as Boolean
                println("Initial test rendering: ${if (success) "succeeded" else "failed"}")
                updateStatus("Ready to visualize graphs", false)
            }
        } catch (e: Exception) {
            println("Error during JavaScript bridge setup: ${e.message}")
            updateStatus("Error: ${e.message}", true)
        }
    }


    private fun updateStatus(message: String, isError: Boolean) {
        statusCallback(message, isError)
    }


    fun renderGraph(graph: Graph): Boolean {
        if (!isWebViewReady) {
            println("WebView not ready, can't render graph")
            updateStatus("WebView not ready", true)
            return false
        }

        val mermaidDefinition = renderer.generateMermaidDefinition(graph)
        println("Generated Mermaid definition:\n$mermaidDefinition")

        val escapedDefinition = renderer.escapeMermaidForJavaScript(mermaidDefinition)

        try {
            val success = webEngine.executeScript("renderGraph(`$escapedDefinition`)") as Boolean
            println("Graph render: ${if (success) "succeeded" else "failed"}")

            if (success) {
                updateStatus("Graph rendered successfully", false)
            } else {
                updateStatus("Failed to render graph", true)
            }

            return success
        } catch (e: Exception) {
            println("Error executing JavaScript: ${e.message}")
            updateStatus("Error: ${e.message}", true)
            return false
        }
    }


    fun isReady(): Boolean {
        return isWebViewReady
    }


    private fun getMermaidHtml(): String {
        return resourceFile.toURI().toString()
    }
}