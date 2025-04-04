// Initialize Mermaid with specific configuration
    mermaid.initialize({
        startOnLoad: true,
        theme: 'default',
        flowchart: {
            useMaxWidth: false,
            htmlLabels: true,
            curve: 'linear'
        },
        securityLevel: 'loose'
    });

    // Function to be called from Java
    function renderGraph(definition) {
        try {
            // For debugging
            document.getElementById('debug-container').style.display = 'block';
            document.getElementById('debug-container').textContent = 'Rendering: ' + definition;

            // Clear any previous errors
            document.getElementById('error-container').style.display = 'none';

            // Insert the graph definition
            const container = document.getElementById('diagram-container');
            container.innerHTML = '<pre class="mermaid">' + definition + '</pre>';

            // Render the graph
            mermaid.init(undefined, '.mermaid');

            return true;
        } catch (e) {
            console.error("Mermaid rendering error:", e);
            document.getElementById('error-container').style.display = 'block';
            document.getElementById('error-container').textContent = "Error: " + e.message;
            return false;
        }
    }