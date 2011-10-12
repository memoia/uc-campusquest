/*
 * Main.java
 *
 * Created on January 5, 2006, 10:49 AM
 */

package project01;

import javax.swing.*;
import javax.swing.event.*;
import java.awt.*;
import java.awt.event.*;
import java.applet.*;
import com.bruceeckel.swing.*;
import java.lang.*;


/**
 *
 * @author Ian Melnick
 */
public class Main extends JApplet {
    enum AppModes { Editor, Router }
    public Args a;
    
    
    /** Creates a new instance of Main */
    public Main() {
        a = Utility.getArgs(new String[0], this);
    }
    public Main(String[] args) {
        a = Utility.getArgs(args, this);
    }
    
    public void launch() {
        switch(a.mode) {
            case Router:
                Console.run(new Router(a), (int)a.resolution.getWidth(), (int)a.resolution.getHeight());
                break;
            case Editor:
                Console.run(new Editor(a), (int)a.resolution.getWidth(), (int)a.resolution.getHeight());
                break;
        }
    }
    
    
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        
        Main prog = new Main(args);
        prog.launch();
        
        /*} catch (java.lang.OutOfMemoryError err) {
            JOptionPane.showMessageDialog(null, "Ran out of memory; try increasing heap size. If using browser, increase through Java control panel---look for Runtime Settings/Java Runtime Param, add -Xmx96m", "Error", JOptionPane.ERROR_MESSAGE);
            System.out.println("Ran out of memory, start with increased heap size: -Xmx96m");
            System.exit(1);
        }*/
        
        
    }
    
}