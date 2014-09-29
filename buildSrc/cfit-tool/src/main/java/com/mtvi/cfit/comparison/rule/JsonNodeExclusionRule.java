package com.mtvi.cfit.comparison.rule;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.ArrayUtils;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.node.ArrayNode;
import org.codehaus.jackson.node.ObjectNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;

/**
 * JSON based exclusion rule.
 * <p>
 *     Could be used for excluding block/nodes from JSONPath response.
 *     <br/>
 *     Removes block/nodes defined by given pattern. Nodes paths, are delimited by {@see #PATH_DELIMITER}.
 *     <br/>
 *     Sample definitions:
 *     <br/>
 *     <ul>
 *         <li>summary::executionTime - removes executionTime node on summary node, that is contained by top level root node.</li>
 *         <li>metadata - removes field from root node.</li>
 *         <li>response::docs::CMRs','*CMRs' - Removes 'CMRs' node on each top level elements in 'response/docs' elements.
 *         Also removes removal of CMRs field on all sub nodes.
 *         </li>
 *     </ul>
 * </p>
 *
 * @author zenind.
 */
public final class JsonNodeExclusionRule extends BaseComparisonRule {

    /** Log.*/
    private static final Logger LOG = LoggerFactory.getLogger(JsonNodeExclusionRule.class);
    /** Node mapper.*/
    private static final ObjectMapper MAPPER = new ObjectMapper();
    /** Delimiter.*/
    private static final String PATH_DELIMITER = "::";
    /** Apply for all symbol.*/
    private static final String APPLY_FOR_ALL_SYMBOL = "*";
    /** By Path strategy.*/
    private static final ByPathNodeRemovalStrategy BY_PATH = new ByPathNodeRemovalStrategy();
    /** All Nodes Scan strategy.*/
    private static final AllNodesScanRemovalStrategy ALL_NODES = new AllNodesScanRemovalStrategy();

    /**
     * Constructor.
     * @param alias - rule alias.
     * @param nodes - list of nodes to be excluded.
     */
    public JsonNodeExclusionRule(String alias, List<String> nodes) {
        super(alias, nodes);
    }

    @Override
    public String apply(String content) {
        if (!CollectionUtils.isEmpty(getValues())) {
            try {
                JsonNode view = MAPPER.readTree(content);
                for (String path : getValues()) {
                    NodeRemovalStrategy strategy = getStrategy(path);
                    strategy.removeNode(view, path);
                }

                return view.toString();
            } catch (IOException e) {
                LOG.debug(String.format("Error occurred while applying rule '%s'", this));
            }
        }
        return content;
    }

    private NodeRemovalStrategy getStrategy(String path) {
        if (!path.startsWith(APPLY_FOR_ALL_SYMBOL)) {
            return BY_PATH;
        } else {
            return ALL_NODES;
        }
    }

    /**
     * Nodes removal strategy.
     */
    interface NodeRemovalStrategy {
        /**
         * Removes nodes by given path.
         * @param root - root node.
         * @param removalPath - removal path
         */
        void removeNode(JsonNode root, String removalPath);

    }

    /**
     * Recursively scans child nodes and removes nodes represented by removal path.
     *
     * @see #ALL_NODES
     */
    private static class AllNodesScanRemovalStrategy implements NodeRemovalStrategy {

        @Override
        public void removeNode(JsonNode root, String removalPath) {
            String nodeName = removalPath.substring(1);
            handleNode(root, nodeName);
        }

        private void handleNode(JsonNode node, String fieldName) {
            if (node instanceof ArrayNode) {
                for (JsonNode childNode : node) {
                    handleNode(childNode, fieldName);
                }
            }

            if (node instanceof ObjectNode) {
                ObjectNode objectNode = (ObjectNode) node;
                if (node.has(fieldName)) {
                    objectNode.remove(fieldName);
                }

                for (JsonNode childNode : objectNode) {
                    handleNode(childNode, fieldName);
                }
            }
        }
    }

    /**
     * Does traversing of root node by given path and removes found node.
     */
    private static class ByPathNodeRemovalStrategy implements NodeRemovalStrategy {

        @Override
        public void removeNode(JsonNode root, String removalPath) {
            String[] nodes = removalPath.split(PATH_DELIMITER);
            apply(root, nodes);
        }

        private void apply(JsonNode parentNode, String[] nodes) {
            int nodeIndex = 0;
            for (String node : nodes) {
                nodeIndex++;
                if (!parentNode.has(node)) {
                    return;
                }

                if (nodeIndex == nodes.length) {
                    if (parentNode instanceof ObjectNode) {
                        applyForNode(parentNode, nodes[nodes.length - 1]);
                        return;
                    }
                }

                parentNode = parentNode.findPath(node);

                if (parentNode instanceof ArrayNode) {
                    String[] nodesToProcess = (String[]) ArrayUtils.subarray(nodes, nodeIndex, nodes.length);
                    for (JsonNode childNode : parentNode) {
                        apply(childNode, nodesToProcess);
                    }
                }

                if ((nodeIndex + 1) == nodes.length) {
                    if (parentNode instanceof ObjectNode) {
                        applyForNode(parentNode, nodes[nodes.length - 1]);
                    } else if (parentNode instanceof ArrayNode) {
                        String[] nodesToProcess = (String[]) ArrayUtils.subarray(nodes, nodeIndex, nodes.length);
                        for (JsonNode childNode : parentNode) {
                            apply(childNode, nodesToProcess);
                        }
                    }
                    return;
                }
            }
        }

        private void applyForNode(JsonNode parentNode, String fieldName) {
            ((ObjectNode) parentNode).remove(fieldName);
        }

    }
}
