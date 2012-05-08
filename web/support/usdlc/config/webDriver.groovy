package usdlc.config

browserDriverList = 'chrome:firefox:ie:htmlunit'

webDrivers = [
		chrome: 'org.openqa.selenium.chrome.ChromeDriver',
		firefox: 'org.openqa.selenium.firefox.FirefoxDriver',
		ie: 'org.openqa.selenium.ie.InternetExplorerDriver',
		htmlunit: 'org.openqa.selenium.htmlunit.HtmlUnitDriver',
		iphone: 'org.openqa.selenium.iphone.IPhoneDriver',
		android: 'org.openqa.selenium.android.AndroidDriver',
]

screencast = [keys: "` F12"]

environments {
	standalone {
		binding.webDriver = 'chrome'
	}
	servlet {
		binding.webDriver = 'htmlunit'
	}
}
