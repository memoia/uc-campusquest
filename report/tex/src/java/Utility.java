/*
 * Utility.java
 *
 * Created on January 5, 2006, 10:55 AM
 */

package project01;

import java.awt.Dimension;
import java.io.*;
import java.net.*;
import java.util.LinkedList;
import java.util.jar.*;


/**
 *
 * @author Ian Melnick
 */
public class Utility {    
    public static Args getArgs(String[] args, Main prog) {
        Args a = new Args();
        
        if(args.length>0 && (args[0].equals("-h") || args[0].equals("--help"))) {
            System.out.println("Usage: java -jar Project01.jar <Editor|Router> <map> <width> <height>");
            System.exit(0);
        }
        
        if(args.length>0 && args[0]!=null) {
            try {
                a.mode = a.mode.valueOf(args[0]);
            } catch (IllegalArgumentException err) {
                System.out.print("Can't do " + args[0] + ", but I do know these: ");
                for(Main.AppModes op : Main.AppModes.values()) {
                    System.out.print(op + " ");
                }
                System.exit(1);
            }
        }
        else {
            a.mode = a.mode.valueOf("Router");
        }
        
        if(args.length>1 && args[1]!=null) {
            a.map = args[1];
        }
        else {
            try {
                a.map = prog.getParameter("m");
            } catch (java.lang.NullPointerException e) {
                a.map = "scieng_100";
            }
        }
        
        // When called as an applet, it will get width/height properties
        // the normal way; only init() will be called, so we don't have to
        // worry about main().
        if(args.length>3 && (args[2]!=null && args[3]!=null)) {
            a.resolution = new Dimension(Integer.parseInt(args[2]), 
                                         Integer.parseInt(args[3]));
        }
        else {
            a.resolution = new Dimension(900,700);
        }
        
        return a;
    }
    
    public static String[] map_images(String matching) {
        if(matching==null) matching = "";
        final String mapDescrip = matching;
        final String lookForDir = "images/";
        final String mapPrefix  = "map_";
        
        // Open directory containing map images
        // If running from JAR, must extract names from JAR.
        // Otherwise, just scan directory containing images.
        String imgLoc = Utility.class.getResource(lookForDir).toString();
        if(imgLoc.startsWith("jar:")) {
            LinkedList<String> imgList = new LinkedList<String>();
            
            URI filePath = URI.create(imgLoc.split("!")[0].substring(4));
            JarInputStream jin = null;
            try {
                jin=new JarInputStream(new FileInputStream(new File(filePath)));
            } catch (Exception er) {
                Errors.debug(er);
            }
            
            JarEntry jar = null;
            String pathParts[] = null;
            try {
                while( (jar = jin.getNextJarEntry()) != null )
                    if(jar.getName().contains(lookForDir + mapPrefix) && (pathParts = jar.getName().split("/"))!=null)
                        imgList.add(pathParts[pathParts.length-1]);
            } catch (Exception er) {
                Errors.debug(er);
            }
            
            return imgList.toArray(new String[0]);
        }
        else {
            File imageDir = new File(URI.create(imgLoc));
            if(!imageDir.isDirectory())
                System.err.println(imageDir + " is not a directory! Continuing...");
                
            // Only allow map images from same building as current map
            // FilenameFilter: http://javaalmanac.com/egs/java.io/GetFiles.html
            // imc.map().split("_")[0]  == _building
            FilenameFilter filter = new FilenameFilter() {
                public boolean accept(File dir, String name) {
                    return name.startsWith(mapPrefix + mapDescrip);
                }
            };
            return imageDir.list(filter);
        }
        
        //return null;
    }
}

class Args {
    public Main.AppModes mode;
    public Dimension resolution;
    public String map;
}
