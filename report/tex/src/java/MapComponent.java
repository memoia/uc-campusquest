/*
 * MapComponent.java
 *
 * Created on January 8, 2006, 1:50 PM
 */
// <applet code='Main' width='200' height='50'></applet>

package project01;

import javax.swing.*;
import java.awt.*;
import java.util.*;
import com.bruceeckel.swing.*;
import java.lang.*;     // Runtime
import java.io.*;       // IO Streams
import java.net.URL;
//import java.beans.*;    // XMLEncoder/Decoder
//import org.devlib.schmidt.imageinfo.*;        // for getting image dpi
import com.sun.image.codec.jpeg.*;              // for getting image dpi

/**
 * Manages and draws nodes and edges.
 * <p>
 * Note: Original name was "InteractiveMapComponent" so "imc" is found
 * throughout src when using it
 *
 * @author Ian Melnick
 */
public class MapComponent extends JComponent {
    
    // class vars---current coords for drawing, etc etc.
    public NodeManager      nm;
    public MapManager       mm;
    String                  map;            // map name to use/load
    URL                     mapPath;        // location of image
    ImageIcon               mapImage;       // stores the image data
    //JPEGImageDecoder        mapImageInfo;   // contains image resolution in dpi
    Point                   mapCoords;      // current image coordinates relative to top-left corner of screen
    Dimension               mapSize;        // loaded map image dimensions
    LinkedList<NodeItem>    nodes;          // array of nodes
    int                     nodeSize;       // visible node diameter
    int                     nodeId;         // node counter for setting ID numbers
    public int              edgeId;         // edge counter for setting ID numbers
    Runtime                 r;              // gets info such as memory use
    String                  dataDir;        // directory to save/load data to/from
    boolean                 drawInvis;      // draw invisible nodes?
    
           
    public MapComponent(String map) {
        nm          = new NodeManager();
        mm          = new MapManager();
        r           = Runtime.getRuntime();
        this.map    = map;
        nodeSize    = 20;
        drawInvis   = false;
        
        mapPath     = MapComponent.class.getResource("images/map_"+map+".jpg");        // http://java.sun.com/docs/books/tutorial/uiswing/misc/icon.html
        if(mapPath==null) {
            JOptionPane.showMessageDialog(null, "Requested map not found", "Error", JOptionPane.ERROR_MESSAGE);
            System.exit(1);
        }
        
        // System.getenv requires Java 1.5
        if(System.getenv("HOMEDRIVE")==null) {
            dataDir = System.getenv("HOME");
        }
        else {
            dataDir = System.getenv("HOMEDRIVE") + System.getenv("HOMEPATH");
        }
        
        // initMap()
        
        nodes       = new LinkedList<NodeItem>();
        nodeId      = 0;
        edgeId      = 0;
    }
    public void initMap(boolean drawInvisibles) {
        this.drawInvis = drawInvisibles;
        this.initMap();
    }
    public void initMap() {
        // Set up mapImageInfo
        /*try {
            mapImageInfo = JPEGCodec.createJPEGDecoder(mapPath.openStream());
        } catch (IOException err) {
            System.err.println("MapComponent: mapImageInfo: " + err);
        }*/
        
        // Set up mapImage
        mapImage    = new ImageIcon(mapPath);
        
        Errors.debug("Image: " + mapImage);// + " (at: " + mapImageInfo.getJPEGDecodeParam() + ")");
        Errors.debug("Mem used: " + r.totalMemory() + " bytes");
        Errors.debug("Mem max : " + r.maxMemory() +   " bytes");
        Errors.debug("Mem free: " + r.freeMemory() +  " bytes");
        
        mapCoords   = new Point(0, 0);   // -800, -500
        mapSize     = new Dimension(mapImage.getIconHeight(), mapImage.getIconWidth());
    }
    /*public void paintComponent(Graphics g) {
        //super.paintComponent(g);
    }*/
    //note: paintComponent used to be in separate files for each
    //  primary mode window. Since I never changed it for any of
    //  these modes, I've merged it back into MapComponent...
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        mapImage.paintIcon(this, g, (int)this.mapCoords.getX(), (int)this.mapCoords.getY());
                
