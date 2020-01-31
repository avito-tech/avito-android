package com.avito.instrumentation.impact.report.graph.html

import com.google.gson.Gson
import kotlinx.html.ScriptType
import kotlinx.html.body
import kotlinx.html.div
import kotlinx.html.form
import kotlinx.html.head
import kotlinx.html.html
import kotlinx.html.id
import kotlinx.html.input
import kotlinx.html.lang
import kotlinx.html.link
import kotlinx.html.meta
import kotlinx.html.script
import kotlinx.html.stream.createHTML
import kotlinx.html.style
import kotlinx.html.title
import kotlinx.html.unsafe

internal fun cytoscapeGraphHtml(
    gson: Gson,
    nodes: Set<CytoscapeNode<CytoscapeNodeData>>,
    edges: Set<CytoscapeNode<CytoscapeEdgeData>>
): String {

    val nodeNames: Set<String> = nodes
        .map { it.data.id }
        .toSet()

    val graphData = nodes + edges

    val graphJson = gson.toJson(graphData)
    val nodeNamesJson = gson.toJson(nodeNames)

    return createHTML(prettyPrint = true).html {
        lang = "en"

        head {
            title = "Modules Graph"

            meta(charset = "UTF-8")

            script(type = ScriptType.textJavaScript, src = "https://js.cytoscape.org/js/cytoscape.min.js") {}
            script(type = ScriptType.textJavaScript, src = "https://bevacqua.github.io/horsey/dist/horsey.js") {}

            link(href = "https://bevacqua.github.io/horsey/dist/horsey.css", rel = "stylesheet", type = "text/css")

            style {
                unsafe {
                    raw(
                        """
                            form {
                                margin: 0;
                            }

                            body, html, svg, #container {
                                margin: 0;
                                padding: 0;
                                width: 100%;
                                height: 100%;
                                overflow: hidden;
                                position: absolute;
                            }

                            .search_container {
                                position: absolute;
                                margin: 30px;
                                background-color: black;
                                padding: 20px 25px;
                                box-shadow: 0 4px 8px 0 rgba(0,0,0,0.2);
                                background-color: white;
                                border-radius: 6px;
                            }

                            #search {
                                height: 30px;
                                width: 300px;
                                font-size: 10pt;
                                padding: 8px;
                                color: #666;
                                border: solid 1px;
                                border-radius: 6px;
                                border-color: #ddd;
                                outline: 0;
                            }
                            #search::placeholder {
                                color: #ddd;
                            }
                    """.trimIndent()
                    )
                }
            }
        }

        body {
            div {
                id = "container"
            }

            div(classes = "search_container") {

                form(classes = "search_form") {
                    input {
                        autoComplete = false
                        id = "search"
                        placeholder = "Enter module name"
                    }
                }
            }

            script(type = ScriptType.textJavaScript) {
                unsafe {
                    raw(
                        cytoscapeGraphScript(
                            modulesJson = nodeNamesJson,
                            graphJson = graphJson
                        )
                    )
                }
            }
        }
    }
}
