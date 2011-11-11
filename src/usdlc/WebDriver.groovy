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
import static usdlc.Config.config

import org.openqa.selenium.Capabilities
import org.openqa.selenium.WebElement
import org.openqa.selenium.By
import org.openqa.selenium.support.ui.ExpectedCondition
import org.openqa.selenium.support.ui.WebDriverWait

class WebDriver {
	String driverName = config.webDriver
	org.openqa.selenium.WebDriver driver
	Capabilities capabilities
	int timeout = 10
	org.openqa.selenium.WebDriver getDriver() {
		if (! driver) {
			setDriver(driverName)
		}
		try {
			capabilities = driver.capabilities
		} catch (e) {
			setDriver(driverName)
		}
		driver
	}
	void load(url) {
		try {
			getDriver().get(url)
		} catch (exception) {
			exception.printStackTrace()
			setDriver(driverName)
			driver.get(url)
		}
	}
	/**
	 * Driver can be named from configuration (chrome, firefox, ie or htmlunit) 
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
		driver = Class.forName(name).newInstance()
	}
	/**
	 * Wait for something to become available - for a given patience.
	 */
	WebElement waitFor(Closure closure) {
		def countdown = timeout * 2
		WebElement result
		while (!(result = closure()) && countdown) {
			sleep(500)
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
				return driver.findElement(target)
			} catch (nsee) {
				return null
			}
		}
		assert result, "No element $target"
		action result
	}
	/**
	 * Wait for a web element to become available - for a given patience.
	 * id, name, link text, css selector, xpath, class name, tag name or partial link text.
	 * Link text is a path with links separated by ->.
	 */
	WebElement waitFor(String targets, Closure action) {
		def result
		targets.split(/\s+->\s+/).each { target ->
			By id = By.id(target)
			By name = By.name(target)
			By linkText = By.linkText(target)
			By cssSelector = By.cssSelector(target)
			By xpath = By.xpath(target)
			By className = By.className(target.replaceAll(/\s/, '-'))
			By tagName = By.tagName(target)
			By partialLinkText = By.partialLinkText(target)
			result = waitFor {
				return findElement(id) ?: findElement(name) ?:
				findElement(linkText) ?: findElement(cssSelector) ?:
				findElement(xpath) ?: findElement(className) ?:
				findElement(tagName) ?: findElement(partialLinkText)
			}
			assert result, "No element $target"
			result = action result
		}
		result
	}
	/**
	 * Go from A to B by waiting and clicking links
	 */
	WebElement click(String targets) {
		waitFor(targets) { it.click() }
	}
	WebElement findElement(By by) {
		try {
			return driver.findElement(by)
		} catch (nsee) {
			return null
		}
	}
}
