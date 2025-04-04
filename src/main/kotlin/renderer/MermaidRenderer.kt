package org.example.renderer

import org.example.model.Graph


class MermaidRenderer {

 /*
    Generates a Mermaid definition from the provided graph.
    The graph is represented in a flowchart (left to right) format, with nodes and edges.
    If the graph is empty or all vertices are disabled, appropriate messages are returned.
    Each node is given a unique ID to avoid conflicts in the Mermaid syntax.
  */
    fun generateMermaidDefinition(graph: Graph): String {
        if (graph.isEmpty()) {
            return "flowchart LR\nA[No graph defined]"
        }

        if (graph.areAllVerticesDisabled()) {
            return "flowchart LR\nA[All vertices disabled]"
        }

        val mermaidBuilder = StringBuilder("flowchart LR\n")

        if (graph.getEnabledEdges().isEmpty() && graph.getStandaloneVertices().isEmpty()) {
            return "flowchart LR\nA[No graph defined]"
        }

        graph.getStandaloneVertices().forEach { vertex ->
            val nodeId = generateSafeNodeId(vertex)
            mermaidBuilder.append("$nodeId([\"$vertex\"])\n")
        }

        graph.getEnabledEdges().forEach { edge ->
            val fromNodeId = generateSafeNodeId(edge.from)
            val toNodeId = generateSafeNodeId(edge.to)
            mermaidBuilder.append("$fromNodeId([\"${edge.from}\"]) --> $toNodeId([\"${edge.to}\"])\n")
        }


        return mermaidBuilder.toString()
    }


    private fun generateSafeNodeId(nodeText: String): String {
        return "node" + nodeText.hashCode().toString().replace("-", "n")
    }


    fun escapeMermaidForJavaScript(definition: String): String {
        return definition
            .replace("\\", "\\\\")
            .replace("`", "\\`")
    }
}