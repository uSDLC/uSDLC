package usdlc.actor

import usdlc.Store

class HtmlActor extends Actor {
	/**
	 Use to generate HTML to display on the screen.
	 */
	void run(Store script) {
		exchange.response.write exchange.store.read()
		exchange.response.write bootstrapJs
	}

	static bootstrapJs =
		"<script src='/usdlc/rt/js/bootstrap.coffeescript'></script>".bytes
}
