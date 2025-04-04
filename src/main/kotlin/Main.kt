package org.example

import javafx.application.Application
import javafx.concurrent.Worker
import javafx.geometry.Insets
import javafx.scene.Scene
import javafx.scene.control.*
import javafx.scene.layout.*
import javafx.scene.paint.Color
import javafx.scene.web.WebEngine
import javafx.scene.web.WebView
import javafx.stage.Stage
import java.io.File

class GraphVisualizer : Application() {
    private val file = File("src/main/resources/static/index.html")
    private val graphInput = TextArea().apply {
        promptText = "Enter graph edges (one per line)\nFormat: NodeA -> NodeB"
        prefHeight = 200.0
    }
    private val vertexList = ListView<String>().apply {
        prefHeight = 200.0
    }
    private val webView = WebView()
    private val webEngine: WebEngine = webView.engine
    private val vertices = mutableSetOf<String>()
    private val disabledVertices = mutableSetOf<String>()
    private var isWebViewReady = false
    private val statusLabel = Label("Ready").apply {
        padding = Insets(5.0)
        style = "-fx-text-fill: gray; -fx-font-style: italic;"
    }

    override fun start(stage: Stage) {
        val root = BorderPane()

        val inputArea = VBox(5.0).apply {
            padding = Insets(10.0)
            children.add(Label("Graph Input").apply {
                style = "-fx-font-weight: bold; -fx-font-size: 14px;"
            })
            children.add(graphInput)
            children.add(Button("Clear").apply {
                setOnAction { graphInput.clear() }
            })
        }

        val vertexArea = VBox(5.0).apply {
            padding = Insets(10.0)
            children.add(Label("Vertex List").apply {
                style = "-fx-font-weight: bold; -fx-font-size: 14px;"
            })
            children.add(Label("Click to toggle vertices").apply {
                style = "-fx-font-style: italic; -fx-text-fill: gray; -fx-font-size: 12px;"
            })
            children.add(vertexList)

            children.add(Button("Reset All Vertices").apply {
                setOnAction {
                    disabledVertices.clear()

                }
            })
        }

        val leftControls = VBox(inputArea, vertexArea).apply {
            prefWidth = 250.0
        }

        val graphView = VBox().apply {
            padding = Insets(10.0)
            children.addAll(
                Label("Graph Visualization").apply {
                    style = "-fx-font-weight: bold; -fx-font-size: 14px;"
                },
                webView.apply {
                    VBox.setVgrow(this, Priority.ALWAYS)
                },
                statusLabel
            )
        }

        vertexList.setCellFactory {
            object : ListCell<String>() {
                override fun updateItem(item: String?, empty: Boolean) {
                    super.updateItem(item, empty)
                    if (empty || item == null) {
                        text = null
                        style = ""
                    } else {
                        text = item
                        if (disabledVertices.contains(item)) {
                            style = "-fx-text-fill: gray; -fx-strikethrough: true;"
                        } else {
                            style = "-fx-text-fill: black;"
                        }
                    }
                }
            }
        }

        root.left = leftControls
        root.center = graphView



        val scene = Scene(root, 900.0, 650.0)
        stage.scene = scene
        stage.title = "Graph Visualizer"
        stage.show()

        loadMermaid()
    }

    private fun loadMermaid() {
        webEngine.loadWorker.stateProperty().addListener { _, _, newState ->
            if (newState == Worker.State.SUCCEEDED) {
                statusLabel.text = "WebView loaded successfully"
                isWebViewReady = true
                setupJavaScriptBridge()

            } else if (newState == Worker.State.FAILED) {
                statusLabel.text = "Failed to load WebView"
                statusLabel.style = "-fx-text-fill: red;"
            }
        }

        webEngine.load(file.toURI().toString())
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
                val success = webEngine.executeScript("""
                    renderGraph("flowchart LR\\nA-->B")
                """) as Boolean
                println("Initial test rendering: ${if (success) "succeeded" else "failed"}")
                statusLabel.text = "Ready to visualize graphs"
            }
        } catch (e: Exception) {
            println("Error during JavaScript bridge setup: ${e.message}")
            statusLabel.text = "Error: ${e.message}"
            statusLabel.style = "-fx-text-fill: red;"
        }
    }
}

fun main() {
    Application.launch(GraphVisualizer::class.java)
}