import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.example.model.Graph


/*
    Unit tests for the Graph class.
    These tests cover the parsing of input, toggling vertices, and checking graph state.
 */
class GraphTest {
    private lateinit var graph: Graph

    @BeforeEach
    fun setUp() {
        graph = Graph()
    }

    @Test
    fun `parseInput with empty string should create empty graph`() {
        val result = graph.parseInput("")
        assertEquals(0, result)
        assertTrue(graph.isEmpty())
    }

    @Test
    fun `parseInput with valid edges should create correct graph`() {
        val input = """
            A -> B
            B -> C
            C -> A
        """.trimIndent()

        val result = graph.parseInput(input)

        assertEquals(3, result)
        assertEquals(listOf("A", "B", "C"), graph.getVertices())
        assertEquals(3, graph.getEnabledEdges().size)
    }

    @Test
    fun `parseInput with standalone vertices should create correct graph`() {
        val input = """
            A
            B
            C
        """.trimIndent()

        val result = graph.parseInput(input)

        assertEquals(0, result)  // No edges
        assertEquals(listOf("A", "B", "C"), graph.getVertices())
        assertEquals(3, graph.getStandaloneVertices().size)
    }

    @Test
    fun `toggleVertex should correctly toggle disabled state`() {
        graph.parseInput("A -> B")

        assertFalse(graph.isVertexDisabled("A"))

        assertFalse(graph.toggleVertex("A"))
        assertTrue(graph.isVertexDisabled("A"))

        assertTrue(graph.toggleVertex("A"))
        assertFalse(graph.isVertexDisabled("A"))
    }

    @Test
    fun `resetDisabledVertices should enable all vertices`() {
        graph.parseInput("A -> B\nB -> C")

        graph.toggleVertex("A")
        graph.toggleVertex("B")

        assertEquals(2, graph.resetDisabledVertices())

        assertFalse(graph.isVertexDisabled("A"))
        assertFalse(graph.isVertexDisabled("B"))
    }

    @Test
    fun `getEnabledEdges should filter out edges with disabled vertices`() {
        graph.parseInput("A -> B\nB -> C\nC -> D")

        graph.toggleVertex("B")

        val enabledEdges = graph.getEnabledEdges()
        assertEquals(1, enabledEdges.size)
        assertEquals("C", enabledEdges[0].from)
        assertEquals("D", enabledEdges[0].to)
    }

    @Test
    fun `areAllVerticesDisabled should return correct state`() {
        graph.parseInput("A -> B\nC")

        assertFalse(graph.areAllVerticesDisabled())

        graph.toggleVertex("A")
        graph.toggleVertex("B")
        graph.toggleVertex("C")

        assertTrue(graph.areAllVerticesDisabled())
    }
}