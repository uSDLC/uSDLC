package usdlc

import org.apache.commons.logging.LogFactory
import org.apache.commons.logging.impl.SimpleLog

/**
 * Many of the packages use the Apache commons logging wrapper. This class is
 * used to point the results to
 * the response to the browser. Use for the offending classes like:
 *
 * 	static {
 * 	  LogFactory.getFactory().setAttribute("org.apache
 * 	    .commons.logging.Log", "usdlc.Log");
 *  }
 */
class Log extends SimpleLog {
	Log(String name) { super(name) }
	/**
	 * By default, SimpleLog sends to stderr. Let's redirect it to
	 * somewhere more useful.
	 * @param buffer buffer the logger has prepared for the record
	 */
	protected void write(StringBuffer buffer) { System.out.println buffer }
	/**
	 * Given a file path to save the log to, return a closure that will
	 * write whatever it is given
	 * @param name Name/path of file to append log information to.
	 * @return closure to call to write to the log.
	 */
	static file(name) {
		def store = Store.base(".store/log/${name}.log")
		store.append("\n${new Date().format('yyyy-MM-dd HH:mm')}: ")
		return { store.append it }
	}
	/**
	 * Write row to csv log file
	 */
	static csv(name, row) {
		def store = Store.base("${name}.csv")
		store.append "\n${new Date().format('yyyyMMdd,HHmm,Z')},"
		store.append row
	}
	/**
	 * Shortcut convenience method to write to stdout (server output stream).
	 * @param message Message to write
	 */
	static inf(message) { System.out.println message; message }
	/**
	 * Shortcut convenience method to write to stderr (server output stream).
	 * @param message Message to write
	 */
	static err(message) { System.err.println message; message }
	/**
	 * The most likely (and in fact only) use for this class is the
	 * interception Apache Commons logging -
	 * so we had better tell the factory.
	 */
	static void apacheCommons() {
		def log = 'org.apache.commons.logging.Log'
		LogFactory.factory.setAttribute(log, 'usdlc.Log');
		// "trace", "debug", "info", "warn", "error", or "fatal"
		def defaultLog = 'org.apache.commons.logging.simplelog.defaultlog'
		System.properties[defaultLog] = 'error';
	}
}
