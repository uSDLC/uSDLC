package usdlc

import org.jasypt.util.password.BasicPasswordEncryptor
import usdlc.drivers.Groovy

import static usdlc.config.Config.config
import java.util.logging.Logging

class User {
	def data, password, session, userPath, home, userName
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
		scripts = ['usdlc/support/usdlc/authorise.groovy']
		def found = false
		home = ''
		Store.projectRoots.each {
			def projectPath = it.path
			def up = "$projectPath/Environment/Users/$userId"
			def hd = Store.base("$up/Home")
			if (hd.exists()) home = hd.href
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
			this.userName = userName
			def groovy = new Groovy(user: session.user, session: session)
			groovy.scripts(scripts)
			return true
		}
		return false
	}
	/**
	 * Return a list of all users
	 */
	static list() {
		def users = [];
		Store.projectRoots.each {
			def userDir = it.rebase('Environment/Users')
			if (userDir.exists()) {
				userDir.file.eachDir { File file ->
					users << Store.decamel(file.name);
				}
			}
		}
		return users;
	}
	/**
	 * Logging in means checking the password before allowing the user to
	 * continue.
	 */
	def login(userName, passwordEntered) {
		try {
			if (load(userName) && checkPassword(passwordEntered)) {
				return true
			}
		} catch (e) { /* drop through to return to guest */ }
		logout()
		return false
	}
	def logout() {
		def userName = config.userId ?: 'Administrator'
		load(userName)
		return this
	}

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
		try {
			return new BasicPasswordEncryptor().
					checkPassword(against, password)
		} catch (ee) {
			return false
		}
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
		if (isAdmin) return true
		def isRead = isRead(action)
		if (!store.isHtml && isRead) return true
		def path = store.dir
		if (path == 'home' || path == '') {  // home page
			// only Admin can edit home page
			return isRead
		}
		if (path.indexOf('/') == -1) return true

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

	private isRead(action) {action == 'read' || action == 'raw'}

	def toHtml() {
		if (home) {
			return "<a href='$home' class='contentLink' initials='$initials' action='page'>$userName</a>"
		}
		return ''
	}

	def getInitials() {
		if (!data.initials) { data.initials = userName.replaceAll(~/[^A-Z]/, '') }
		return data.initials
	}
	def setInitials(to) {data.initials = to}

	static csvRE = ~/\r*[\n,]\s*/

	def propertyMissing(String name) { data[name] }

	def propertyMissing(String name, value) { data[name] = value }
}
