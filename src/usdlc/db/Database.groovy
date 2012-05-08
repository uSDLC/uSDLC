package usdlc.db

import groovy.sql.GroovyResultSet
import groovy.sql.Sql
import org.h2.tools.RunScript
import static usdlc.config.Config.config

class Database {
	def url, version = -1, tableGroup
	/**
	 * Instantiate a database with a map of named parameters:
	 *   name: name of database for versioning
	 *   url: jdbc url to database, defaults to h2 if no jdbc:
	 *   version: database version number. Not set turns off versioning.
	 */
	def Database(Map conf) {
		url = conf.url ?: 'usdlc'
		tableGroup = conf.name ?: conf.url
		if (conf.version) {
			version = conf.version
			connection {
				def dbVersion = 0
				try {
					dbVersion = firstRow("""select version from versions
						where tableGroup=$tableGroup""").version
				} catch (e) { }
				if (!dbVersion) {
					try {
						executeUpdate(
								"insert into versions values($tableGroup,0)")
					} catch (e) { e.printStackTrace() }
				}
				while (dbVersion != version) {
					runScript dbVersion += 1
					executeUpdate("""update versions
						set version = $dbVersion where tableGroup =
						$tableGroup""")
				}
			}
		}
	}
	/**
	 * All actions occur within a closure so that the database can be
	 * released.
	 */
	def connection(Closure actions) {
		Connection connection = null
		def result = null
		try {
			while (!connection?.active()) {
				// close off out-of-date one if exists
				connection?.sql?.close()
				synchronized (pool) {
					if (!pool[url]) {
						pool[url] = [new Connection(url)]
					}
					connection = pool[url].pop()
				}
			}
			// This magic allows actions to call database methods implicitly.
			actions.delegate = connection
			result = actions()
			connection.sql.commit()
		} catch (exception) {
			connection.sql.rollback()
			exception.printStackTrace()
		} finally {
			synchronized (pool) { pool[url] << connection }
		}
		return result
	}
	/** List of unused connections keyed on database they belong to  */
	final static def pool = [:]
	/** Fetch an in-memory database for testing or small temporary
	 * functions
	 */
	static memoryDb(name) { "jdbc:h2:mem:$name:$name" }

	static h2Db(name) { "jdbc:h2:.db/$name" }

	private runScript(toVersion) {
		toVersion = String.format('%03d', toVersion)
		def name = "database_update_${tableGroup}_${toVersion}.groovy"
		// todo: search projects for update script
	}

	class Connection {
		@Delegate Sql sql
		/** Open a simple database by URL or config name only  */
		def Connection(String name) { this([url: name]) }
		/** Open a database with parameters (i.e. credentials)  */
		def Connection(Map properties) {
			if (!properties.url.startsWith('jdbc:')) {
				properties.url = h2Db(properties.url)
			}
			sql = Sql.newInstance(properties as Properties)
		}
		/** See if connection can be used - 1 hour old max.  */
		boolean active() {
			sql.connection && (System.currentTimeMillis() - created) < 3600000
		}
		/** Close all open databases in the pools  */
		static void close() { pool.each { it.sql.connection?.close() } }

		long created = System.currentTimeMillis()
		/** Given a row from a csv, use the heading to return a list for
		 * create  */
		String headings(row) {
			def columns = []
			row.each { column, value -> columns << "$column as varchar(255)" }
			columns.join(', ')
		}
		/** Wrapper for SQL SELECT command  */
		def select(String sql, Closure actions = null) {
			actions ? eachRow("select $sql", actions) : rows("select $sql")
		}
		/** Wrapper for SQL SELECT command for one record  */
		def first(String sql) { firstRow("select $sql") }
		/** Run test as a set of commands separates by ; at end of line */
		def script(String sql) {
			sql.split(~/;\s*[\r\n]+/).each { executeUpdate(it) }
		}
		/**
		 * Can be called by migration closures to find a script of the form
		 * "$tableGroup.$toVersion(3 digits).sql" and migrate it.
		 */
		static migrateByScript = { String url, tableGroup, toVersion ->
			try {
				def script =
					"${tableGroup}.${String.format('%03d', toVersion)}.sql"
				System.err.print "Migrating $script ..."
				def timer = new Timer()
				runSqlScript url, script
				System.err.println " done in $timer"
			} catch (IOException ioe) { ioe.printStackTrace() }
		}
		/**
		 * Given a text file containing sql, run it against the specified
		 * database.
		 */
		static runSqlScript(String url, String script) {
			RunScript.execute(url, null, null, script, null, false)
		}
	}
}
