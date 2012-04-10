assert client.driver.driver, "No driver" # needed here to load capabilities
capabilities = client.driver.capabilities
assert capabilities, "Webdriver has no capabilities?"
print capabilities.platform
print capabilities.browserName
print capabilities.version
assert capabilities.isJavascriptEnabled()
client.driver.timeout = 10 # restore default timeout of 10 seconds