package org.example

import javafx.application.Application
import javafx.geometry.Insets
import javafx.scene.Scene
import javafx.scene.control.*
import javafx.scene.layout.*
import javafx.scene.web.WebView
import javafx.stage.Stage
import org.example.controller.GraphController
import org.example.view.WebViewManager


class GraphVisualizer : Application() {
    private val graphInput = TextArea().apply {
        promptText = "Enter graph edges (one per line)\nFormat: NodeA -> NodeB"
        prefHeight = 200.0
    }

    private val vertexList = ListView<String>().apply {
        prefHeight = 200.0
    }

    private val webView = WebView()
    private val statusLabel = Label("Ready").apply {
        padding = Insets(5.0)
        style = "-fx-text-fill: gray; -fx-font-style: italic;"
    }

    private lateinit var webViewManager: WebViewManager
    private lateinit var graphController: GraphController

    override fun start(stage: Stage) {
        webViewManager = WebViewManager(webView.engine)
        graphController = GraphController(webViewManager)

        setupUI(stage)

        setupCallbacks()

        webViewManager.initialize()
    }


    private fun setupUI(stage: Stage) {
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
                setOnAction { graphController.resetAllVertices() }
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
                        if (graphController.isVertexDisabled(item)) {
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

        graphInput.textProperty().addListener { _, _, _ ->
            graphController.updateGraph(graphInput.text)
        }

        vertexList.setOnMouseClicked {
            val selected = vertexList.selectionModel.selectedItem
            if (selected != null) {
                graphController.toggleVertex(selected)
            }
        }

        val scene = Scene(root, 900.0, 650.0)
        stage.scene = scene
        stage.title = "Graph Visualizer"
        stage.show()
    }

    private fun setupCallbacks() {
        webViewManager.setStatusCallback { message, isError ->
            statusLabel.text = message
            statusLabel.style = if (isError)
                "-fx-text-fill: red;"
            else
                "-fx-text-fill: green;"
        }

        graphController.setVertexListUpdateCallback {
            updateVertexList()
        }
    }


    private fun updateVertexList() {
        vertexList.items.setAll(graphController.getVertices())
        vertexList.refresh()
    }
}


fun main() {
    Application.launch(GraphVisualizer::class.java)
}