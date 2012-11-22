package usdlc.actor

import usdlc.Store
import usdlc.drivers.CoffeeScript

class WorkflowActor extends Actor {
	void run(Store script) {
		def globalVar = exchange.request.query.globalVar
		exchange.response.write """
			window.$globalVar = function() {
				nothing = null
				"""

		// array of js files to combine
		def files = [
			script, script.rebase('Owners.workflow'),
			script.rebase('Defaults.workflow'),
			script.rebase('Workflows.workflow'),
			Store.base('/usdlc/support/client/workflowDSL.coffeescript')]

		CoffeeScript compiler = exchange.request.session.instance CoffeeScript
		compiler.bare = true
		Store js = compiler.javascript(files)
		exchange.response.write js.read() ?: ''

		exchange.response.write '}\n'
	}
}
