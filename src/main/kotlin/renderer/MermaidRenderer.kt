package org.example.renderer

import org.example.model.Graph


class MermaidRenderer {


    fun generateMermaidDefinition(graph: Graph): String {
        if (graph.isEmpty()) {
            return "flowchart LR\nA[No graph defined]"
        }

        if (graph.areAllVerticesDisabled()) {
            return "flowchart LR\nA[All vertices disabled]"
        }

        val mermaidBuilder = StringBuilder("flowchart LR\n")

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
        // Replace problematic characters and create a safe ID
        return "node" + nodeText.hashCode().toString().replace("-", "n")
    }


    fun escapeMermaidForJavaScript(definition: String): String {
        return definition
            .replace("\\", "\\\\")
            .replace("`", "\\`")
    }
}