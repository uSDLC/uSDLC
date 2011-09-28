package usdlc

import org.openqa.selenium.By
import org.openqa.selenium.JavascriptExecutor

class Screencast extends CoffeeScript.Delegate {
	def commands = [
		note: { params ->
			async("usdlc.notes('${params.join(' ')}');")
		},
		prompt: { params ->
			assert false: "not implemented for $params"
		},
		create: { params ->
			assert false: "not implemented for $params"
		},
		sleep: { params ->
			assert false: "not implemented for $params"
		},
		timeout: { params ->
			assert false: "not implemented for $params"
		},
		click: { params ->
			assert false: "not implemented for $params"
		},
		check: { params ->
			assert false: "not implemented for $params"
		},
		title: { params ->
			assert false: "not implemented for $params"
		},
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
	]
	WebDriver web = new WebDriver()
	Screencast() {
		web.driver.get("$exchange.request.header.host/Sandbox")
		find(by.cssSelector('#pageTitle>h1')).text
		web.waitFor(By.cssSelector('#pageTitle>h1')) { assert it.text == 'Sandbox' }
	}
	
	void async(String script, Object... args) {
			((JavascriptExecutor) web.driver).executeAsyncScript(script, args)
	}
	
	void script(String script, Object... args) {
			((JavascriptExecutor) web.driver).executeScript(script, args)
	}
}