package usdlc.actor

import usdlc.Store
import usdlc.drivers.Jython

class JythonActor extends Actor {
	Jython jython
	def binding

	void init() {
		jython = exchange.request.session.instance Jython
		binding = [exchange : exchange, support: this]
	}

	public void run(Store script) {
		jython.run(script, binding)
	}
}
