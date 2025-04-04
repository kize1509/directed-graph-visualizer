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

    private val staticResources = File("src/main/resources/static/index.html")
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
                    updateVertexList()
                    updateGraph()
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

        graphInput.textProperty().addListener { _, _, _ -> updateGraph() }
        vertexList.setOnMouseClicked { toggleVertex() }

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
                if (graphInput.text.isNotEmpty()) {
                    updateGraph()
                }
            } else if (newState == Worker.State.FAILED) {
                statusLabel.text = "Failed to load WebView"
                statusLabel.style = "-fx-text-fill: red;"
            }
        }



        webEngine.load(staticResources.toURI().toString())
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
                val emptyGraph = "flowchart LR\nA[No graph defined] --> B[No graph defined]"
                val success = webEngine.executeScript("renderGraph(`$emptyGraph`)") as Boolean
                println("Initial test rendering: ${if (success) "succeeded" else "failed"}")
                statusLabel.text = "Ready to visualize graphs"
            }
        } catch (e: Exception) {
            println("Error during JavaScript bridge setup: ${e.message}")
            statusLabel.text = "Error: ${e.message}"
            statusLabel.style = "-fx-text-fill: red;"
        }
    }

    private fun updateGraph() {
        if (!isWebViewReady) {
            println("WebView not ready yet, skipping graph update")
            return
        }

        vertices.clear()
        val lines = graphInput.text.lines()
        val validEdges = mutableListOf<String>()

        lines.forEach { line ->
            val trimmedLine = line.trim()
            if (trimmedLine.isNotEmpty()) {
                if (trimmedLine.contains("->")) {
                    val parts = trimmedLine.split("->")
                    val from = parts[0].trim()

                    if (parts.size < 2 || parts[1].trim().isEmpty()) {
                        println("Skipping incomplete edge: $trimmedLine")
                        if (from.isNotEmpty()) {
                            vertices.add(from)
                        }
                    } else {
                        val to = parts[1].trim()
                        vertices.add(from)
                        vertices.add(to)
                        validEdges.add(trimmedLine)
                    }
                } else {
                    vertices.add(trimmedLine)
                }
            }
        }

        updateVertexList()
        renderGraph(validEdges)
    }

    private fun updateVertexList() {
        val sortedVertices = vertices.sorted()
        vertexList.items.setAll(sortedVertices)
        vertexList.refresh()
    }

    private fun toggleVertex() {
        val selected = vertexList.selectionModel.selectedItem ?: return
        if (disabledVertices.contains(selected)) {
            disabledVertices.remove(selected)
        } else {
            disabledVertices.add(selected)
        }
        updateVertexList()
        updateGraph()
    }

    private fun renderGraph(edges: List<String>) {
        if (!isWebViewReady) {
            println("WebView not ready, can't render graph")
            return
        }

        if (edges.isEmpty() && vertices.isEmpty()) {
            val emptyGraph = "flowchart LR\nA[No graph defined]"
            val success = webEngine.executeScript("renderGraph(`$emptyGraph`)") as Boolean
            println("Empty graph render: ${if (success) "succeeded" else "failed"}")
            statusLabel.text = "No graph defined"
            return
        }

        // Filter edges to exclude disabled vertices
        val filteredEdges = edges.filterNot { edge ->
            val parts = edge.split("->")
            if (parts.size < 2) return@filterNot true
            val from = parts[0].trim()
            val to = parts[1].trim()
            disabledVertices.contains(from) || disabledVertices.contains(to)
        }

        val mermaidDefinition = StringBuilder("flowchart LR\n")

        val connectedVertices = mutableSetOf<String>()

        filteredEdges.forEach { edge ->
            val parts = edge.split("->")
            val from = parts[0].trim()
            val to = parts[1].trim()
            connectedVertices.add(from)
            connectedVertices.add(to)
        }

        vertices.filter { vertex ->
            !connectedVertices.contains(vertex) && !disabledVertices.contains(vertex)
        }.forEach { vertex ->
            val nodeId = generateSafeNodeId(vertex)
            mermaidDefinition.append("$nodeId([\"$vertex\"])\n")
        }

        filteredEdges.forEach { edge ->
            val parts = edge.split("->")
            val from = parts[0].trim()
            val to = parts[1].trim()
            val fromNodeId = generateSafeNodeId(from)
            val toNodeId = generateSafeNodeId(to)
            mermaidDefinition.append("$fromNodeId([\"$from\"]) --> $toNodeId([\"$to\"])\n")
        }

        if (filteredEdges.isEmpty() && !vertices.any { !disabledVertices.contains(it) }) {
            val emptyGraph = "flowchart LR\nA[All vertices disabled]"
            val success = webEngine.executeScript("renderGraph(`$emptyGraph`)") as Boolean
            println("All-disabled graph render: ${if (success) "succeeded" else "failed"}")
            statusLabel.text = "All vertices disabled"
            return
        }

        println("Generated Mermaid definition:")
        println(mermaidDefinition.toString())

        val escapedDefinition = mermaidDefinition.toString()
            .replace("\\", "\\\\")
            .replace("`", "\\`")

        try {
            val success = webEngine.executeScript("renderGraph(`$escapedDefinition`)") as Boolean
            println("Graph render: ${if (success) "succeeded" else "failed"}")
            statusLabel.text = "Graph rendered successfully"
            statusLabel.style = "-fx-text-fill: green;"
        } catch (e: Exception) {
            println("Error executing JavaScript: ${e.message}")
            statusLabel.text = "Error: ${e.message}"
            statusLabel.style = "-fx-text-fill: red;"
        }
    }


    private fun generateSafeNodeId(nodeText: String): String {
        return "node" + nodeText.hashCode().toString().replace("-", "n")
    }
}

fun main() {
    Application.launch(GraphVisualizer::class.java)
}