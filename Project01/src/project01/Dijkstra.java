/*
 * Dijkstra.java
 *
 * // TODO: URGENT. The reset() method likely will not reset ALL touched nodes.
 * // Should create a new Set to contain ALL touched nodes throughout process
 * // to ensure everything gets reset on request.
 *
 * 
 * This is where the shortest path algorithm goes to work.
 *
 * Created on January 28, 2006, 7:14 PM
 *
 * http://carbon.cudenver.edu/~hgreenbe/sessions/dijkstra/DijkstraApplet.html
 *   used as influence, but no source used.
 *
 * === Semi-Pseudo ============================
 * setConnectedDistances(node):
 *    For each n connected to node, set n's distance values and n's source info
 *    Return the list of connected nodes
 * closestNodeInT():
 *    Return the node in T with the lowest distance value (that's not in P)
 *
 * given start
 * given destination
 * P = {start}              // permanent labels
 * T = {}                   // temporary labels
 * next(node) {
 *   T.addAll(setConnectedDistances(node));
 *   n = closestNodeInT();
 *   T.remove(n);
 *   P.add(n);
 *   if(!p.contains(destination))
 *     next(n);
 * }
 */


package project01;

import java.util.*;
import java.util.concurrent.CopyOnWriteArraySet;


/**
 *
 * @author Ian Melnick
 */
public class Dijkstra {
    private static final boolean DEBUG = false;
    private MapComponent.NodeManager nm;
    private NodeItem src, dest;
    private LinkedList<NodeItem> p;
    private CopyOnWriteArraySet<NodeItem> t; // avoids duplicates and potential concurrency problems
    private Dijkstra intermediate;
    
    /** Creates a new instance of Dijkstra */
    public Dijkstra(MapComponent.NodeManager nm, NodeItem src, NodeItem dest) {
        this.nm     = nm;                                   // node manager
        this.src    = src;                                  // source node
        this.dest   = dest;                                 // destination node
        this.p      = new LinkedList<NodeItem>();           // permanently labeled nodes
        this.t      = new CopyOnWriteArraySet<NodeItem>();  // temporarily labeled nodes
        this.intermediate = null;                           // use this for multi-floor searches
        
        this.src.setDistToMe(0);
        this.p.add(src);
    }
    
    /** Returns true if source and destination nodes are on different floors */
    private boolean multiFloor() {
        return (src.map().compareTo(dest.map())!=0);
    }
    
    /** Returns the LinkedList<NodeItem> as a string */
    private String listToString(Collection<NodeItem> L) {
        Iterator<NodeItem> I = L.iterator();
        String ret = "{";
        while(I.hasNext()) {
            ret += (I.next().id());
            if(I.hasNext())
                 ret += ",";
        }
        return ret += "}";
    }
    
    /** Return the closest node in T that's not in P */
    private NodeItem closestNodeInT() {
        Iterator<NodeItem> i = t.iterator();
        NodeItem candidate = null;
        NodeItem next = null;
        
        try {
            
            // Set candidate to the first node in T not in P
            do {
                candidate = i.next();
            } while (i.hasNext() && p.contains(candidate));
            
        } catch(NoSuchElementException err) {
            if(nearestFloorLink(dest.map())==null)
                Errors.debug("No path exists from " + src + " to " + dest);
            
            p.add(dest);
            return dest;
            //System.exit(1);
        }
        
        // If anyone else in T has a shorter dist, reset candidate
        while(i.hasNext()) {
            next = i.next();
            if(next.getDistToMe()<candidate.getDistToMe() && !p.contains(next))
                candidate = next;
        }
        
        return candidate;
    }
    
    /**
     * Set the Dijkstra-specific distance information on all nodes
     * connected to n, and return the list containing all these connected nodes.
     */
    private LinkedList<NodeItem> setConnectedDistances(NodeItem node) {
        int d;                          // store node distance value
        NodeItem n;                     // temp node
        EdgeItem e;                     // temp edge
        LinkedList<NodeItem> c;         // list for nodes to set
        ListIterator<NodeItem> i;       // iterates over t

        c = nm.connectedNodes(node);
        
        //if(DEBUG) System.err.println("Next nodes: " + c);
        i = c.listIterator();
        while(i.hasNext()) {
            n = i.next();
            e = nm.sharedEdge(node,n);
            
            d = node.getDistToMe() + e.getDistance();
            
            /*
             * If node's distance hasn't been set yet, OR if the current way
             * is shorter than what it had set before, then set the Dijkstra
             * info in this node to the current best option.
             */
            if(n.getDistToMe() < 0 || d < n.getDistToMe()) {
                n.setDistToMe(d);
                n.setSrcEdge(e);
                n.setSrcNodeId(node.id());
            }
        }
        
        return c;
    }
    
