package usdlc.actor

import usdlc.Store
import usdlc.drivers.JavaScript

import static usdlc.config.Config.config

class RhinoActor extends Actor {
	JavaScript javascript
	def binding, stack = [null]

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
//	public include(name) {
//		name = Store.camelCase(name)
//		def key = "$name#$currentlyRunningScript.pathFromWebBase"
//		if (name.indexOf('.') == -1) {
//			name += ".$currentlyRunningScript.parts.ext"
//		}
//		if (!dsls.containsKey(key)) {
//			dsls[key] = onParentPath(name) ?: onDslSourcePath(name)
//		}
//		if (!dsls[key]) {
//			throw new Exception("No include script $name on parent path or $config.dslSourcePath")
//		}
//		try {
//			run dsls[key]
//		} catch (e) { throw new Exception("including '$name'", e) }
//	}
	public include(name) {
		name = Store.camelCase(name)
		if (name.indexOf('.') == -1) {
			name += ".$currentlyRunningScript.parts.ext"
		}
		def found = onParentPath(name) ?: onDslSourcePath(name)
		if (!found) {
			throw new Exception("No include script $name on parent path or $config.dslSourcePath")
		}
		try {
			stack.push(found)
			run found
			stack.pop()
		} catch (e) { throw new Exception("including '$name'", e) }
	}

	Store onDslSourcePath(name) {
		def project = currentlyRunningScript.project
		config.dslSourcePath.findResult { String path ->
			def store = Store.base("$path$name", project)
			store.exists() ? store : null
		}
	}
	/**
	 * Return first matching file searching up parent tree.
	 */
	Store onParentPath(name) {
		if (name.indexOf('/') == -1) {
			def lastScript = stack.last() ?: currentlyRunningScript
			return lastScript.onParentPath {
				def possible = it.rebase(name)
				// Allow script to include same name and find in parent path
				if (possible == lastScript) return null
				return possible.ifExists()
			}
		}
		return null
	}

//	static dsls = [:]
}
