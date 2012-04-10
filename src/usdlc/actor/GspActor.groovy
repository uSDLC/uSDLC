package usdlc.actor

import groovy.text.SimpleTemplateEngine
import usdlc.Store

class GspActor extends Actor {
	SimpleTemplateEngine engine;

	void init() {
		engine = exchange.request.session.instance SimpleTemplateEngine
	}

	public void run(Store script) {
		def template = engine.createTemplate(script.text).make(context)
		context.exchange.response.write template
	}
}