    /**
     * Work through nodes and set/update their labels,
     * adding to P when we're sure.
     */
    private void updateLabels(NodeItem node) {
        if(DEBUG) Errors.debug("src:"+src+", dest:"+dest);
        if(DEBUG) Errors.debug("P contains: " + listToString(p));
        NodeItem n = null;
        t.addAll(setConnectedDistances(node));
        t.removeAll(p);     // remove nodes already in p
        if(DEBUG) Errors.debug("T contains: " + listToString(t));
        n = closestNodeInT();
        p.add(n);
        //while(t.remove(n)) continue;
        t.remove(n);
        if(!p.contains(dest))
            updateLabels(n);
    }
    
    /**
     * Return route list, ordered from start to destination.
     * Note that to generate the list we start from the destination and
     * work our way backwards, so if there's a path inconsistency, the
     * returned list will indicate that the user should start at the
     * destination.
     */
    public LinkedList<NodeItem> getRoute() {
        /* Pull nodes from P starting from end, trace back by edges.
         * Loop:
         *   while src not in r,        (contains())
         *     myDistNum = lastDistNum - lastEdgeLength
         *     myNode = look for node in P which has myDistNum
         *     r.addFirst(myNode)
         */
        LinkedList<NodeItem> r = new LinkedList<NodeItem>();
        r.add(this.dest);
        
        int distNum;
        NodeItem ln, tn;
        
        // Single floor---normal op
        while(!r.contains(src)) {
            ln = r.getFirst();
            //distNum = (ln.getDistToMe()) - (ln.getSrcEdge().getDistance());
            tn = nm.getNodeById(ln.getSrcNodeId());
            if(tn!=null)
                r.addFirst(tn);
            else {
                Errors.debug("no source for node " + ln);
                break;
            }
        }
        
        // If intermediate exists, must add in intermediate's route first
        if(intermediate!=null)
            r.addAll(intermediate.getRoute());
                
        return r;
    }
    
    public LinkedList<NodeItem> getP() { return this.p; }
    
    public void run() {
        
        // First check if source and destination floors are different
        // If they aren't the same, get route details from floor link
        // to destination, which will be used later when returning the
        // correct route.
        if(multiFloor()) {
            NodeItem localLink = nearestFloorLink(dest.map());
            NodeItem otherLink = localLink.floorLink(dest.map());
            MapComponent intmc = new MapComponent(dest.map());
            intmc.nm.loadNodes();
            NodeItem otherSrc  = intmc.nm.getNodeById(otherLink.id());
            NodeItem otherDest = intmc.nm.getNodeById(dest.id());
            intermediate = new Dijkstra(intmc.nm, otherSrc, otherDest);
            intermediate.run();
            dest = localLink;
        }
        
        // Then run normal algorithm
        updateLabels(src);
        
    }
        
    public void reset() {
        NodeItem tn;
        ListIterator<NodeItem> tI;
        tI = p.listIterator();
        while(tI.hasNext()) {
            (tI.next()).resetDijkstraValues();
        }
    }
    
    
    /* FLOOR LINK SEARCHING */
    
    /**
     * Returns true if link either directly links to destMap
     * or links to another floor which ultimately links to destMap.
     */
    private boolean linksToFloor(NodeItem link, String destMap) {
        if(!link.isFloorLink())
            return false;
        if(link.hasFloorLink(destMap))
            return true;
        else {
            Set<String> floors = link.connectedFloors();
            Iterator<String> f = floors.iterator();
            while(f.hasNext())
                return linksToFloor(link.floorLink(f.next()), destMap);
        }
        return false;
    }
    
    /**
     * Returns the closest floor linking node
     * from the source node that can provide a connection
     * path to the destination's floor (i.e., through other
     * floor links).
     */
    private NodeItem nearestFloorLink(String destMap) {
        LinkedList<NodeItem> nodesLookedAt = new LinkedList<NodeItem>();
        return nearestFloorLink(src, destMap, nodesLookedAt);
    }
    private NodeItem nearestFloorLink(NodeItem source, String destMap, LinkedList<NodeItem> nodesLookedAt) {
        if(linksToFloor(source, destMap))
            return source;
        
        nodesLookedAt.add(source);
        Iterator<NodeItem> i = nm.connectedNodes(source).iterator();
        NodeItem n;
        while(i.hasNext()) {
            n = i.next();
            if(!nodesLookedAt.contains(n)) {
                nodesLookedAt.add(n);
                return nearestFloorLink(n, destMap, nodesLookedAt);
            }
        }
        return null;
    }
    
}
