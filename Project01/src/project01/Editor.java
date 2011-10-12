/*
 * Editor.java
 *
 * Created on September 14, 2005, 11:27 PM
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */
// <applet code='Editor' width='200' height='50'></applet>

package project01;

import javax.swing.*;
import javax.swing.event.*;
import java.awt.*;
import java.awt.event.*;
import java.applet.*;
import java.util.*;
//import com.bruceeckel.swing.*;
//import java.io.*;


/**
 * Sets up the environment and runs the Map Data Editor program;
 * contains listener methods that work with the {@link MapComponent}.
 * 
 * @author Ian Melnick
 */
public class Editor extends Main {
    
    private JButton bLoad, bSave;
    private JSlider sPanX, sPanY;
    private JPanel bottom, bottomL, bottomC, bottomR;
    private JLabel status, message;
    private MapComponent imc;
    
    public Editor(Args a) {
        //a       = Utility.getArgs(args, this);
        this.a  = a;
        
        bLoad   = new JButton("Load");
        bSave   = new JButton("Save");
        imc     = new MapComponent(a.map);
        imc.initMap(true);
        sPanX   = new JSlider(JSlider.HORIZONTAL, -(int)imc.getMapSize().getWidth(), 0, (int)imc.getCoordinates().getX());
        sPanY   = new JSlider(JSlider.VERTICAL, -(int)imc.getMapSize().getHeight(), 0, (int)imc.getCoordinates().getY());
        bottom  = new JPanel(new GridLayout(1,3));
        bottomL = new JPanel(new FlowLayout());
        bottomC = new JPanel(new FlowLayout());
        bottomR = new JPanel(new FlowLayout());
        status  = new JLabel(" ");
        message = new JLabel(" ");
    }
    
    
    /**
     * Listener methods for mouse actions 
     */
    private class MouseMethods extends MouseInputAdapter implements MouseWheelListener {
        private Point mouseClickPoint, mouseReleasePoint, newNode;
        private boolean mouseDragged, scrollDirection;              // scroll direction = false, use y, true use x
        public void mouseMoved(MouseEvent e) {
            status.setText("map: " + a.map + "      dc: (" + 
                    (int)e.getPoint().getX()+","+(int)e.getPoint().getY() +
                    ")" + "  mc: (" + 
                    (int)imc.mm.mouseCoordsToMapCoords(e.getPoint()).getX()+","+
                    (int)imc.mm.mouseCoordsToMapCoords(e.getPoint()).getY() + ")"
            );
        }
        public void mouseClicked(MouseEvent e) {
            if((e.getButton()==1 || e.getButton()==3) && e.getClickCount()==2) {
                // Check if it was pressed over a visible node
                // Loop thru nodes and verify if mouse coords are within boundaries of any of the visible sections.
                //System.out.println("Pressed at: " + e.getPoint());
                
                // If double-click over a node, edit it
                // If no node, create node at click point
                NodeItem selNode = imc.nm.getNodeAtCoords(e.getPoint());
                try {
                    //System.out.println("Tapped node: " + selNode.location());
                    selNode.select();
                    imc.repaint();
                    if(selNode.selected()) {
                        NodeEditor npm = new NodeEditor(selNode, imc);
                    }
                } catch (java.lang.NullPointerException error) {
                    //System.out.println("The third button is reserved for node operations.");
                    // Create node at specified point.
                    // Point needs to be offset from the current window coords so it's relative to map instead
                    newNode = new Point(
                        (int)(imc.getCoordinates().getX() - e.getPoint().getX()),
                        (int)(imc.getCoordinates().getY() - e.getPoint().getY())
                    );
                    setMessage("Created Node");
                    imc.nm.addNode(newNode);
                }
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
                    EdgeItem edge = null;
                    try {
                        if(relNode!=null && !imc.nm.connected(selNode, relNode)) {
                            edge = new EdgeItem(imc.map(), ++imc.edgeId);
                            edge.setDistance(imc.calcDistance(selNode,relNode));
                            selNode.addEdge(edge);
                            relNode.addEdge(edge);
                            setMessage("Created Edge " + edge.id());
                        }
                        else { // Move node
                            selNode.setLocation(imc.mm.mouseCoordsToMapCoords(this.mouseReleasePoint));
                            NodeItem otherNode = null;
                            ListIterator<NodeItem> i = imc.nm.connectedNodes(selNode).listIterator();
                            while(i.hasNext()) {
                                otherNode = i.next();
                                edge = imc.nm.sharedEdge(otherNode, selNode);
                                edge.setDistance(imc.calcDistance(otherNode, selNode));
                            }
                        }
                        imc.repaint();
                    } catch (java.lang.NullPointerException error) {
                        // move map when no node available
                        imc.mm.offsetByMouse(this.mouseClickPoint, this.mouseReleasePoint);
                        sPanY.setValue((int)imc.getCoordinates().getY());
                        sPanX.setValue((int)imc.getCoordinates().getX());
                    }
                }
                
                this.mouseDragged=false;
            }
        }
        public void mouseWheelMoved(MouseWheelEvent e) {
            Point newCoords = new Point();
            if(this.scrollDirection) {
                newCoords.setLocation( imc.getCoordinates().getX()-(e.getWheelRotation()*50), imc.getCoordinates().getY() );
                sPanX.setValue((int)newCoords.getX());
            }
            else {
                newCoords.setLocation( imc.getCoordinates().getX(), imc.getCoordinates().getY()-(e.getWheelRotation()*50) );
                sPanY.setValue((int)newCoords.getY());
            }
            imc.mm.setOrigin(newCoords);
        }
    }
    
    /**
     * Listener methods for button actions
     */
    private class ButtonMethods implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            if(e.getSource() == bSave) {
                imc.nm.saveNodes();
                setMessage("Saved");
            }
            if(e.getSource() == bLoad) {
                imc.nm.loadNodes();
                imc.repaint();
                setMessage("Loaded");
            }
        }
    }
    
    /**
     * Listener methods for slider actions
     */
    private class SliderMethods implements ChangeListener {
        public void stateChanged(ChangeEvent e) {
            JSlider s = (JSlider)e.getSource();
            Point newCoords = new Point();
            if(!s.getValueIsAdjusting()) {
                if(e.getSource()==sPanY)
                    newCoords.setLocation( imc.getCoordinates().getX(), s.getValue() );
                else if(e.getSource()==sPanX)
                    newCoords.setLocation( s.getValue(), imc.getCoordinates().getY() );
                imc.mm.setOrigin(newCoords);
            }
        }
    }
    
    
    public void setMessage(String m) {
        message.setText(m);
    }
    
    
    public String getAppletInfo() {
        return "(c) Ian Melnick, Union College 2005-2006";
    }
    public String[][] getParameterInfo() {
        String pinfo[][] = {
            {"m","String","Map file to load (specify location/floor only, i.e., map_scieneg_000.jpg is loaded with arg scieng_000)"}
            /*{"w","Integer","Width of map window (px)"},
            {"h","Integer","Height of map window (px)"}*/
        };
        return pinfo;
    }
    
    
    
    public void init() {
        MouseMethods mm  = new MouseMethods();
        ButtonMethods bm = new ButtonMethods();
        SliderMethods sm = new SliderMethods();
        
        bLoad.addActionListener(bm);
        bSave.addActionListener(bm);
        sPanX.addChangeListener(sm);
        sPanY.addChangeListener(sm);
        imc.addMouseListener(mm);
        imc.addMouseMotionListener(mm);
        imc.addMouseWheelListener(mm);
        
        bottomL.add(bLoad);
        bottomL.add(bSave);
        bottomC.add(status);
        bottomR.add(message);
        bottom.add(bottomL);
        bottom.add(bottomC);
        bottom.add(bottomR);
        
        Container cp = getContentPane();
        cp.setLayout(new BorderLayout());
        cp.add(sPanX, BorderLayout.NORTH);
        cp.add(sPanY, BorderLayout.WEST);
        cp.add(bottom, BorderLayout.SOUTH);
        cp.add(imc);
        
        //System.out.println(this.getSize());
    }
    
}
