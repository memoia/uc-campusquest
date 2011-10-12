/*
 * NodeEditor.java
 *
 * Created on December 11, 2005, 8:22 PM
 */


// http://java.sun.com/docs/books/tutorial/uiswing/events/windowlistener.html
// http://java.sun.com/j2se/1.4.2/docs/api/java/awt/event/WindowAdapter.html


package project01;

import javax.swing.*;
import javax.swing.event.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;



/**
 * Dialog for changing node (and edge) properties.
 *
 * @author Ian Melnick
 */
public class NodeEditor {
    JFrame window;
    JTabbedPane tabs;
    JPanel bottom, npf, ecf, ecp, fpf, fcp;
    
    JTextField description, distance;
    JCheckBox visible, delete, deledge, linksFloors;
    JComboBox connections, floorLinks;
    int dnodeId;
    JButton save, addFloorLink, delFloorLink;
    
    NodeItem node;
    MapComponent imc;
    LinkedList<NodeItem> cl;
    
    WindowMethods wm;
    ButtonMethods bm;
    ItemMethods   im;
    
    /**
     * Creates a new instance of NodeEditor 
     */
    public NodeEditor(NodeItem node, MapComponent imc) {
        this.node = node;
        this.imc = imc;
        this.dnodeId = 0;        
        this.cl = imc.nm.connectedNodes(node);
        
        wm = new WindowMethods();
        bm = new ButtonMethods();
        im = new ItemMethods();
        
        window = new JFrame("Modify Node " + this.node.id());
        window.setLayout(new BorderLayout());
        window.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        
        
        tabs = new JTabbedPane();
        bottom = new JPanel();
        npf = new JPanel();
        ecf = new JPanel();
        ecp = new JPanel();
        fpf = new JPanel();
        fcp = new JPanel();
        
        
        bottom.setLayout(new FlowLayout());
            save = new JButton("Update");
            bottom.add(save);
        
        npf.setLayout(new GridLayout(6,2));
            npf.add(new JLabel("ID"));
            npf.add(new JLabel(String.valueOf(node.id())));
            
            npf.add(new JLabel("Location"));
            npf.add(new JLabel(node.location().toString()));
            
            description = new JTextField(node.description());
            npf.add(new JLabel("Description"));
            npf.add(description);
            
            visible = new JCheckBox("Visible",node.visibility());
            delete = new JCheckBox("Delete", false);
            npf.add(visible);
            npf.add(delete);
            
        ecp.setLayout(new GridLayout(2,3));     // Edge properties panel, shown only when edge selected via ecf
            ecp.setVisible(false);
            //ecp.add(new JLabel("Orientation N/A"));
            
            distance = new JTextField();          // set in ButtonMethods
            ecp.add(new JLabel("Distance (Paces)"));
            ecp.add(distance);
            
            deledge = new JCheckBox("Delete", false);
            ecp.add(deledge);
        
        ecf.setLayout(new BorderLayout());
            if(cl.size() > 0) {
                Vector<String> nids = new Vector<String>();
                nids.add("Select connected node to modify edge");
                ListIterator<NodeItem> li = cl.listIterator();
                NodeItem t;
                while(li.hasNext()) {
                    t = li.next();
                    nids.add(String.valueOf(t.id()));
                }
                connections = new JComboBox(nids);
                ecf.add(connections, BorderLayout.NORTH);
            }
        
            ecf.add(ecp/*, BorderLayout.SOUTH*/);
        
        // container for floorLink properties
        fpf.setLayout(new BorderLayout());
        fcp.setLayout(new FlowLayout());
            addFloorLink = new JButton("Add New Link");
            delFloorLink = new JButton("Remove Selection");
            initFloorLinkPane();
        
        tabs.addTab("Node", null, npf, "Change Node Properties");
        tabs.addTab("Edges", null, ecf, "Change Edge Properties");
        tabs.addTab("Floors", null, fpf, "Change Floor-Link Properties");
        window.add(tabs);
        window.add(bottom, BorderLayout.SOUTH);
        
        window.addWindowListener(wm);
        save.addActionListener(bm);
        addFloorLink.addActionListener(bm);
        delFloorLink.addActionListener(bm);
        if(node.isFloorLink())  floorLinks.addActionListener(bm);
        if(cl.size() > 0)       connections.addActionListener(bm);
        
        window.pack();
        window.setVisible(true);
    }
    
    
    
