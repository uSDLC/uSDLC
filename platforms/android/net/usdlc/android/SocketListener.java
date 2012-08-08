package net.usdlc.android;

import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.Reader;
import java.net.MalformedURLException;
import java.net.Socket;
import java.net.URL;
import java.util.ArrayList;

public abstract class SocketListener extends Thread {
	private PrintWriter writer;
	protected String address;

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
				" HTTP/1.1\r\nUser-Agent: uSDLC/1.0\r\n\r\n";
		//noinspection InfiniteLoopStatement
		while (true) {
			Socket socket = null;
			try {
				socket = new Socket(host, port);
				writer = new PrintWriter(socket.getOutputStream());
				BufferedReader reader = new BufferedReader(new
					InputStreamReader(socket.getInputStream()), 512);
				log("connected");
				send(request);
				String[] header = readBlock(reader);
				while (true) {
					String[] command = readBlock(reader);
					if (command.length == 0) break;
					String response = process(command);
					if (response == null) {
						response = "error,not implemented,"+command[0];
					}
					send(response + "\r\n");
				}
			} catch (Exception e) {
				error("", e);
			} finally {
				try { socket.close(); } catch (Exception ignored) {}
				waitForConnection();
			}
		}
	}
	/**
	 * Wait for a connections (override if more than time can be used
 	 */
	protected void waitForConnection() {
		try {
			// not as bad as it looks as a change in network status
			// will give it a good kick in the goolies
			sleep(10000);
		} catch (Exception ignored) {}
	}

	private String[] readBlock(BufferedReader reader) throws
		Exception {
		ArrayList<String> block = new ArrayList<String>();
		String line = "";
		// drop blank lines
		do {
			line = reader.readLine();
		} while (line != null  &&  line.length() == 0);

		while (line != null  &&  line.length() != 0) {
			block.add(line);
			line = reader.readLine();
		}
		//noinspection ToArrayCallWithZeroLengthArrayArgument
		return block.toArray(new String[0]);
	}
	/**
	 * Override to process a command block. Return null of command not
	 * implemented.
	 */
	protected String process(String[] command) { return null; }
	/**
	 * Override if you need to send more than just the raw command
	 */
	protected void send(String contents) {
		writer.print(contents + "\r\n");
		writer.flush();
	}

	protected static final String TAG = "Socket Listener";
	protected void error(String message, Throwable exception) {
		Log.e(TAG, message, exception);
	}
	protected void error(String message) {
		Log.e(TAG, message);
	}
	protected void log(String message) {
		Log.i(TAG, message);
	}
}
