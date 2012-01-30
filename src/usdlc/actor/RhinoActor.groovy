package usdlc.actor

import usdlc.Store
import usdlc.drivers.JavaScript
import static usdlc.config.Config.config

class RhinoActor extends Actor {
	JavaScript javascript
	def binding

	void init() {
		javascript = exchange.request.session.instance JavaScript
		binding = [exchange: exchange, support: this]
	}

	public void run(Store script) {
		javascript.run(script, binding)
	}

	public $sleep(seconds) {
		sleep((long) (seconds * 1000))
	}
	/**
	 * Another script can be run int the same context with:
	 *
	 * run "path/scriptName"
	 * dsl "dslName"
	 * include "scriptName.type"
	 *
	 * Where "type" is same as running script if not defined. Must
	 * eventually parse down to JavaScript. All incarnations
	 * search on the DSL path as well as given the raw name.
	 */
	public include(name) {
		if (name.indexOf('.') == -1) {
			name += ".$currentlyRunningScript.parts.ext"
		}
		if (!dsls.containsKey(name)) {
			dsls[name] = sameDirAsEnclosingScript(name) ?:
				onDslSourcePath(name)
		}
		if (!dsls[name]) throw new Exception("No include script $name")
		try {
			run dsls[name]
		} catch (e) { throw new Exception("including '$name'", e) }
	}

	Store onDslSourcePath(String name) {
		config.dslSourcePath.findResult { String path ->
			def store = Store.base("$path$name")
			store.exists() ? store : null
		}
	}

	Store sameDirAsEnclosingScript(name) {
		if (name.indexOf('/') == -1) {
			name = "$currentlyRunningScript.parent/$name"
			def store = Store.base(name)
			return store.exists() ? store : null
		}
		return null
	}

	static dsls = [:]
}
