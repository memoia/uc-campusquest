/*
 * project01: Description
 * Ian Melnick (dazed)
 * March 14, 2006, 4:04 PM
 * Course, Prof
 *
 * MapItem.java
 */

package project01;
import java.io.Serializable;


/**
 * Describes an item to be placed on a map.
 * 
 * @author Ian Melnick
 */
public class MapItem implements Serializable {
    /** This node's unique identification number. */
    protected int id;
    
    /** Describes this node, i.e., room number. */
    protected String description;
    
    /** The name of the map that this item is a member of. */
    protected String map;
    
    
    /** Creates a new instance of MapItem */
    public MapItem()                    { this.id = -1;                 }
    public MapItem(int id)              { this.id = id;                 }
    public MapItem(int id, String map)  { this(id); this.map = map; }
    public MapItem(int id, String map, String d) { this(id,map); this.description = d; }
   
    
    public int id()                     { return this.id;               }
    public void setId(int id)           { this.id = id;                 }
    
    /** Set: This node's string representation. */
    public void setDescription(String d){ this.description = d;         }
    
    /** Get: This node's description. */
    public String description()         { return this.description;      }
    
    public void setMap(String m)        { this.map = m;                 }
    public String map()                 { return this.map;              }
    
    /** Get: This node's string representation. */
    public String toString()            { return /*map+": "+*/description;  }
}
