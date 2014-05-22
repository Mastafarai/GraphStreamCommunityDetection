package th.utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import org.graphstream.graph.Edge;
import org.graphstream.graph.Graph;
import org.graphstream.graph.Node;

/**
 *
 * @author Anastasis Andronidis <anastasis90@yahoo.gr>
 */
public class ExtractCommunities {

    public static int MaxToMin(Graph graph) {
        return MaxToMin(graph, new Integer[0]);
    }

    public static int MaxToMin(Graph graph, Integer[] fixedIDs) {
        // fixedIDs is not supported yet!
        
        SortedMap<Double, List<Integer>> edgeWeightsMap = new TreeMap<Double, List<Integer>>(Collections.reverseOrder());
        Set<Edge> subEdgeGraph = new HashSet<Edge>(50);
        int communityNum = 0;
        
        for (Node n : graph) {
            if (!n.hasAttribute("visited")) {
                n.setAttribute("visited", 1);
                for (Edge e : n.getEdgeSet()) {
                    subEdgeGraph.add(e);
                }

                // Set vertices that have no edges, as
                // independed communities
                if (subEdgeGraph.isEmpty()) {
                    n.setAttribute("community", ++communityNum);
                }
                
                // Go for BFS
                Iterator<Node> breadth = n.getBreadthFirstIterator();
                while (breadth.hasNext()) {
                    Node next = breadth.next();
                    if (!next.hasAttribute("visited")) {
                        next.setAttribute("visited", 1);
                        for (Edge e : next.getEdgeSet()) {
                            subEdgeGraph.add(e);
                        }
                    }
                }
                
                // Create edge weight map
                for (Edge e : subEdgeGraph) {
                    Double w = e.getAttribute("weight");

                    if (edgeWeightsMap.containsKey(w)) {
                        edgeWeightsMap.get(w).add(e.getIndex());
                    } else {
                        List<Integer> l = new ArrayList<Integer>(4);
                        l.add(e.getIndex());
                        edgeWeightsMap.put(w, l);
                    }
                }
                
                for (Entry<Double, List<Integer>> entry : edgeWeightsMap.entrySet()) {
                    for (Integer edgeID : entry.getValue()) {
                        Edge e = graph.getEdge(edgeID);

                        Node[] nodes = {e.getNode0(), e.getNode1()};

                        // If non of the 2 vertices has a community
                        if (nodes[0].getAttribute("community") == null && nodes[1].getAttribute("community") == null) {
                            communityNum++;
                            nodes[0].setAttribute("community", communityNum);
                            nodes[1].setAttribute("community", communityNum);
                        } else if (nodes[0].getAttribute("community") != null && nodes[1].getAttribute("community") == null) {
                            nodes[1].setAttribute("community", (Integer) nodes[0].getAttribute("community"));
                        } else if (nodes[0].getAttribute("community") == null && nodes[1].getAttribute("community") != null) {
                            nodes[0].setAttribute("community", (Integer) nodes[1].getAttribute("community"));
                        }
                    }
                }
                
                // clear resources
                edgeWeightsMap.clear();
                subEdgeGraph.clear();
            }
        }
                
        // Delete visited attribute
        for (Node n : graph.getEachNode()) {
            n.removeAttribute("visited");
        }
        
        return communityNum;
    }

    public static int BFS(Graph graph) {
        return BFS(graph, new Integer[0]);
    }

    /**
     * Find disjoined communities by BFS. The first community has number 1.
     *
     * @param graph    The graph that we will extract the communities.
     * @param fixedIDs If we want some vertices to be in a custom community,
     *                 then this array should contain they IDs.
     *
     * @return The number of communities detected.
     */
    public static int BFS(Graph graph, Integer[] fixedIDs) {
        int communityNum = 0;

        if (fixedIDs.length > 0) {
            communityNum++;
        }
        for (Integer id : fixedIDs) {
            graph.getNode(id).setAttribute("visited", 1);
            graph.getNode(id).setAttribute("community", communityNum);
        }

        for (Node n : graph.getEachNode()) {
            if (!n.hasAttribute("visited")) {
                n.setAttribute("visited", 1);
                n.setAttribute("community", ++communityNum);

                // Go for BFS
                Iterator<Node> breadth = n.getBreadthFirstIterator();
                while (breadth.hasNext()) {
                    Node next = breadth.next();
                    if (!next.hasAttribute("visited")) {
                        next.setAttribute("visited", 1);
                        next.setAttribute("community", communityNum);
                    }
                }
            }
        }

        // Delete visited attribute
        for (Node n : graph.getEachNode()) {
            n.removeAttribute("visited");
        }

        return communityNum;
    }
}
