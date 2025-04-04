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

    function renderGraph(definition) {
        try {
            document.getElementById('time-container').style.display = 'block';
            document.getElementById('time-container').textContent = 'last update: ' + new Date().toLocaleString();

            document.getElementById('error-container').style.display = 'none';

            const container = document.getElementById('diagram-container');
            container.innerHTML = '<pre class="mermaid">' + definition + '</pre>';

            mermaid.init(undefined, '.mermaid');

            return true;
        } catch (e) {
            console.error("Mermaid rendering error:", e);
            document.getElementById('error-container').style.display = 'block';
            document.getElementById('error-container').textContent = "Error: " + e.message;
            return false;
        }
    }