package usdlc

import usdlc.db.Database
/**
 * entry = session.entry // null if doesn't exists
 * entry = session.instance MyClass // creates new instance if needed
 * entry = session MyClass // shortcut creates new instance if needed
 * entry = session.entry { "create new entry" }   // sets first time only
 */
class Session {
	static Session load(String key) {
		boolean isNewSession = !key
		if (isNewSession) {
			long before = lastKey
			lastKey = System.currentTimeMillis()
			if (lastKey == before) lastKey++
			key = lastKey.toString()
		}
		if (!sessions.containsKey(key)) {
			sessions[key] = new Session(key: key, isNewSession: isNewSession)
			sessions[key].persist =
				new PersistedSession(session: sessions[key])
			sessions[key].entry = { String name, Closure creator ->
				Map instances = sessions[key].instances
				if (!instances.containsKey(name)) {
					instances[name] = creator(name)
					//noinspection GroovyEmptyCatchBlock
					try {
						instances[name].session = sessions[key]
					} catch (e) {}
				}
				instances[name]
			}
		}
		return sessions[key]
	}

	static Map<String, Map> sessions = [:]
	static long lastKey = 0

	def data = [
			instances: [:],
			created: System.currentTimeMillis()
	]

	Session(Map init) { if (init) data << init }
	/**
	 * Returns a property if it exists. If not call a method by that name to
	 * set the property (first time only). Otherwise, null.
	 */
	Object getProperty(String name) { return data[name] }
	/** We can set the property explicitly */
	void setProperty(String name, value) { data[name] = value }
	/** or as an instance of a defined class */
	def instance(Class c, Object[] args) {
		if (!hasInstance(c)) { data[c.name] = c.newInstance(args) }
		return data[c.name]
	}
	def hasInstance(Class c) { data[c.name] }
	/** or from a closure (only called if property does not exist */
	Object invokeMethod(String name, Object args) {
		if (data.containsKey(name)) return data[name]
		def argv = (Object[]) args
		if (argv.size()) {
			if (argv[0] instanceof Closure) {
				return data[name] = argv[0]()
			}
			if (argv[0] instanceof Class)
				return data[name] = argv[0].newInstance()
		}
		return data[name] = null
	}
}

class PersistedSession {
	def session

	def propertyMissing(String name) {
		Database.connection { db ->
			def sql = """select * from sessions
				where session=$session.key and key=$name"""
			db.sql.firstRow(sql)?.value
		}
	}

	def propertyMissing(String name, value) {
		Database.connection { db ->
			def sql = """update sessions set value=$value
						where session=$key and key=$name"""
			if (!db.sql.executeUpdate(sql)) {
				db.sql.executeUpdate(
						"insert into sessions values($key,$name,$value)")
			}
		}
	}
}
