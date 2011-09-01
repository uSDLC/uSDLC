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
	/**
	 * Driver can be named from configuration (chrome, firefox, ie or htmlunit) or be the fully qualified class
	 * name of a WebDriver.
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
	WebElement waitFor(int timeout, Closure closure) {
		WebElement result
		timeout *= 2
		while (!(result = closure()) && timeout) {
			sleep(500)
			timeout--
		}
		result
	}
	/**
	 * Wait for a web element to become available - for a given patience.
	 */
	WebElement waitFor(By target, int timeout = 10) {
		def result = waitFor(timeout) {
			try {
				return driver.findElement(target)
			} catch (nsee) {
				return null
			}
		}
		assert result, "No element $target"
		result
	}
	/**
	 * Wait for a web element to become available - for a given patience.
	 */
	WebElement waitFor(String target, int timeout = 10) {
		By id = By.id(target)
		By name = By.name(target)
		By linkText = By.linkText(target)
		By cssSelector = By.cssSelector(target)
		By xpath = By.xpath(target)
		By className = By.className(target)
		By tagName = By.tagName(target)
		By partialLinkText = By.partialLinkText(target)
		def result = waitFor(timeout) {
			try {
				return driver.findElement(id) ?: driver.findElement(name) ?:
				driver.findElement(linkText) ?: driver.findElement(cssSelector) ?:
				driver.findElement(xpath) ?: driver.findElement(className) ?:
				driver.findElement(tagName) ?: driver.findElement(partialLinkText)
			} catch (nsee) {
				return null
			}
		}
		assert result, "No element $target"
		result
	}
}
