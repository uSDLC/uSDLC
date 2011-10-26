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
package usdlc.db

import groovy.sql.GroovyResultSet
import groovy.sql.Sql
import org.h2.tools.RunScript
import static usdlc.Config.config

class Database {
	Sql sql
	/**
	 * Connect to the default database
	 */
	static Database connection(Closure actions) {
		connection(usdlcDatabase, actions)
	}
	/**
	 * Connect to a database by name. The url for this name is in the config file.
	 */
	static Database connection(String database, Closure actions) {
		Database connection = null
		def result
		try {
			while (!connection?.active()) {
				connection?.sql?.close()  // close off out-of-date one if exists
				synchronized (pool) {
					if (!pool[database]) pool[database] = [new Database(database)]
					connection = pool[database].pop()
				}
			}
			result = actions(connection)
		} catch (exception) {
			exception.printStackTrace()
		} finally {
			synchronized (pool) { pool[database] << connection }
		}
		result
	}
	/**
	 * The first time in a static run that we access a group of related tables we 
	 * check the code generated version against that in the current database. If 
	 * they differ, ask caller to migrate the data.
	 *
	 * static version = db.version("classpath:usdlc/db/Core")
	 *
	 * @param tableGroup Name of group of related tables that use the same version
	 * @param migrate Call to create tables or migrate old to new form. One parameter 
	 * is the old version. If it is zero, the tables do not exist and have to be created
	 * but no migration needed. Must return true if migration was successful so that the 
	 * new version can be recorded. This parameter is optional and the code will migrate 
	 * using scripts of the form $tableGroup.$version.sql to move up migration steps 
	 * one at a time.
	 */
	static String version(String key, Closure migrate = migrateByScript) {
		String[] group = (config.tableVersions[key] as String)?.split(',')
		def dbVersion = 0
		if (group) {
			String url = config.databases[group[0]], tableGroup = group[1]
			int targetVersion = group[2].toInteger()
			connection(url) { Database db ->
				try {
					dbVersion = (db.sql.firstRow(
						"select version from versions where tableGroup = $tableGroup") 
					as GroovyResultSet)['version']
				} catch (e) {
					runSqlScript url, "${tableGroup}.001.sql"
				}
				if (!dbVersion) {
					try { 
						db.sql.executeUpdate "insert into versions values($tableGroup,0)"
					} catch (e) { e.printStackTrace() }
				}
				def toVersion = targetVersion ?: 1
				while (dbVersion != toVersion) {
					dbVersion += 1
					migrationNeeded = true
					if (!migrate(url, tableGroup, dbVersion)) { return /* failure */ }
					db.sql.executeUpdate(
						"update versions set version = $dbVersion where tableGroup = $tableGroup")
				}
			}
		}
		key
	}
	/**
	 * Can be called by migration closures to find a script of the form 
	 * "$tableGroup.$toVersion(3 digits).sql" and migrate it.
	 */
	static migrateByScript = { String url, tableGroup, toVersion ->
		try {
			def script = "${tableGroup}.${String.format('%03d', toVersion)}.sql"
			System.err.print "Migrating $script ..."
			def timer = new Timer()
			runSqlScript url, script
			System.err.println " done in $timer"
		} catch (IOException ioe) { ioe.printStackTrace() }
	}
	/**
	 * Given a text file containing sql, run it against the specified database.
	 */
	static runSqlScript(String url, String script) {
		RunScript.execute(url, null, null, script, null, false)
	}

	final static Map<String, List<Database>> pool = [:]

	private Database(String name) { this([url: config.databases[name]]) }

	private Database(Map properties) { sql = Sql.newInstance(properties as Properties) }
	/** See if connection can be used.      */
	boolean active() { sql.connection && (System.currentTimeMillis() - created) < 3600000 } // 1 hour
	static void close() { pool.each { it.sql.connection?.close() } }

	long created = System.currentTimeMillis()
	/** Check the base version and migrate if necessary.      */
	static String usdlcDatabase = version('usdlc-core')
}