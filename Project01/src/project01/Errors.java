/*
 * project01: Description
 * Ian Melnick (dazed)
 * March 17, 2006, 2:04 PM
 * Course, Prof
 *
 * Errors.java
 *
 * Exception/Thread tracing info provided in:
 * http://www.rgagnon.com/javadetails/java-0420.html
 */

package project01;

/**
 *
 * @author Ian Melnick
 */
public class Errors {
    
    private static String source() {
        StackTraceElement method = Thread.currentThread().getStackTrace()[4];
        StackTraceElement caller = Thread.currentThread().getStackTrace()[5];
        return method.getClassName() + "."
                + method.getMethodName() + "()"
                + " ["+caller.getMethodName() + ":"
                + caller.getLineNumber()+"]";
    }
    
    public static void debug(Object message) {
        System.out.println(source() + " " + message);
    }
    
}
