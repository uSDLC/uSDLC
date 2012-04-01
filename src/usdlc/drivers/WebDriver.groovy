package usdlc.drivers

import org.openqa.selenium.By
import org.openqa.selenium.Capabilities
import org.openqa.selenium.WebElement
import static usdlc.config.Config.config

class WebDriver {
	String driverName = config.webDriver
	org.openqa.selenium.WebDriver _driver
	Capabilities capabilities
	int timeout = 10

	org.openqa.selenium.WebDriver getDriver() {
		if (!_driver) { driver = driverName }
		try {
			capabilities = _driver.capabilities
		} catch (e) { driver = driverName }
		return _driver
	}

	void load(url) {
		try {
			driver.get(url)
			baseElement = _driver
		} catch (exception) {
			exception.printStackTrace()
			driver = driverName
			_driver.get(url)
			baseElement = _driver
		}
	}
	/**
	 * Driver can be named from configuration (chrome, firefox,
	 * ie or htmlunit)
	 * or be the fully qualified class name of a WebDriver.
	 */
	void setDriver(String name) {
		try {
			this.@driver?.quit()
		} catch (e) {
		}
		driverName = name
		if (name in config.webDrivers) {
			name = config.webDrivers[name]
		}
		baseElement = _driver = WebDriver.classLoader.loadClass(name).
				newInstance() as org.openqa.selenium.WebDriver
	}
	/**
	 * Wait for something to become available - for a given patience.
	 */
	WebElement waitFor(Closure elementFinder) {
		def countdown = timeout * 2
		WebElement result
		while (!(result = elementFinder()) && countdown) {
			sleep(200)
			countdown--
		}
		result
	}
	/**
	 * Wait for a web element to become available - for a given patience.
	 */
	WebElement waitFor(By target, Closure action) {
		def result = waitFor {
			try {
				return baseElement.findElement(target)
			} catch (nsee) {
				return null
			}
		}
		assert result, "No element $target"
		action result
	}
	/**
	 * Wait for a web element to become available - for a given patience.
	 * id, name, link text, css selector, xpath, class name,
	 * tag name or partial link text.
	 * Link text is a path with links separated by ->.
	 */
	WebElement waitFor(String targets, Closure action) {
		def element = driver
		targets.split(/\s+->\s+/).each { target ->
			element = findElement(target) {null}
			assert element, "No element $target"
		}
		return action(element[0])
	}
	/**
	 * Wait for and return a specified element
	 */
	WebElement waitFor(String targets) {waitFor(targets){it}}

	private WebElement findElement(String target, Closure moreChecks) {
		By id = By.id(target)
		By name = By.name(target)
		By linkText = By.linkText(target)
		By cssSelector = By.cssSelector(target)
		By xpath = By.xpath(target)
		By className = By.className(target.replaceAll(/\s/, '-'))
		By tagName = By.tagName(target)
		By plt = By.partialLinkText(target)
		return waitFor {
			baseElement.findElements(id) ?:
				baseElement.findElements(name) ?:
					baseElement.findElements(linkText) ?:
						baseElement.findElements(cssSelector) ?:
							baseElement.findElements(xpath) ?:
								baseElement.findElements(className) ?:
									baseElement.findElements(tagName) ?:
										baseElement.findElements(plt) ?:
											moreChecks(target)
		}
	}
	private findElementByVisualClues(target) {
		// start with the obvious - <label>
		def element = baseElement.findElements(By.cssSelector('label'))
	}
	def findFormElement(target) {
		findElement(target) {findElementByVisualClues(it)}
	}
	/**
	 * Go from A to B by waiting and clicking links
	 */
	WebElement click(String targets) {
		waitFor(targets) { it.click() }
	}
	/**
	 * Wait for a selector to become available and check the innerHTML for
	 * the existence of all regex patterns in the list
	 */
	void checkAll(selector, Iterable regexList) {
		check(selector) { element ->
			for (regex in regexList) { assert element.html =~ regex }
		}
	}
	/**
	 * Wait for a selector to become available and check the innerHTML for
	 * the existence of at least one of the regex patterns in the list
	 */
	void checkSome(selector, Iterable regexList) {
		check(selector) { element ->
			for (regex in regexList) { if (element.html =~ regex) return }
			assert false, regexList
		}
	}
	/**
	 * Wait for a selector to become available and check the innerHTML for
	 * to make sure none of the regex patterns in the list are found
	 */
	void checkNone(selector, Iterable regexList) {
		check(selector) { element ->
			for (regex in regexList) { assert !(element.html =~ regex) }
		}
	}
	/**
	 * Wait for a selector to become available and check the innerHTML for
	 * to make sure none of the regex patterns in the list are found
	 */
	void checkSelected(selector, Iterable regexList) {
		checkAll(selector, regexList.collect {
			"<option [^>]*selected=.*?>$it</option>"
		})
	}
	void checkSelected(selector, regex) {checkSelect(selector,[regex])}
	/**
	 * Wait for a selector to become available and check the innerHTML for
	 * a regex pattern
	 */
	void check(selector, against, Closure action) {
		web.waitFor(selector) { element ->
			currentElement = element
			action(element)
		}
	}
	/**
	 * Wait for a selector to become available and check the innerHTML for
	 * a regex pattern
	 */
	void check(selector, regex) { checkAll(selector,[regex]) }
	/**
	 * Allows chaining of checks against the same element.
	 */
	void check(regex) { assert currentElement.text =~ regex }
	def currentElement = null, _baseElement = null
	def setBaseElement(to) { _baseElement = to }
	def getBaseElement() {
		if (! _baseElement) _baseElement = driver
		return _baseElement
	}
	/**
	 * It is not uncommon to be searching for elements within another
	 * (as in input elements in a form). Any calls to a selector within
	 * this closure are restricted to the outer element. Can be nested.
	 */
	void with(target, Closure actions) {
		def before = baseElement
		baseElement = waitFor(target)
		try { actions() } finally { baseElement = before }
	}

	void enter(form, fields) {
		with(form) {
			fields.each { name, value ->
				def field = findFormElement(name)
				switch (field.tagName) {
					case 'input':
					case 'textarea':
						field.clear()
						field.sendKeys(value)
						break
					case 'select':
						def options = value as Set
						field.findElements(By.cssSelector('option')).each{
							if (it.getAttribute('selected')) it.click()
							if (it.text in options) it.click()
						}
				}
			}
		}
	}
}
