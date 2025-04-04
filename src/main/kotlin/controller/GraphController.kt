package org.example.controller

import org.example.model.Graph
import org.example.view.WebViewManager


class GraphController(private val webViewManager: WebViewManager) {
    private val graph = Graph()
    private var vertexListUpdateCallback: () -> Unit = {}


    fun setVertexListUpdateCallback(callback: () -> Unit) {
        this.vertexListUpdateCallback = callback
    }


    fun updateGraph(input: String) {
        graph.parseInput(input)
        vertexListUpdateCallback()
        renderGraph()
    }


    fun toggleVertex(vertex: String) {
        graph.toggleVertex(vertex)
        vertexListUpdateCallback()
        renderGraph()
    }


    fun resetAllVertices() {
        graph.resetDisabledVertices()
        vertexListUpdateCallback()
        renderGraph()
    }


    fun getVertices(): List<String> {
        return graph.getVertices()
    }


    fun isVertexDisabled(vertex: String): Boolean {
        return graph.isVertexDisabled(vertex)
    }


    fun renderGraph(): Boolean {
        return webViewManager.renderGraph(graph)
    }
}