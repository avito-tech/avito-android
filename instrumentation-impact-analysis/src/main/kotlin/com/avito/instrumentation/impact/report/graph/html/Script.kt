package com.avito.instrumentation.impact.report.graph.html

internal fun cytoscapeGraphScript(
    modulesJson: String,
    graphJson: String
): String {
    //language=JavaScript
    return """
        function render(data) {
            var layout = {
                name: 'concentric',
                fit: true,
                minNodeSpacing: 56,
                concentric: function(node) { return node.indegree(); },
                levelWidth: function() { return 10; }
            };

            var style = [{
                    selector: 'node',
                    style: {
                        'width': '40',
                        'height': '40',
                        'font-size': '18pt',
                        'background-color': '#666',
                        'label': 'data(id)'
                    }
                }, {
                    selector: 'node[modified = "true"]',
                    style: {
                        'background-color': '#e57373'
                    }
                }, {
                    selector: 'node[primaryModified = "true"]',
                    style: {
                        'background-color': '#b71c1c'
                    }
                }, {
                    selector: 'node:selected',
                    style: {
                        'width': '70',
                        'height': '70',
                        'font-size': '24pt'
                    }
                }, {
                    selector: 'edge',
                    style: {
                        'width': 0.5,
                        'line-color': '#ccc'
                    }
                }, {
                    selector: 'edge[modified = "true"]',
                    style: {
                        'line-color': '#ef9a9a'
                    }
                }, {
                    selector: 'edge[primaryModified = "true"]',
                    style: {
                        'line-color': '#e57373'
                    }
                }, {
                    selector: 'node.passive',
                    style: {
                        opacity: 0.1
                    }
                }, {
                    selector: 'edge.passive',
                    style: {
                        opacity: 0.1
                    }
                }, {
                    selector: 'edge.active',
                    style: {
                        'width': 2
                    }
                }
            ];

            var cy = cytoscape({
                container: document.getElementById('container'),
                layout: layout,
                style: style,
                elements: data
            });

            var allNodes = cy.elements();
            var activeNodes = allNodes;
            var selectedNode = null;

            setupInput($modulesJson);

            cy.on('select', 'node', function(event) {
                selectedNode = event.target;
                activeNodes = event.target.incomers();

                applyStyles();
            });

            cy.on('unselect', 'node', function() {
                selectedNode = null;
                activeNodes = allNodes;

                applyStyles();
            });

            function applyStyles() {
                cy.startBatch();

                allNodes.removeClass('passive');
                allNodes.removeClass('active');

                if (selectedNode) {
                    activeNodes.addClass('active');
                    selectedNode.addClass('active');
                }

                allNodes
                    .not(activeNodes)
                    .not(selectedNode)
                    .addClass('passive');

                cy.endBatch();
            }

            function escapeNodeId(value) {
                return value.replace(/(:)/g, "\\$1");
            }

            function setupInput(items) {
                horsey(
                    document.querySelector('#search'), {
                        source: [
                            {
                                list: items
                            }
                        ]
                    }
                );

                document.querySelector('.search_form').addEventListener('submit', function(event) {
                    event.preventDefault();

                    var input = document.querySelector('#search');
                    var nodeSelector = 'node#' + escapeNodeId(input.value);

                    console.log(nodeSelector);

                    cy.nodes().unselect();
                    cy.$(nodeSelector).select();
                });
            }
        }

        render($graphJson);
    """.trimIndent()
}
