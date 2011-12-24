package usdlc.actor

import usdlc.JavaScript
import usdlc.Store

class RhinoActor extends Actor {
	JavaScript javascript
	def binding

	void init() {
		javascript = exchange.request.session.instance JavaScript
		binding = [exchange : exchange, support: this]
	}

	public void run(Store script) {
		javascript.run(script, binding)
	}

	public $sleep(seconds) {
		sleep((long) (seconds * 1000))
	}
}
