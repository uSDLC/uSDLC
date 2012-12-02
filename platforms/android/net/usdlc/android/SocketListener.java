package net.usdlc.android;

//import android.util.Log;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.MalformedURLException;
import java.net.Socket;
import java.net.URL;

public class SocketListener extends Thread {
	private PrintWriter writer;
	public String address;

	public SocketListener() { address = "http://127.0.0.1:9000/socket"; }
	public SocketListener(String url) { address = url; }

	@Override
	public void run() {
		URL url;
		try {
			url = new URL(address);
		} catch (MalformedURLException e) {
			throw new RuntimeException("bad URL "+address, e);
		}
		String host = url.getHost();
		int port = url.getPort();
		String request = "GET " + url.getFile() +
				" HTTP/1.1\r\nUser-Agent: uSDLC/2.0\r\n\r\n";
		//noinspection InfiniteLoopStatement
		while (running) {
			Socket socket = null;
			try {
				socket = new Socket(host, port);
				socket.setKeepAlive(true);
				socket.setSoLinger(true, 1000);
				socket.setSoTimeout(0);
				writer = new PrintWriter(socket.getOutputStream());
				BufferedReader reader = new BufferedReader(new
					InputStreamReader(socket.getInputStream()), 512);
				log("reconnect");
				send(request);
//				String header = readBlock(reader);
				while (running) {
					String code = readBlock(reader).trim();
					if (code.length() == 0) break;
					String response = process(code);
					send(response + "\r\n");
				}
			} catch (Exception e) {
				error("", e);
			} finally {
				try { socket.close(); } catch (Exception ignored) {}
				if (running) waitForConnection();
			}
		}
	}
	boolean running = true;
	public void terminate() {
		running = false;
		this.notifyAll();
	}
	/**
	 * Wait for a connections (override if more than time can be used)
 	 */
	protected void waitForConnection() {
		try {
			// not as bad as it looks as a change in network status
			// will give it a good kick in the goolies
			sleep(10000);
		} catch (Exception ignored) {}
	}

	private String readBlock(BufferedReader reader) throws
		Exception {
		StringBuilder block = new StringBuilder(128);
		String line = "";
		// drop blank lines
		do {
			line = reader.readLine();
		} while (running && line != null  &&  line.length() == 0);

		while (running && line != null  &&  line.length() != 0) {
			block.append(line).append('\n');
			line = reader.readLine();
		}
		//noinspection ToArrayCallWithZeroLengthArrayArgument
		return block.toString();
	}
	/**
	 * Override to process a command block. Return null if command not
	 * implemented. Otherwise send the response line.
	 */
	protected String process(String code) { return null; }
	/**
	 * Override if you need to send more than just the raw command
	 */
	protected void send(String contents) {
		writer.print(contents + "\r\n");
		writer.flush();
	}

	protected static final String TAG = "Socket Listener";
	protected void error(String message, Throwable exception) {
//		Log.e(TAG, message, exception);
	}
	protected void error(Object message) {
//		Log.e(TAG, message.toString());
	}
	protected void log(String message) {
//		Log.i(TAG, message);
	}
}
