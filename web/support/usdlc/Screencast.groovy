/*
 * Copyright 2011 the Authors for http://usdlc.net
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package usdlc

import org.openqa.selenium.By
import org.openqa.selenium.JavascriptExecutor
import usdlc.actor.Actor
import static usdlc.Config.config

class Screencast extends CoffeeScript.Delegate {
	boolean command(cmd, params) {
		params = params.collect { "'$it'" }.join(',')
		script("usdlc.screencast.$cmd($params)")
		true
	}
	def commands = [
		prompt: { text ->
			command('note', [text])
			semaphore.wait {}
		},
		create: { params ->
			assert false: "not implemented for $params"
		},
		sleep: { params ->
			assert false: "not implemented for $params"
		},
		timeout: { minutes -> timeout = minutes * 60 },
		click: { params ->
			assert false: "not implemented for $params"
		},
		check: { types -> types.check() },
		insert: { params ->
			assert false: "not implemented for $params"
		},
		link: { params ->
			assert false: "not implemented for $params"
		},
		select: { params ->
			assert false: "not implemented for $params"
		},
		next: { params ->
			assert false: "not implemented for $params"
		},
		section: { params ->
			assert false: "not implemented for $params"
		},
		source: { params ->
			assert false: "not implemented for $params"
		},
		keys: { params ->
			assert false: "not implemented for $params"
		},
		menu: { params ->
			assert false: "not implemented for $params"
		},
		step: { params ->
			assert false: "not implemented for $params"
		},
		element: { selector, contents ->
			[ check: {
					web.waitFor(selector) { element ->
						assert element.text ==~ contents
					}
				}]
		},
	]
	WebDriver web = new WebDriver()
	void init() {
		web.load("http://$exchange.request.header.host?Sandbox")
		web.waitFor(By.cssSelector('div.screencast')) {}
		command('keys', [config.screencast.keys])
		Actor.cache['usdlc/screencast/response'] =
				new ScreencastResponseActor(semaphore: semaphore)
	}

	void async(String script, Object... args) {
		((JavascriptExecutor) web.driver).executeAsyncScript(script, args)
	}

	void script(String script, Object... args) {
		((JavascriptExecutor) web.driver).executeScript(script, args)
	}
	Semaphore semaphore = new Semaphore(timeout)
	int timeout = 120	// defaults to 2 minutes
}

class ScreencastResponseActor extends Actor {
	void run(Store script) {
		semaphore.release()
	}
	Semaphore semaphore
}