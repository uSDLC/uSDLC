package usdlc.actor

import usdlc.Store
import usdlc.drivers.CoffeeScript

/**
 * Parse coffee-script to java-script for the browser. Compress if flagged to do so.
 */
class CoffeescriptActor extends Actor {
	void run(Store script) {
		CoffeeScript compiler = exchange.request.session.instance CoffeeScript
		Store js = compiler.javascript(exchange.store)
		exchange.response.write js.read() ?: ''
	}
}
