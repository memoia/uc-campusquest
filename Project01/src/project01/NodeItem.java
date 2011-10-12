/*
 * NodeItem.java
 *
 * Created on October 29, 2005, 3:30 PM
 */

package project01;

import javax.swing.*;
import javax.swing.event.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.io.Serializable;


/** 
 * Describes a node on a map.
 *
 * @author Ian Melnick
 */
public class NodeItem extends MapItem implements Serializable {
    /** Coordinates that this NodeItem is located at. */
    private Point location;
    
    /** Whether or not this node is currently selected (i.e., by the user). */
    private boolean selected;
    
    /** Whether or not this node is visible to the user (invisible if corner).*/
    private boolean visible;
    
    /** Dijkstra value: this node's distance from the starting point. */
    private int distanceToMe;
    
    /** Dijkstra value: node ID of node directly before this one on path. */
    private int sourceNode;
    
    /** Holds list of EdgeItems this node is connected to. */
    private Set<EdgeItem> edges;
    
    /**
     * Dijkstra value: edge that provides overall shortest route that this is
     * connected to.
     */
    private EdgeItem sourceEdge;
    
    /**
     * Stores nodes that this node is connected to by elevators/stairs.
     * String map name, NodeItem the node. Zero length if this node isn't
     * a floor link.
     */
    private Map<String,NodeItem> floorLink;
    
    
    /**
     * Create a new NodeItem.
     */
    public NodeItem(String map, Point p, int id) {
        this.map = map;
        this.location = p;
        this.visible = true;
        this.selected = false;
        this.id = id;
        this.description = "n"+this.id+" ("+this.location.getX()+", "+this.location.getY()+")";
        this.edges = new HashSet<EdgeItem>();
        this.floorLink = new HashMap<String,NodeItem>();
        this.resetDijkstraValues();
    }
        
    /** Get: This node's coordinates. */
    public Point location()             { return this.location;             }
    
    /** Set: This node's coordinates. */
    public void setLocation(Point p)    { this.location = p;                }
    
    /** Get: Whether or not this node is selected (i.e., by user). */
    public boolean selected()           { return this.selected;             }
    
    /** Set: Toggle whether node is selected. */
    public void select()                { this.selected = !this.selected;   }
    
    /** Get: Whether or not this node is visible to user. */
    public boolean visibility()         { return this.visible;              }
    
    /** Set: Toggle visibility. */
    public void visible()               { this.visible = !this.visible;     }
    
    /** Get: List of EdgeItems this node is connected to. */
    public Set<EdgeItem> edges()        { return this.edges;                }
    
    /** Get: Dijkstra value: return distance from start node. */
    public int getDistToMe()            { return this.distanceToMe;         }
    
    /** Set: Dijkstra value: set distance from start node to this. */
    public void setDistToMe(int dist)   { this.distanceToMe = dist;         }
    
    /**
     * Set: Dijkstra value: edge that we follow to get to this node along
     * the shortest path.
     */
    public void setSrcEdge(EdgeItem e)  { this.sourceEdge = e;              }
    
    /**
     * Get: Dijkstra value: edge that we follow to get to this node along
     * the shortest path.
     */
    public EdgeItem getSrcEdge()        { return this.sourceEdge;           }
    
    /**
     * Set: Dijkstra value: node ID that is right before us along the
     * shortest path.
     */
    public void setSrcNodeId(int id)    { this.sourceNode = id;             }
    
    /**
     * Get: Dijkstra value: node ID that is right before us along the
     * shortest path.
     */
    public int getSrcNodeId()           { return this.sourceNode;           }
    
    /** Resets the Dijkstra values to ready for a new shortest-path run. */
    public void resetDijkstraValues() {
        setDistToMe(-1);
        setSrcNodeId(-1);
        setSrcEdge(null);
    }
    
    /** Whether or not this node is connected to a specific EdgeItem. */
    public boolean hasEdge(EdgeItem ei) {
        if(edges.contains(ei))  return true;
        else                    return false;
    }
    
    /** Connect an EdgeItem to this node if node doesn't already have it. */
    public void addEdge(EdgeItem ei) {
        edges.add(ei);
    }
    
    /** Disconnect an EdgeItem from this node. */
    public void delEdge(EdgeItem ei) {
        edges.remove(ei);
    }
    
    /** Whether or not this node links to other floors */
    public boolean isFloorLink() {
        return !floorLink.isEmpty();
    }
    
    /** Whether or not this node has a link to specified floor */
    public boolean hasFloorLink(String mapName) {
        return floorLink.containsKey(mapName);
    }
    
    /** Add a floor/node to list of linked floors */
    public void addFloorLink(String mapName, NodeItem linkedNode) {
        if(!floorLink.containsKey(mapName))
            floorLink.put(mapName, linkedNode);
    }
    
    /** Remove a floor/node from list of linked floors */
    public void delFloorLink(String mapName) {
        floorLink.remove(mapName);
    }
    
    /** Get list of floors this node connects to */
    public Set<String> connectedFloors() {
        return floorLink.keySet();
    }
    
    /** Based on a floor, return the connected node */
    public NodeItem floorLink(String mapName) {
        return floorLink.get(mapName);
    }
}
