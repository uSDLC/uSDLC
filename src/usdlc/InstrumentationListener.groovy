package usdlc

class InstrumentationListener {
	def serverSocket

	InstrumentationListener(port = 9002) {
		println "Start instrumentation..."
		serverSocket = new ServerSocket(port)
	}

	void run() {
		def running = true
		while (running) {
			println "Accepting socket connection"
			def clientSocket = serverSocket.accept()
			def output = new PrintWriter(clientSocket.getOutputStream(),
					true);
			def input = new BufferedReader(new InputStreamReader(clientSocket
					.getInputStream()));

			def request = input.readLine()
			def device = request.split('=')[-1].split(' ')[0]
			println "Device: $device"
			def remote = usdlc.RemoteComms.get(device)

			def connectionValid = true
			def commandSender = Thread.start {
				while (connectionValid) {   // command to device
					println "Waiting for a command"
					def code = remote.fromUSDLC.take()
					if (code) {
						println "Sending a command ($code) to the device"
						// not the second line marker - code is blocks
						// of lines separated by a blank line
						output.println("$code\r\n")
					}
				}
			}

			def inHeader = true
			def line   // response from device
			while ((line = input.readLine()) != null) {
				line = line.trim()
				if (line.size() == 0) {
					inHeader = false
				} else if (line.equals('exit')) {
					println "Exit response received"
					connectionValid = running = false
					break
				} else if (!inHeader) {
					println "Read response from device ($line)"
					remote.toUSDLC.put(line)
				}
			}
			connectionValid = false
			synchronized (commandSender) {commandSender.notify()}
		}
		println "...end instrumentation"
	}
}
