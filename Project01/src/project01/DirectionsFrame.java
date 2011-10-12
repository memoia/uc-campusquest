/*
 * DirectionsFrame.java
 *
 * Created on January 8, 2006, 5:45 PM
 */

//http://java.sun.com/developer/techDocs/hi/repository/TBG_Navigation.html
//http://java.sun.com/docs/books/tutorial/uiswing/components/toolbar.html
//http://java.sun.com/docs/books/tutorial/uiswing/components/scrollpane.html

package project01;

import javax.swing.*;
import javax.swing.event.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;



/**
 * Frame responsible for stepping user through point-to-point directions.
 *
 * @author Ian Melnick
 */
public class DirectionsFrame {
    JFrame window;
    JTextArea directionBox;
    int curDirection;
    String curMap;
    JScrollPane scrollPane;
    JToolBar toolBar;
    JButton bPrevious, bNext;
    Dijkstra d;
    MapComponent.MapManager mm;
    LinkedList<NodeItem> r;
    Router router;
    
    /** Creates a new instance of DirectionsFrame */
    public DirectionsFrame(Router router, Dijkstra d) {
        this.d = d;
        this.router = router;
        this.mm = router.getMapManager();
        ButtonMethods bm = new ButtonMethods();
        curDirection = 0;
        curMap = router.getMap();
        
        window = new JFrame("Steps");
        window.setLayout(new BorderLayout());
        window.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
                
        bPrevious = new JButton();
            bPrevious.setActionCommand("previous");
            bPrevious.setToolTipText("Previous Instruction");
            bPrevious.addActionListener(bm);
            bPrevious.setText("<<");
        
        bNext = new JButton();
            bNext.setActionCommand("next");
            bNext.setToolTipText("Next Instruction");
            bNext.addActionListener(bm);
            bNext.setText(">>");

        toolBar = new JToolBar("Navigation");
        toolBar.add(bPrevious);
        toolBar.add(bNext);
        
        directionBox = new JTextArea();     //10, 30
        directionBox.setEditable(false);       
        scrollPane = new JScrollPane(directionBox);
        
        window.add(toolBar, BorderLayout.NORTH);
        window.add(scrollPane, BorderLayout.SOUTH);
        

        d.run();
        r = d.getRoute();
        showDirections(curDirection);
        d.reset();
        
                
        window.pack();
        window.setVisible(true);
    }
    

    private void showDirections(int curDirectionNumber) {
        directionBox.setText(null);
        NodeItem prvNode, curNode, nxtNode;
        for(int i=0; i<r.size(); i++) {
            if(i==0)                directionBox.append("Start at:\t");
            else if(i==r.size()-1)  directionBox.append("End at:\t");
            else                    directionBox.append("Go to:\t");
            
            if(i<r.size()-1)        nxtNode = r.get(i+1);
            else                    nxtNode = null;
            if(i>0)                 prvNode = r.get(i-1);
            else                    prvNode = null;
            curNode = r.get(i);
            
            double angle = -1;
            if(nxtNode!=null && prvNode!=null) {
                Point c = prvNode.location();
                Point p = curNode.location();
                Point n = nxtNode.location();
                
                Point u, v;  //line vectors
                u = new Point((int)(c.getX()-p.getX()),(int)(c.getY()-p.getY()));
                v = new Point((int)(n.getX()-p.getX()),(int)(n.getY()-p.getY()));
                
                double dp, mu, mv;  //dot product, magnitude u, magnitude v
                dp = (u.getX()*v.getX()) + (u.getY()*v.getY());
                mu = Math.sqrt(Math.pow(u.getX(),2)+Math.pow(u.getY(),2));
                mv = Math.sqrt(Math.pow(v.getX(),2)+Math.pow(v.getY(),2));
                
                angle = Math.toDegrees(Math.acos( dp / (mu*mv) ));
            }
            
            // TODO: Angle isn't enough to determine turning direction.
            
            //directionBox.append(String.valueOf(r.get(i).id()));
            /*if(angle > 85 && angle < 95) {
                directionBox.append("Continue\n");
                continue;
            }*/
            directionBox.append(curNode.description());        // + "\t"+angle
            if(i==curDirectionNumber) {
                directionBox.append("\t<<<");
                if(curNode.map().compareTo(curMap)!=0) {
                    curMap = curNode.map();
                    router.setMap(curMap);
                    router.update();
                    mm = router.getMapManager();
                }
            }
            directionBox.append("\n");
        }
    }
    
    
    
    /**
     * Listener methods for button actions
     */
    private class ButtonMethods implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            if(e.getSource() == bNext) {
                if(curDirection<r.size()-1)
                    curDirection++;
                showDirections(curDirection);
                mm.centerAroundPoint(r.get(curDirection).location());
            }
            if(e.getSource() == bPrevious) {
                if(curDirection>0)
                    curDirection--;
                showDirections(curDirection);
                mm.centerAroundPoint(r.get(curDirection).location());
            }
        }
    }    
    
}
