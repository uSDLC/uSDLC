package usdlc

import java.util.concurrent.ArrayBlockingQueue

class RemoteComms {
	ArrayBlockingQueue fromUSDLC = new ArrayBlockingQueue(128)
	ArrayBlockingQueue toUSDLC = new ArrayBlockingQueue(32)

	static connections = [:]

	static RemoteComms get(name) {
		name = name.toString()
		if (!(name in connections)) connections[name] = new RemoteComms()
		return connections[name]
	}
}