    private void initFloorLinkPane() {
        fpf.removeAll();
        //fpf.add(new JLabel("Get down on the floors, whores!"));
        /*
         * Checkbox: This node connects to other floors
         * If checkbox is yes, display:
         *   List of connected floors
         *   Remove button
         *   Add button
         */
        linksFloors = new JCheckBox("Links floors",node.isFloorLink());
        linksFloors.addItemListener(im);
        fpf.add(linksFloors, BorderLayout.NORTH);
        fpf.add(fcp);
        
        // floorLink editor pane (shown only when selected via fpf)
        fcp.removeAll();
        floorLinks = new JComboBox(node.connectedFloors().toArray());
        fcp.add(addFloorLink);
        if(node.isFloorLink()) {
            fcp.add(floorLinks);
            fcp.add(delFloorLink);
        }
        fcp.setVisible(node.isFloorLink());
    }
    
    
    
    
    private class WindowMethods extends WindowAdapter {
        public void windowClosed(WindowEvent e) {
            node.select();
            imc.repaint();
        }
    }
    
    private class ItemMethods implements ItemListener {
        public void itemStateChanged(ItemEvent e) {
            
            if(e.getSource() == linksFloors) {
                switch(e.getStateChange()) {
                    case ItemEvent.SELECTED:
                        fcp.setVisible(true);
                        break;
                    case ItemEvent.DESELECTED:
                        fcp.setVisible(false);
                        //TODO: turn this into a method
                        Set<String> floors = node.connectedFloors();
                        Iterator<String> j = floors.iterator();
                        String floor;
                        NodeItem connection;
                        while(j.hasNext()) {
                            floor = j.next();
                            connection = node.floorLink(floor);
                            //connection.delFloorLink(floor);
                            FloorLinker.deleteConnection(imc.map(), floor, connection.id());
                            node.delFloorLink(floor);
                        }
                        imc.nm.saveNodes();
                        initFloorLinkPane();
                        break;
                }
            }
            
        }
    }
    
    /* Listener methods for button and combobox actions */
    private class ButtonMethods implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            
            if(e.getSource() == save) {
                // set node properties as modified in dialog
                if(delete.isSelected())
                    imc.nm.delNode(node);
                else {
                    if(visible.isSelected()!=node.visibility())
                        node.visible();
                    node.setDescription(description.getText());
                }
                
                // set edge properties
                saveEdgeProperties();
                
                // close window
                window.dispose();
            }
            
            if(e.getSource() == connections) {
                try {
                    if(dnodeId!=0)
                        saveEdgeProperties();
                    dnodeId = Integer.parseInt((String)connections.getSelectedItem());
                    distance.setText(((imc.nm.sharedEdge(node, imc.nm.getNodeById(dnodeId))).getDistance())+"");
                    ecp.setVisible(true);
                } catch (NumberFormatException ex) {
                    dnodeId = 0;
                    //System.err.println(ex);
                    // Remove properties window
                    ecp.setVisible(false);
                }
            }
            
            // Floor-link actions
            
            if(e.getSource() == addFloorLink) {
                String[] images = Utility.map_images(imc.map().split("_")[0]);
                
                // Create new FloorLinkers for floors above and below this one.
                int thisFloor = Character.valueOf((imc.map().split("_")[1]).charAt(0));
                int otherFloor;
                String otherMap;
                FloorLinker linker;
                for(int i=0; i<images.length; i++) {
                    otherFloor = Character.valueOf((images[i].split("_")[2]).charAt(0));
                    otherMap = images[i].split("map_")[1];
                    otherMap = otherMap.substring(0, otherMap.length()-4);
                    if((otherFloor==thisFloor+1 || otherFloor==thisFloor-1) && !node.hasFloorLink(otherMap)) {
                        // One FloorLinker at a time---otherwise Heap barfs
                        // Linker takes care of updating node data once user selects dest node
                        linker = new FloorLinker(imc.map(), otherMap, node, imc.getCoordinates());
                        break;
                    }
                }
                //imc.nm.saveNodes();
            }
            
            if(e.getSource() == delFloorLink) {
                //System.out.println("delFloorLink");
                // Delete link from node on each floor
                // NodeItem.delFloorLink( (String)linksFloors.getSelectedItem() );
                String floor = (String)floorLinks.getSelectedItem();
                Set<String> updFloors = node.connectedFloors();
                Iterator<String> i = updFloors.iterator();
                NodeItem next;
                while(i.hasNext()) {
                    next = node.floorLink(i.next());
                    FloorLinker.deleteConnection(imc.map(), floor, next.id());
                }
                node.delFloorLink(floor);
                imc.nm.saveNodes();
                initFloorLinkPane();
            }

        }
        
        //
        
        private void saveEdgeProperties() {
            NodeItem dnode  = imc.nm.getNodeById(dnodeId);
            EdgeItem edge   = imc.nm.sharedEdge(node, dnode);
            if(dnodeId > 0 && deledge.isSelected()) {    
                node.delEdge(edge);
                dnode.delEdge(edge);
            }
            else if(edge!=null) {
                try {
                    edge.setDistance(Integer.parseInt(distance.getText()));
                } catch(NumberFormatException err) {
                    JOptionPane.showMessageDialog(null, "Invalid Distance", "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        }
    }
    
}
