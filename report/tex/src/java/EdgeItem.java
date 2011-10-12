/*
 * EdgeItem.java
 *
 * Created on December 12, 2005, 5:04 PM
 */

package project01;


import javax.swing.*;
import javax.swing.event.*;
import java.awt.*;
import java.awt.event.*;
import java.io.Serializable;


/**
 * Describes an edge on a map. 
 *
 * @author Ian Melnick
 */
public class EdgeItem extends MapItem implements Serializable {
    private int distance;       // distance in "paces"
    
    /** Creates a new instance of EdgeItem */
    public EdgeItem(String map, int id) {
        this.map        = map;
        this.id         = id;
        this.distance   = 0;
    }
    
    public int getDistance()            { return this.distance;             }
    public void setDistance(int paces)  { this.distance = paces;            }
}
