import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.example.renderer.MermaidRenderer
import org.example.model.Graph
/*
    Unit tests for the MermaidRenderer class.
    These tests cover the generation of the Mermaid definition from a graph in various states.
 */
class MermaidRendererTest {
    private lateinit var renderer: MermaidRenderer
    private lateinit var graph: Graph

    @BeforeEach
    fun setUp() {
        renderer = MermaidRenderer()
        graph = Graph()
    }

    @Test
    fun `generateMermaidDefinition for empty graph should return default message`() {
        val definition = renderer.generateMermaidDefinition(graph)
        assertTrue(definition.contains("No graph defined"))
    }

    @Test
    fun `generateMermaidDefinition with all vertices disabled should return message`() {
        graph.parseInput("A -> B")
        graph.toggleVertex("A")
        graph.toggleVertex("B")

        val definition = renderer.generateMermaidDefinition(graph)
        assertTrue(definition.contains("All vertices disabled"))
    }

    @Test
    fun `generateMermaidDefinition should include edges and standalone vertices`() {
        graph.parseInput("A -> B\nC")

        val definition = renderer.generateMermaidDefinition(graph)

        assertTrue(definition.contains("A") && definition.contains("B") && definition.contains("C"))
        assertTrue(definition.contains("-->"))
    }

    @Test
    fun `escapeMermaidForJavaScript should properly escape special characters`() {
        val input = "flowchart LR\nA --> B\n`dangerous`"
        val escaped = renderer.escapeMermaidForJavaScript(input)

        assertTrue(escaped.contains("\\`dangerous\\`"))
    }
}