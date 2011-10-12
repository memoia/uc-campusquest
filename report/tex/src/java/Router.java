/*
 * Router.java
 *
 * Created on January 8, 2006, 1:45 PM
 */
// <applet code='Router' width='200' height='50'></applet>

package project01;

import javax.swing.*;
import javax.swing.event.*;
import java.awt.*;
import java.awt.event.*;
import java.applet.*;
//import com.bruceeckel.swing.*;
//import java.io.*;
import java.util.*;
import project01.DirectionsFrame;


/**
 * Sets up the environment and runs the Map Shortest Path Route program;
 * contains listener methods that work with the {@link MapComponent}.
 * 
 * @author Ian Melnick
 */
public class Router extends Main {
    
    private JComboBox cStart, cEnd;
    //private JSlider sPanX, sPanY;
    private JPanel bottom, bottomL, bottomC, bottomR;
    private JLabel status;    
    private MapComponent imc;
    private Vector<MapItem> nodes;
    private Container cp;
    private ButtonMethods bm;
    private MouseMethods mm;
    
    
    public Router(Args a) {
        this.a  = a;
        setMap(a.map);
        setNodeList();
    }
    
    public void setMap(String mapName) {
        imc = new MapComponent(mapName);
        imc.initMap();
        imc.nm.loadNodes();
    }
    public String getMap() {
        return imc.map();
    }
    public MapComponent.MapManager getMapManager() {
        return this.imc.mm;
    }
    
    private void setNodeList() {
        nodes = new Vector<MapItem>(1);
        nodes.add(new MapItem(-1, imc.map(), "---- "+imc.map()+" ----"));
        nodes.addAll(imc.nm.nodeVector());
        // Open the imc's of all maps and append their nodes to this vector
        MapComponent j;
        for(String mapName : Utility.map_images(null)) {
            mapName = mapName.split("map_")[1];
            mapName = mapName.substring(0, mapName.length()-4);
            if(mapName.compareTo(imc.map())!=0) {
                nodes.add(new MapItem(-1, mapName, "---- "+mapName+" ----"));
                j = new MapComponent(mapName);
                j.nm.loadNodes();
                nodes.addAll(j.nm.nodeVector());
            }
        }
        j = null;
    }
    private void showDirections(Dijkstra d) {
        new DirectionsFrame(this, d);
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
                NodeItem selNode = imc.nm.getNodeAtCoords(e.getPoint());
                try {
                    if(selNode==null ||
                            (cStart.getSelectedItem().getClass()==NodeItem.class &&
                            cEnd.getSelectedItem().getClass()==NodeItem.class)) {
                        cStart.setSelectedItem(nodes.get(0));
                        cEnd.setSelectedItem(nodes.get(0));
                    }
                    else if(cStart.getSelectedItem().getClass()!=NodeItem.class)
                        cStart.setSelectedItem(selNode);
                    else if(cEnd.getSelectedItem().getClass()!=NodeItem.class)
                        cEnd.setSelectedItem(selNode);
                } catch (java.lang.NullPointerException error) {
                    Errors.debug("Not a valid point");
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
                if(e.getButton()==1 || e.getButton()==3) {
                    imc.mm.offsetByMouse(this.mouseClickPoint, this.mouseReleasePoint);
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
    
    /**
     * Listener methods for button/combobox actions
     */
    private class ButtonMethods implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            if(e.getSource() == cEnd && 
              (cStart.getSelectedItem().getClass()==NodeItem.class && 
               cEnd.getSelectedItem().getClass()==NodeItem.class) ) {
                
                NodeItem start = (NodeItem)cStart.getSelectedItem();
                NodeItem end   = (NodeItem)cEnd.getSelectedItem();
                
                if(start.map().compareTo(imc.map())!=0) {
                    setMap(start.map());
                    update();
                }
                
                showDirections(new Dijkstra(imc.nm, imc.nm.getNodeById(start.id()), end));
            }
        }
    }
    
    /**
     * Listener methods for slider actions
     */
    /* 
     * private class SliderMethods implements ChangeListener {
     *   public void stateChanged(ChangeEvent e) {
     *       JSlider s = (JSlider)e.getSource();
     *       Point newCoords = new Point();
     *       if(!s.getValueIsAdjusting()) {
     *           if(e.getSource()==sPanY)
     *               newCoords.setLocation( imc.getCoordinates().getX(), s.getValue() );
     *           else if(e.getSource()==sPanX)
     *               newCoords.setLocation( s.getValue(), imc.getCoordinates().getY() );
     *           imc.mm.setOrigin(newCoords);
     *       }
     *   }
     *}*/
    
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
        cp      = getContentPane();
        mm      = new MouseMethods();
        bm      = new ButtonMethods();
        
        status  = new JLabel();
        cStart  = new JComboBox(nodes);
        cEnd    = new JComboBox(nodes);
        
        cStart.addActionListener(bm);
        cEnd.addActionListener(bm);
        
        bottom  = new JPanel(new GridLayout(1,3));
        bottomL = new JPanel(new FlowLayout());
        bottomC = new JPanel(new FlowLayout());
        bottomR = new JPanel(new FlowLayout());
        
        bottomL.add(new JLabel("From"));
        bottomL.add(cStart);
        bottomL.add(new JLabel("to"));
        bottomL.add(cEnd);
        bottomR.add(status);
        bottom.add(bottomL);
        bottom.add(bottomR);
        
        cp.setLayout(new BorderLayout());
        cp.add(bottom, BorderLayout.SOUTH);
        
        update();
    }
    
    public void update() {
        status.setText("map: " + imc.map());

        imc.addMouseListener(mm);
        imc.addMouseMotionListener(mm);
        imc.addMouseWheelListener(mm);
        
        if(cp.getComponentCount()>=2)
            cp.remove(1);

        cp.add(imc);
        
        //contentPane.paintComponents(contentPane.getGraphics());
        cp.validate();
    }
    
}
