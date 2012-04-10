package usdlc.drivers

import org.openqa.selenium.By
import org.openqa.selenium.Capabilities
import org.openqa.selenium.WebElement
import org.openqa.selenium.support.ui.Select

import java.util.regex.Pattern

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
	void setDriver(String name = config.webDriver) {
		try {
			this.@driver?.quit()
		} catch (e) {
		}
		driverName = name
		if (name in config.webDrivers) {
			name = config.webDrivers[name]
		}
//		baseElement = _driver = ClassLoader.systemClassLoader.loadClass(name).
//				newInstance() as org.openqa.selenium.WebDriver
		baseElement = _driver = WebDriver.classLoader.loadClass(name).
				newInstance() as org.openqa.selenium.WebDriver
	}
	/**
	 * Wait for something to become available - for a given patience.
	 */
	def waitFor(Closure elementFinder) {
		def countdown = timeout * 2, elements
		while (!(elements = elementFinder()) && countdown) {
			sleep(200)
			countdown--
		}
		return elements
	}
	/**
	 * Wait for a web element to become available - for a given patience.
	 */
	WebElement waitFor(By target, Closure action) {
		def elements = waitFor { findElement(target) }
		assert elements?.size(), "No element '$target'"
		return action(currentElements = elements)
	}
	/**
	 * Wait for a web element to become available - for a given patience.
	 * id, name, link text, css selector, xpath, class name,
	 * tag name or partial link text.
	 * Link text is a path with links separated by ->.
	 */
	def waitFor(String targets, Closure action) {
		def elements =[driver]
		targets.split(/\s+->\s+/).each { target ->
			elements = findElements(target){[]}
			assert elements?.size(), "No element '$target'"
		}
//		if (elements.size() == 1) {
//			switch (elements[0].tagName) {
//				case 'select':
//					elements = elements[0].findElement(By.tagName('option'))
//					break;
//				// todo: case 'label': return the inner form entry field
//				// todo: case 'th': return list of elements in row or column
//				// todo: case 'td': return list of elements in row if first
//				// todo: case 'tr': return list of elements in column if first
//				// todo: case 'input', type='radio', same behavior as select
//			}
//		}
		return action(elements)
	}
	/**
	 * Wait for and return a specified element
	 */
	def waitFor(String targets) {waitFor(targets){it}}

	private findElements(By by) {
		try {
			return baseElement.findElements(by)
		} catch(e) {
			return []
		}
	}
	private findElements(String target, Closure moreChecks) {
		By id = By.id(target)
		By name = By.name(target)
		By linkText = By.linkText(target)
		By cssSelector = By.cssSelector(target)
		By xpath = By.xpath(target)
		By className = By.className(target.replaceAll(/\s/, '-'))
		By tagName = By.tagName(target)
		By plt = By.partialLinkText(target)
		return waitFor {
			findElements(id) ?:
				findElements(name) ?:
					findElements(linkText) ?:
						findElements(cssSelector) ?:
							findElements(xpath) ?:
								findElements(className) ?:
									findElements(tagName) ?:
										findElements(plt) ?:
											moreChecks(target)
		}
	}
	private findElementsByVisualClues(target) {
		// start with the obvious - <label>
		def elements = findElements(By.cssSelector('label'))
		return []
	}
	def findFormElements(target) {
		findElements(target) {findElementsByVisualClues(it)}
	}
	/**
	 * Go from A to B by waiting and clicking links
	 */
	void click(String selector) { waitFor (selector) {it.each {it.click()} } }
	/**
	 * Look at a pattern and decide if it is a regex or just a string action
	 * based on /regex/.
	 */
	Closure comparator(pattern, stringComparator = {it == pattern}) {
		pattern = pattern.toString()
		def cmp
		if (pattern.startsWith('/') && pattern.endsWith('/')) {
			def regex = Pattern.compile(pattern[1..-2])
			cmp = { text -> regex.matcher(text).matches() }
		} else {
			cmp = stringComparator
		}
		return { element -> values(element).find{cmp(it)} }
	}
	/**
	 * Allows chaining of checks against the same element.
	 */
	void check(pattern) {
		def cp = comparator(pattern)
		currentElements.each {
			assert cp(it), "check '${values(it)}' is not $pattern"
		}
	}
	/**
	 * Retrieve the perceived value of an element - being the value attribute
	 * if it exists, otherwise the text.
	 */
	private values(element) {
		switch (element.tagName) {
			case 'input':
				switch (element.getAttribute('type')) {
					case 'checkbox':
						return [element.isSelected()]
					case 'radio':
						if (element.isSelected()) {
							return [element.getAttribute('value')]
						}
						return []
					default:
						def result = []
						def v = element.getAttribute('value')
						if (v) result << v
						if (element.text) result << element.text
						return result
				}
			case 'select':
				return new Select(element).allSelectedOptions.collect {
					[it.text, it.getAttribute('value')]
				}.flatten()
				return [element.text]
			default:
				return [element.text]
		}
	}
	/*
	 * Go through a list of of web elements and
	 * return a list of counts for each pattern
	 */
	def match(elements, patterns) {
		def matches = []
		patterns.flatten().each {
			def cp = comparator(it)
			matches.push(elements.count {cp(it)})
		}
		return matches
	}
	/**
	 * Wait for a selector to become available and check the innerText for
	 * the existence of all patterns in the list - and only them.
	 */
	void checkOnly(selector, Iterable patterns) {
		waitFor(selector) { elements ->
			assert elements.size() == patterns.size(), selector
			match(elements, patterns).each {
				assert it == 1, \
				 "check only '$selector' ($elements.text) is not $patterns\n"
			}
		}
	}
	/**
	 * Wait for a selector to become available and check the innerText for
	 * the existence of all patterns in the list
	 */
	void checkAll(selector, Iterable patterns) {
		waitFor(selector) { elements ->
			match(elements, patterns).each {
				assert it >= 1, \
				 "check all: $selector ($elements.text) is not $patterns\n"
			}
		}
	}
	/**
	 * Wait for a selector to become available and check the innerText for
	 * the existence of at least one of the regex patterns in the list
	 */
	void checkSome(selector, Iterable patterns) {
		waitFor(selector) { elements ->
			assert match(elements, patterns).find { it >= 1}, \
				 "check some: $selector ($elements.text) is not $patterns\n"
		}
	}
	/**
	 * Wait for a selector to become available and check the innerHTML for
	 * to make sure none of the regex patterns in the list are found
	 */
	void checkNone(selector, Iterable patterns) {
		waitFor(selector) { elements ->
			assert !match(elements, patterns).count { it}, \
				 "check none: $selector ($elements.text) is not $patterns\n"
		}
	}
	/**
	 * Wait for a selector to become available and check the innerHTML for
	 * to make sure none of the regex patterns in the list are found
	 */
	void checkEmpty(selector) {
		waitFor(selector) { elements ->
			assert match(elements, ['']).size() == elements.size()
		}
	}
	def currentElements = null, _baseElement = null
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
	void with(element, Closure actions) {
		def before = baseElement
		baseElement = element
		try { actions() } finally { baseElement = before }
	}

	def findClosest(tag, near) {
		assert near.size() > 0, "find closes '$tag' to what?"
		near = near[0]
		def result = null
		with(near) {
			def found = findElements(By.tagName(tag))
			if (found.size() > 0) {
				result = found[0]
			}
		}
		if (!result) {
			while (near) {
				if (near.tagName == tag) {
					result = near
					break
				}
				near = near.findElement(By.xpath('..'))
				if (near.tagName == 'body') break
			}
		}
		return result ?: near
	}

	@SuppressWarnings(["GroovyOverlyComplexMethod",
	"GroovyMethodWithMoreThanThreeNegations"])
	void enter(fields) {
		with(findClosest('form', waitFor(fields?.form ?: 'form'))) {
			fields.each { name, value ->
				try {
				if (name == 'form') return
				def elements = findFormElements(name)
				assert elements.size(), "No form field '$name'"
				def field = elements[0]
				switch (field.tagName) {
					case 'input':
					case 'textarea':
						switch (field.getAttribute('type')) {
							case 'button':
							case 'image':
							case 'reset':
								field.click()
								break;
							case 'checkbox':
								if (!field.isSelected() != !value) {
									field.click()
								}
								break;
							case 'radio':
								field = elements.find {
									it.getAttribute('value') == value
								}
								assert field, "No radio $name is $value"
								field.click()
								break;
							default:
								field.clear()
								field.sendKeys(value)
								break
						}
						break
					case 'select':
						select(field, value)
						break
					default:
						assert false, "No entry for tag '$field.tagName'"
				}
				} catch (e) {
					throw new RuntimeException(
							"Error entering input field '$name'", e)
				}
			}
		}
	}
	def select(field, String value) { select(field, [value]) }
	def select(field, Iterable values) {
		def select = new Select(field)
		select.allSelectedOptions.each {
			select.deselectByVisibleText(it.text)
		}
		values.each {
			select.selectByValue(it)
			select.selectByVisibleText(it)
		}
	}
}
