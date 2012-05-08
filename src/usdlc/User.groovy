package usdlc

import static usdlc.config.Config.config
import org.jasypt.util.password.BasicPasswordEncryptor
import usdlc.drivers.Groovy

class User {
	def data, password, session, userPath
	/**
	 * Associate user and session
	 */
	static set(session) {
		def loaded = session.user
		def user = session.user User
		if (!loaded) {
			user.session = session
			user.logout()
		}
		return user
	}
	/**
	 * Once a user is authorised, init() is called to run all the associated
	 * scripts
	 */
	def load(userName) {
		data = [groups: [:], pages: [:]]
		isAdmin = false
		def userId = Store.camelCase(userName)
		password = 'unknown'
		scripts = ['/support/usdlc/authorise.groovy']
		def found = false
		Store.allProjectRoots.each {
			def projectPath = it.path
			def up = "$projectPath/Environment/Users/$userId"
			scripts << "$up/Login.groovy"
			def pwdStore = Store.base("$up/Password.txt")
			if (pwdStore.exists()) found = true
			def passwordStore = Store.base("$up/Password.txt")
			if (passwordStore.exists()) {
				userPath = up
				password = passwordStore.text
			}
		}
		if (found) {
			def groovy = new Groovy(user: session.user, session: session)
			groovy.scripts(scripts)
			return true
		}
		return false
	}
	/**
	 * Logging in means checking the password before allowing the user to
	 * continue.
	 */
	def login(userName, passwordEntered) {
		if (! load(userName)) return false
		try {
			if (checkPassword(passwordEntered)) { return true }
		} catch (e) { /* drop through to return to guest */ }
		logout()
		return false
	}
	def logout() { load(config.userId ?: 'Guest') }

	def changePassword(was, to) {
		if (checkPassword(was)) {
			password = to ? encryptPassword(to) : ''
			Store.base("$userPath/Password.txt").text = password
			return true
		}
		return false
	}
	def checkPassword(against) {
		if (!against && !password) return true
		return new BasicPasswordEncryptor().checkPassword(against, password)
	}
	/**
	 * When we change a password we will need to encrypt it
	 */
	def encryptPassword(password) {
		return new BasicPasswordEncryptor().encryptPassword(password)
	}
	/**
	 * Admin bypasses all authorisation
	 */
	boolean isAdmin = false
	/**
	 * caches actions (save raw read run) that this user is allowed to do on
	 * this page. Then check for authorisation.
	 *
	 * Action can be read raw write or run
	 */
	boolean authorised(Store store, String action = 'read') {
		if (isAdmin || !store.isHtml) return true
		def path = store.parent
		if (path.indexOf('/') == -1 && path != 'uSDLC') return true
		if (!data.pages[path]) {
			def csv = store.onParentPath {
				it.rebase('Groups.csv').ifExists()
			}
			if (csv) {
				def actions = ''
				csvRE.split(csv.text).each {
					def gact = data.groups[it]
					if (gact && actions.indexOf(gact) == -1) {
						actions += "$gact "
					}
				}
				data.pages[path] = actions ?: 'none'
			} else {
				data.pages[path] = 'none'
			}
		}
		return data.pages[path].indexOf(action) != -1
	}

	static csvRE = ~/\r*[\n,]/

	def propertyMissing(String name) { data[name] }

	def propertyMissing(String name, value) { data[name] = value }
}
