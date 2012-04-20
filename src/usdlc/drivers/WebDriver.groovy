package usdlc.drivers

import org.openqa.selenium.By
import org.openqa.selenium.Capabilities
import org.openqa.selenium.WebElement
import org.openqa.selenium.support.ui.Select

import java.util.regex.Pattern

import static usdlc.config.Config.config
import org.mozilla.javascript.regexp.NativeRegExp

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
		def elements = [driver]
		targets.split(/\s+->\s+/).each { target ->
			elements = findElements(target)
			assert elements?.size(), "No element '$target'"
			elements = infer(elements)
		}
		return action(elements)
	}
	def infer(elements) {
		return elements.collect { element ->
			switch (element.tagName) {
				case 'label':
					return with(element) {
						findElements(By.xpath(
							'following-sibling::*')).find {
						(it.tagName in inputElementNameSet) ? it : null
					} } ?: element
				case 'caption':
					return findOutside(['table'], element)
//				case 'td':
//				case 'th':
				default: return element
			}
		}
	}
	/**
	 * Wait for and return a specified element
	 */
	def waitFor(String targets) {waitFor(targets) {it}}

	private findElements(By by) {
		try {
			return baseElement.findElements(by)
		} catch (e) {
			return []
		}
	}

	private findElements(String target) {
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
											findElementByContent(target) ?: []
		}
	}

	def findElementByContent(content) {
		return findByXpath("descendant::*[text()='$content']") ?:
			findByXpath("descendant::*[starts-with(text(),'$content')]") ?:
				findByXpath("descendant::*[contains(text(),'$content')]")
	}

	def findByXpath(xpath) {
		return findElements(By.xpath(xpath))
	}
	/**
	 * Go from A to B by waiting and clicking links
	 */
	void click(String selector) { waitFor(selector) {it.each {it.click()} } }
	/**
	 * Look at a pattern and decide if it is a regex or just a string action
	 * based on /regex/.
	 */
	Closure comparator(pattern, compare = {defaultCompare(it, pattern)}) {
		if (pattern instanceof Boolean) {
			return {it.isSelected()}
		}
		def cmp
		if (pattern instanceof NativeRegExp) {
			// doesn't handle /srch/gim
			def regex = Pattern.compile(pattern.toString()[1..-2])
			cmp = { text -> regex.matcher(text).matches() }
		} else {
			cmp = compare
		}
		return { element -> values(element).find {cmp(it)}}
	}
	private defaultCompare(left, right) {
		if (left == right) return true
		// for radio where true means we found the right value
		if (left && right instanceof Boolean && right) return true
		if (left.toString() == right.toString()) return true
		return false
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
			case 'table':
				return tableContents(element).flatten()
			case 'th':
			case 'td':
				def loc = cellLocation(element)
				if (loc.x == 0) {
					return tableContents(element)[loc.y][1..-1].text
				} else if (loc.y == 0) {
					return tableContents(element).collect {
						it[loc.x]
					}[1..-1].text
				} else {
					return [element.text]
				}
			case 'ul':
			case 'ol':
				return with(element) {findElements(By.tagName('li'))}
			default:
				return [element.text]
		}
	}

	private tableContents(element) {
		def result = []
		def table = findOutside(['table'], element)
		def rows = with(table) { findElements(By.tagName('tr')) }
		rows.each {
			result << with(it) {
				findElements(By.cssSelector('*')).findAll {
					(it.tagName in ['td','th']) ? it.text : null
				}
			}
		}
		return result
	}

	private cellLocation(element) {
		def x = with(element) {
			findElements(By.xpath('preceding-sibling::td')).size() +
					findElements(By.xpath('preceding-sibling::th')).size()
		}
		def y = with(findClosest(['tr'], [element])) {
			def y = findElements(By.xpath('preceding-sibling::tr')).size()
			if (findElements(By.xpath(
					'parent::tbody/preceding-sibling::thead')).size()) {
				y++ // count the header
			}
			return y
		}
		return [x: x, y: y]
	}
	/*
	 * Go through a list of of web elements and
	 * return a list of counts for each pattern
	 */

	def match(elements, patterns) {
		def matches = []
		patterns.flatten().each { pattern ->
			if (pattern instanceof Double && pattern == 1.0) pattern = 1
			def cp = comparator(pattern)
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
				assert it == 1, "check only '$selector' ($elements.text) is not $patterns"
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
				assert it >= 1, "check all: $selector ($elements.text) is not $patterns"
			}
		}
	}
	/**
	 * Wait for a selector to become available and check the innerText for
	 * the existence of at least one of the regex patterns in the list
	 */
	void checkSome(selector, Iterable patterns) {
		waitFor(selector) { elements ->
			assert match(elements, patterns).find {it >= 1}, "check some: $selector ($elements.text) is not $patterns"
		}
	}
	/**
	 * Wait for a selector to become available and check the innerHTML for
	 * to make sure none of the regex patterns in the list are found
	 */
	void checkNone(selector, Iterable patterns) {
		waitFor(selector) { elements ->
			assert !match(elements, patterns).count { it}, "check none: $selector ($elements.text) is not $patterns"
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
	/**
	 * Called by DSL to set an element that future calls use instead of
	 * document
	 */
	def resetBaseElement(String target) {
		_baseElement = driver
		baseElement = waitFor(target)[0]
	}

	def getBaseElement() {
		if (!_baseElement) _baseElement = driver
		return _baseElement
	}
	/**
	 * It is not uncommon to be searching for elements within another
	 * (as in input elements in a form). Any calls to a selector within
	 * this closure are restricted to the outer element. Can be nested.
	 */
	def with(element, Closure actions) {
		def before = baseElement
		baseElement = element
		try { return actions() } finally { baseElement = before }
	}

	def findClosest(tags, near) {
		near = near[0]
		tags = tags as Set
		if (near.tagName in tags) return near
		def element = findInside(tags, near) ?: findOutside(tags, near) ?:
			findInXpath(tags, near, 'following-sibling::*') ?:
				findInXpath(tags, near, 'preceding-sibling::*')
		assert element, "failed to find $tags near $near.tagName"
		return element
	}

	def findInside(tags, near) {
		return with(near) {
			tags.findResult { tag ->
				def found = findElements(By.tagName(tag))
				(found.size() > 0) ? found[0] : null
			}
		}
	}

	def findOutside(tags, near) {
		def result = null
		while (near && !result) {
			if (near.tagName in tags) {
				result = near
				break
			}
			near = near.findElement(By.xpath('..'))
			if (near.tagName == 'body') break
		}
		return result
	}

	def findInXpath(tags, near, xpath) {
		return near.findElements(By.xpath(xpath)).find {it.tagName in tags}
	}

	def findFormElement(target) {
		def elements = findElements(target)
		assert elements, "No form element for label '$target'"
		return findClosest(inputElementNameSet, elements)
	}
	def inputElementNameSet = ['input', 'textarea', 'select'] as Set

	@SuppressWarnings(["GroovyOverlyComplexMethod",
	"GroovyMethodWithMoreThanThreeNegations"])
	void enter(fields) {
		with(findClosest(['form'], waitFor(fields?.form ?: 'form'))) {
			fields.each { name, value ->
				try {
					if (name == 'form') return
					def field = findFormElement(name)
					assert field, "No form field '$name'"
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
									if (!(value instanceof Boolean)) {
										def rn = field.getAttribute('name')
										def rs = findElements(By.name(rn))
										field = rs.find {
											it.getAttribute('value') == value
										}
										assert field, """
											No radio $name is $value"""
									}
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