        // loop thru nodes and draw on map relative to current image position.
        ListIterator<NodeItem> nli, nlj;
        NodeItem tn, un;
        EdgeItem ec;
        Point tp, up;
        Point minCorner = mm.mapMinCorner();
        Point maxCorner = mm.mapMaxCorner();
        
        nli = this.nodes.listIterator(); // For drawing each node
        while(nli.hasNext()) {
            tn = nli.next();

            // Draw/print node if it's within current shown coordinates
            // 1. Get node map position
            // 2. Determine if it's currently visible:
            //    a. Get component size // this.getWidth(), this.getHeight();
            //    b. Based on origin coordinates and window size, are these coords currently visible?
            // 3. Draw oval around point
            
            // Max corner is current map coordinate
            // at origin (this.getCoordinates()) + window size (this.getWidth(), this.getHeight()).
            tp = mm.positiveMapCoords(tn.location());
            
            // If the current node is in between the 1st corner and the max corner then it should be shown,
            // along with its edges (next block)
            if( (tp.getX() <= maxCorner.getX() && tp.getX() > minCorner.getX()) && 
                (tp.getY() <= maxCorner.getY() && tp.getY() > minCorner.getY())) {
                // Draw oval relative to current display coords
                //g.fillOval( ((int)(tpos.getX()-minCorner.getX()-(nodeSize/2))), ((int)(tpos.getY()-minCorner.getY()-(nodeSize/2))), nodeSize, nodeSize );
                if(tn.selected())
                    drawNode(g, Color.red, tn);
                else if(!tn.selected()) {
                    if(tn.visibility())
                        drawNode(g, Color.green, tn);
                    else if(!tn.visibility() && drawInvis)
                        drawNode(g, Color.yellow, tn);
                }
            
                // Find which node(s) are related to this one and draw connecting edges.
                // IAN DRAW DISTANCE FOR LINE HERE ... use ec = sharedEdge(ns, nd)
                g.setColor(Color.black);
                tp = mm.mapCoordsToMouseCoords(tn.location());
                nlj = nm.connectedNodes(tn).listIterator();
                while(nlj.hasNext()) {
                    un = nlj.next();
                    up = mm.mapCoordsToMouseCoords(un.location());
                    ec = nm.sharedEdge(tn, un);
                    g.drawLine((int)tp.getX(), (int)tp.getY(), (int)up.getX(), (int)up.getY());
                    g.drawString(String.valueOf(ec.getDistance()), (int)( ((up.getX()-tp.getX())/2)+tp.getX() ), (int)(((up.getY()-tp.getY())/2)+tp.getY()) );
                }
                
            
            }
        }
    }
    void drawNode(Graphics g, Color c, NodeItem n) {
        g.setColor(c);
        Point minCorner = mm.mapMinCorner();
        Point p = new Point(mm.positiveMapCoords(n.location()));
        g.fillOval( ((int)(p.getX()-minCorner.getX()-(nodeSize/2))), ((int)(p.getY()-minCorner.getY()-(nodeSize/2))), nodeSize, nodeSize );
        g.setColor(Color.black);
        g.drawString(String.valueOf(n.id()), ((int)(p.getX()-minCorner.getX()-(nodeSize/2))), ((int)(p.getY()-minCorner.getY()-(nodeSize/2))));
    }
    
    public Point getCoordinates() { return mapCoords; }
    public Dimension getMapSize() { return mapSize;   }
    public String map()           { return map;       }
    
    /** Return distance in Feet between two nodes */ 
    public int calcDistance(NodeItem a, NodeItem b) {
        // TODO: Both sun and schmidt libraries don't extract dpi, but windows does!
        /*
         * Pixel distance:
         *   x = selNode.location().getX() - relNode.location().getX()
         *   y = selNode.location().getY() - relNode.location().getY()
         * x.toPositive()
         * y.toPositive()
         * 
         * Convert pixels to inches, to feet:
         *   feetwidth = (imc.getMapSize().getWidth() / getMapHorizDPI) * 40
         *   feetheight= (imc.getMapSize().getHeight() / getMapVertDPI) * 40
         */
        Point distancePx = mm.positiveMapCoords(mm.difference(a.location(), b.location()));
        int mapHorizDPI = 96;  // TODO: Needs to be dynamic!
        int mapVertDPI  = 96;
        int mapScale    = 40;  // constant; 1" = 40'
        int feetWidth   = (int)((distancePx.getX() / mapHorizDPI) * mapScale);
        int feetHeight  = (int)((distancePx.getY() / mapVertDPI) * mapScale);
        int distanceFt = (int)Math.sqrt(Math.pow(feetWidth,2)+Math.pow(feetHeight,2));
        return distanceFt;
    }
    
    
    class MapManager {
        public void setOrigin(Point p) {
            // Check that we're still on the map; if yes, redraw
            if( (p.getY() > -mapSize.getHeight() && p.getY() <= 0) && 
                (p.getX() > -mapSize.getWidth() && p.getX() <= 0) ) {
                mapCoords.setLocation(p);
                repaint();
            }
        }
        public void centerAroundPoint(Point p) {
            p = positiveMapCoords(p);
            Point newloc = new Point( -(int)(p.getX()-(getWidth()/2)), -(int)(p.getY()-(getHeight()/2)) );
            setOrigin(newloc);
        }
        public Point mouseCoordsToMapCoords(Point mouseCoords) {
            Point minCorner = mapMinCorner();
            //Point maxCorner = new Point((int)(-this.getCoordinates().getX()+this.getWidth()), (int)(-this.getCoordinates().getY()+this.getHeight()));
            return new Point( (int)(-mouseCoords.getX()-minCorner.getX()), (int)(-mouseCoords.getY()-minCorner.getY()) );
        }
        public Point mapCoordsToMouseCoords(Point mapCoords) {
            Point minCorner = mapMinCorner();
            Point p = new Point(positiveMapCoords(mapCoords));
            return new Point( (int)(p.getX()-minCorner.getX()), (int)(p.getY()-minCorner.getY()) );
        }
        public Point positiveMapCoords(Point negativeMapCoords) {
            return new Point( (int)(Math.abs(negativeMapCoords.getX())), (int)(Math.abs(negativeMapCoords.getY())) );
        }
        public Point mapMinCorner() {
            return new Point(positiveMapCoords(getCoordinates()));
        }
        public Point mapMaxCorner() {
            return new Point((int)(-getCoordinates().getX()+getWidth()), (int)(-getCoordinates().getY()+getHeight()));
        }
        public void offsetByMouse(Point mcp, Point mrp) {
            Point oc = new Point((int)(mrp.getX()-mcp.getX()), (int)(mrp.getY()-mcp.getY()));
            Point mc = new Point((int)(getCoordinates().getX()+oc.getX()), (int)(getCoordinates().getY()+oc.getY()));
            setOrigin(mc);
        }
        public Point difference(Point a, Point b) {
            return new Point((int)(a.getX()-b.getX()),(int)(a.getY()-b.getY()));
        }
    }
    
    class NodeManager {
        public void addNode(Point p) {
            nodes.add(new NodeItem(map, p, ++nodeId));
            repaint();
        }
        public void delNode(NodeItem n) {
            NodeItem c;
            EdgeItem ei;
            ListIterator<NodeItem> l = connectedNodes(n).listIterator();
            Iterator<EdgeItem> e;
            
            while(l.hasNext()) {
                c = l.next();
                e = n.edges().iterator();
                while(e.hasNext()) {
                    try {
                        ei = e.next();
                        if(c.hasEdge(ei))
                            c.delEdge(ei);
                    } catch (java.util.ConcurrentModificationException er) {
                        //continue;
                        break;
                    }
                }
            }
            nodes.remove(n);
            repaint();
        }
        public NodeItem getNodeAtCoords(Point p) {
            p = mm.positiveMapCoords(mm.mouseCoordsToMapCoords(p));
            ListIterator<NodeItem> nli = nodes.listIterator();
            Point m;
            Object tobj;
            NodeItem tnod, rnod;
            rnod = null;
            while(nli.hasNext()) {
                tnod = nli.next();
                m = mm.positiveMapCoords(tnod.location());
                
                // If p is within tnod.location() and tnod.location()+nodeSize
                // Make sure that coords being compared are relative to screen, like passed mouse coords...
                if( (p.getX() >= m.getX()-(nodeSize) && p.getX() < m.getX()+(nodeSize)) &&
                    (p.getY() >= m.getY()-(nodeSize) && p.getY() < m.getY()+(nodeSize))) {
                    rnod = tnod;
                    break;
                }
                else
                    rnod = null;
            }
            return rnod;
        }
        public NodeItem getNodeById(int nid) {
            NodeItem n;
            ListIterator<NodeItem> nli = nodes.listIterator();
            while(nli.hasNext()) {
                n = nli.next();
                if(n.id()==nid)
                    return n;
            }
            return null;
        }
        public boolean connected(NodeItem ns, NodeItem nd) {
            if(connectedNodes(ns).contains(nd))
                return true;
            else
                return false;
        }
        public LinkedList<NodeItem> connectedNodes(NodeItem n) {
            /* Return a LinkedList of nodes connected to n by common edges */
            // NOTE: Using Generics, no longer need to cast objects when retrieving from List
            ListIterator<NodeItem> l;
            Iterator<EdgeItem> e;
            NodeItem c;
            LinkedList<NodeItem> r = new LinkedList<NodeItem>();
            LinkedList<NodeItem> tnodes = new LinkedList<NodeItem>(nodes);
            tnodes.remove(n);
            
            l = tnodes.listIterator(); // For finding which node contains edge relationship
            while(l.hasNext()) {
                c = l.next();
                e = n.edges().iterator();
                while(e.hasNext())
                    if(c.hasEdge( e.next() ))
                        r.add(c);
            }
        
            return r;
        }
        public EdgeItem sharedEdge(NodeItem ns, NodeItem nd) {
            EdgeItem ei;
            Iterator<EdgeItem> el = ns.edges().iterator();
            while(el.hasNext()) {
                ei = el.next();
                try {
                    if(nd.hasEdge(ei))
                        return ei;
                } catch(NullPointerException err) {
                    continue;
                }
            }
            return null;
        }
        /** Generates a list of visible nodes for use in a JComboBox */
        public Vector<MapItem> nodeVector() {
            Vector<MapItem> nodeVector = new Vector<MapItem>();
            NodeItem n;
            ListIterator<NodeItem> nli = nodes.listIterator();
            //nodeVector.add(new MapItem(-1, map()));
            while(nli.hasNext()) {
                n = nli.next();
                if(n.visibility()) {    
                    nodeVector.add(n);
                }
            }
            return nodeVector;
        }
        public void saveNodes() {
            /* Save the node list to corresponding map data file */
            
            /*try {
                XMLEncoder e = new XMLEncoder(new BufferedOutputStream(
                    new FileOutputStream("/tmp/dat_"+this.map+".xml")));
                e.writeObject(this.nodes); e.close();
            } catch (Exception er) {
                System.out.println(er);
            }*/
            try {
                FileOutputStream out = new FileOutputStream(dataDir + "/map_"+map+".dat");
                ObjectOutputStream s = new ObjectOutputStream(out);
                s.writeObject(String.valueOf(nodeId));
                s.writeObject(String.valueOf(edgeId));
                s.writeObject(nodes);
                s.flush();
                out.close();
            } catch (Exception e) {
                Errors.debug(e);
            }
        }
        @SuppressWarnings("unchecked")      // need this to avoid readObject type warning which can't be fixed
        public void loadNodes() {
            /* Load the node list from corresponding map data file */
            try {
                FileInputStream in = new FileInputStream(dataDir + "/map_"+map+".dat");
                ObjectInputStream s = new ObjectInputStream(in);
                nodeId= Integer.parseInt((String)s.readObject());
                edgeId= Integer.parseInt((String)s.readObject());
                nodes = (LinkedList<NodeItem>)s.readObject();
            } catch (Exception e) {
                Errors.debug(e);
            }
        }
    }
    
    
}
