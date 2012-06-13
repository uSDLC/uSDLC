package usdlc.actor

import usdlc.Store
import usdlc.drivers.CoffeeScript

class CoffeeActor extends RhinoActor {
	void init() {
		super.init()
		//binding += [delegate : new CoffeeScript.Delegate(exchange : exchange)]
		compiler = exchange.request.session.instance CoffeeScript
		compiler.bare = true
	}
	public void run(Store me) {
		super.run(compiler.javascript(me, preprocess(me)))
	}
	CoffeeScript compiler
	def preprocessors = [:]
	def preprocessor(String ext, Closure preprocess) {
		preprocessors[ext] = preprocess
	}
	def preprocess(Store script) {
		preprocessors[script.parts.ext] ?: {text->text}
	}
}
