var mermaidTheme = codibase.server.isDarkMode()? "dark": "default";
mermaid.mermaidAPI.initialize({theme: mermaidTheme, startOnLoad:false});