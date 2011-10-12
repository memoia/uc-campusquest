/*
 * FloorLinker.java
 *
 * Created on February 7, 2006, 2:11 PM
 */

package project01;

import javax.swing.*;
import javax.swing.event.*;
import java.awt.*;
import java.awt.event.*;
import java.applet.*;
import java.util.*;


/**
 * A small map window that allows the user to select a floor-link node.
 * Intended to be launched from Editor mode, in NodeEditor in the Floors tab,
 * when user selects "Add New Link". This is not a primary mode window,
 * and therefore is not intended to be run on application launch. Cannot be
 * run as an applet.
 * 
 * @author Ian Melnick
 */
public class FloorLinker {
    private JFrame window;
    private MapComponent imc;
    private NodeItem source, destination;
    private String srcMap, destMap;
    
    public FloorLinker(String srcMap, String destMap, NodeItem source, Point location) {
        this.source = source;
        this.destination = null;
        this.srcMap = srcMap;
        this.destMap = destMap;
        
        window = new JFrame("Select corresponding node on " + destMap);
        window.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        window.setSize(500, 350);
        
        imc = new MapComponent(destMap);
        imc.nm.loadNodes();
        imc.initMap();
        imc.mm.centerAroundPoint(location);
        
        MouseMethods mm  = new MouseMethods();
        imc.addMouseListener(mm);
        imc.addMouseMotionListener(mm);
        imc.addMouseWheelListener(mm);
        
        Container cp = window.getContentPane();
        cp.setLayout(new BorderLayout());
        cp.add(imc);
        
        window.setVisible(true);
    }
    
    /**
     * Deletes nodeId in destMap from having a floor connection to srcMap.
     */
    public static void deleteConnection(String srcMap, String destMap, int nodeId) {
        MapComponent otherFloor = new MapComponent(destMap);
        otherFloor.nm.loadNodes();
        NodeItem next = otherFloor.nm.getNodeById(nodeId);
        next.delFloorLink(srcMap);
        otherFloor.nm.saveNodes();
    }
    
    /**
     * Listener methods for mouse actions 
     */
    private class MouseMethods extends MouseInputAdapter implements MouseWheelListener {
        private Point mouseClickPoint, mouseReleasePoint, newNode;
        private boolean mouseDragged, scrollDirection;              // scroll direction = false, use y, true use x
        public void mouseMoved(MouseEvent e) {
        }
        public void mouseClicked(MouseEvent e) {
            if((e.getButton()==1 || e.getButton()==3) && e.getClickCount()==2) {
                // Check if it was pressed over a visible node
                // Loop thru nodes and verify if mouse coords are within boundaries of any of the visible sections.
                //System.out.println("Pressed at: " + e.getPoint());
                
                // If double-click over a node, edit it
                destination = imc.nm.getNodeAtCoords(e.getPoint());
                
                // Set up the links between nodes
                try {
                    destination.addFloorLink(srcMap, source);
                    source.addFloorLink(destMap, destination);
                    imc.nm.saveNodes();
                } catch (NullPointerException err) {
                    // No node double-clicked...
                }
                window.dispose();
            }
        }
        public void mousePressed(MouseEvent e) {
            this.mouseClickPoint = e.getPoint();
            if(e.getButton()==2)
                this.scrollDirection = !this.scrollDirection;
        }
        public void mouseDragged(MouseEvent e) {
            this.mouseDragged = true;
        }
        public void mouseReleased(MouseEvent e) {
            this.mouseReleasePoint = e.getPoint();
            if(this.mouseDragged && !this.mouseReleasePoint.equals(this.mouseClickPoint)) {
                // If dragged over node, create edge
                // If dragged over nothing, move node
                if(e.getButton()==1 || e.getButton()==3) {
                    NodeItem selNode = imc.nm.getNodeAtCoords(this.mouseClickPoint);
                    NodeItem relNode = imc.nm.getNodeAtCoords(this.mouseReleasePoint);
                    try {
                        if(relNode!=null && !imc.nm.connected(selNode, relNode)) {
                            EdgeItem edge = new EdgeItem(imc.map(), ++imc.edgeId);
                            selNode.addEdge(edge);
                            relNode.addEdge(edge);
                        }
                        else // Move node
                            selNode.setLocation(imc.mm.mouseCoordsToMapCoords(this.mouseReleasePoint));
                        imc.repaint();
                    } catch (java.lang.NullPointerException error) {
                        // move map when no node available
                        imc.mm.offsetByMouse(this.mouseClickPoint, this.mouseReleasePoint);
                        //sPanY.setValue((int)imc.getCoordinates().getY());
                        //sPanX.setValue((int)imc.getCoordinates().getX());
                    }
                }
                
                this.mouseDragged=false;
            }
        }
        public void mouseWheelMoved(MouseWheelEvent e) {
            Point newCoords = new Point();
            if(this.scrollDirection) {
                newCoords.setLocation( imc.getCoordinates().getX()-(e.getWheelRotation()*50), imc.getCoordinates().getY() );
                //sPanX.setValue((int)newCoords.getX());
            }
            else {
                newCoords.setLocation( imc.getCoordinates().getX(), imc.getCoordinates().getY()-(e.getWheelRotation()*50) );
                //sPanY.setValue((int)newCoords.getY());
            }
            imc.mm.setOrigin(newCoords);
        }
    }
        
    
}
