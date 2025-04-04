package org.example.model


class Graph {
    private val vertices = mutableSetOf<String>()
    private val edges = mutableListOf<Edge>()
    private val disabledVertices = mutableSetOf<String>()


    data class Edge(val from: String, val to: String)


    fun parseInput(input: String): Int {
        vertices.clear()
        edges.clear()

        var validEdgeCount = 0

        input.lines().forEach { line ->
            val trimmedLine = line.trim()
            if (trimmedLine.isNotEmpty()) {
                if (trimmedLine.contains("->")) {
                    val parts = trimmedLine.split("->")
                    val from = parts[0].trim()

                    // Check for incomplete edges
                    if (parts.size < 2 || parts[1].trim().isEmpty()) {
                        // Still add the from node as a vertex if it exists
                        if (from.isNotEmpty()) {
                            vertices.add(from)
                        }
                    } else {
                        // Complete edge
                        val to = parts[1].trim()
                        vertices.add(from)
                        vertices.add(to)
                        edges.add(Edge(from, to))
                        validEdgeCount++
                    }
                } else {
                    // Handle standalone vertex
                    vertices.add(trimmedLine)
                }
            }
        }

        return validEdgeCount
    }


    fun toggleVertex(vertex: String): Boolean {
        return if (disabledVertices.contains(vertex)) {
            disabledVertices.remove(vertex)
            true // Now enabled
        } else {
            disabledVertices.add(vertex)
            false // Now disabled
        }
    }


    fun resetDisabledVertices(): Int {
        val count = disabledVertices.size
        disabledVertices.clear()
        return count
    }


    fun isVertexDisabled(vertex: String): Boolean {
        return disabledVertices.contains(vertex)
    }


    fun getVertices(): List<String> {
        return vertices.sorted()
    }


    fun getEnabledEdges(): List<Edge> {
        return edges.filterNot { edge ->
            disabledVertices.contains(edge.from) || disabledVertices.contains(edge.to)
        }
    }


    fun getStandaloneVertices(): List<String> {
        val connectedVertices = mutableSetOf<String>()

        edges.forEach { edge ->
            connectedVertices.add(edge.from)
            connectedVertices.add(edge.to)
        }

        return vertices.filter { vertex ->
            !connectedVertices.contains(vertex) && !disabledVertices.contains(vertex)
        }
    }


    fun areAllVerticesDisabled(): Boolean {
        return vertices.isNotEmpty() && vertices.all { disabledVertices.contains(it) }
    }


    fun isEmpty(): Boolean {
        return vertices.isEmpty()
    }
}