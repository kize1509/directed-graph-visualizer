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

class GraphVisualizer : Application() {
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

    // function to start the main window and set up the event listeners
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

        // Set up main layout
        root.left = leftControls
        root.center = graphView


        // Final setup
        val scene = Scene(root, 900.0, 650.0)
        stage.scene = scene
        stage.title = "Graph Visualizer"
        stage.show()

    }


}

fun main() {
    Application.launch(GraphVisualizer::class.java)
}