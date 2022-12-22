package util;


import java.io.PrintStream;

/** Collection of methods for logging message on a default PrintStream {@link #out}.
 * <p>
 * Log messages have a default {@link #header header} and {@link #trailer trailer}.
 */
public final class SimpleLog {
	private SimpleLog() {}
	
	public static PrintStream out= System.out;
	public static String trailer= "";
	
	public static String header= ">>>>>>>>>>>>>>> TEST: ";
	public static void log(String msg) {
		if (out!=null) out.println(header+msg+trailer);
	}

	public static void log(Class<?> c, String msg) {
		if (c==null) log(msg);
		else
		if (msg==null) log(c.getSimpleName());
		else log(c.getSimpleName()+": "+msg);
	}

	public static void log(Object o, String msg) {
		if (o!=null) log(o.getClass(),msg); else log(msg);
	}
	
}
