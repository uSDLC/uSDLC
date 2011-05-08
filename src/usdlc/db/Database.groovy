/*
 * Copyright 2011 Paul Marrington for http://usdlc.net
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

import groovy.sql.Sql
import usdlc.Config
import usdlc.Environment

/**
 * User: Paul Marrington
 * Date: 7/05/11
 * Time: 5:16 PM
 */
// todo: database pages doco
class Database {
	/**
	 * Save a connection to the uSDLC database in the environment (if there is not one ready already. It can
	 * be used as Environment.db. When using the gsql actor, it can be made the default with database(db).
	 */
	static connection() {
		def env = Environment.data()
		env.ensure.db = Gsql
		env.db.database(Config.usdlcDatabase)
		// todo: not versioning core yet
		if (!version) { version = version("classpath:usdlc/db/Core") }
	}

	static version = 0
	/**
	 * Close the open uSDLC database
	 */
	static close() { Environment.db.close() }
	/**
	 * The first time in a static run that we access a group of related tables we check the code generated version against that in the current database. If they differ, ask caller to migrate the data.
	 *
	 * static version = db.version("classpath:usdlc/db/Core")
	 *
	 * @param tableGroup Name of group of related tables that use the same version
	 * @param migrate Call to create tables or migrate old to new form. One parameter is the old version. If it is zero, the tables do not exist and have to be created but no migration needed. Must
	 * return true if migration was successful so that the new version can be recorded. This parameter is optional and the code will migrate using scripts of the form $tableGroup.$version.sql to move up migration steps one at a time.
	 * @return true if no migration needed or migration succeeded
	 */
	static version(tableGroup) {
		return Environment.db.version(tableGroup, Config.tableVersions[tableGroup])
	}
}

class Gsql {
	@Delegate Sql sql

	def env = Environment.data()
	/**
	 * Open a database using a typical jdbc string - if it is not already open
	 *      database("jdbc:h2:~/testSqlActor;CIPHER=AES", user : "me", password : "adfs")
	 * @param url jdbc url address - as in "jdbc:h2:/data/test;AUTO_SERVER=TRUE"
	 * @param properties - java DriverManager properties - user, password, driverClassName
	 * @return instance of the database connection - to maintain allow swapping
	 */
	public database(Map properties, String url) {
		def key = "$url::$Environment.cookies.session"
		if (!connections?."$key"?.connection || connections[key].connection.isClosed()) {
			connections[key] = Sql.newInstance(url, properties as Properties)
		}
		return sql = connections[key]
	}
	/**
	 * Simpler SQL databases only need the connection string - or nothing for an in-memory instance.
	 * @param url jdbc url address - as in "jdbc:h2:/data/test;AUTO_SERVER=TRUE"
	 * @return instance of the database connection - to maintain allow swapping
	 */
	public database(String url = 'jdbc:h2:mem:test:db1') { database([:], url) }
	/**
	 * Switch database connections to another known one...
	 * @param to database connection
	 * @return instance of the database connection - to maintain allow swapping
	 */
	public database(Sql to) { sql = to }
	/**
	 * Switch database connections to another known one...
	 * @param to database connection
	 * @return instance of the database connection - to maintain allow swapping
	 */
	public database(Gsql to) { sql = to.sql }
	/**
	 * Set the trace level written to the log
	 * @param level - 0=off, 1=error, 2=info, 3=debug
	 */
	public trace(level = 3) { sql.execute("set TRACE_LEVEL " + level) }
	/**
	 * The first time in a static run that we access a group of related tables we check the code generated version against that in the current database. If they differ, ask caller to migrate the data.
	 *
	 * static version = db.version("classpath:usdlc/db/Core")
	 *
	 * @param tableGroup Name of group of related tables that use the same version
	 * @param migrate Call to create tables or migrate old to new form. One parameter is the old version. If it is zero, the tables do not exist and have to be created but no migration needed. Must
	 * return true if migration was successful so that the new version can be recorded. This parameter is optional and the code will migrate using scripts of the form $tableGroup.$version.sql to move up migration steps one at a time.
	 * @param targetVersion - version to move to
	 * @return true if no migration needed or migration succeeded
	 */
	public version(tableGroup, targetVersion, migrate = migrateByScript) {
		def dbVersion = 0
		Environment.db.with {
			try { firstRow("select version from versions where tableGroup = $tableGroup").version } catch (e) {
				execute "runScript from 'classpath:usdlc/db/Core.001.sql'"
			}
			if (!dbVersion) {
				try { executeUpdate "insert into versions values($tableGroup,0)" } catch (e) {}
				dbVersion = 0
			}
			while (dbVersion != targetVersion) {
				dbVersion += 1
				if (!migrate(tableGroup, dbVersion)) { return /* failure */ }
				executeUpdate "update versions set version = $dbVersion where tableGroup = $tableGroup"
			}
		}
		return dbVersion
	}
	/**
	 * Can be called by migration closures to find a script of the form "$tableGroup.$toVersion(3 digits).sql" and migrate it.
	 */
	public migrateByScript = { tableGroup, toVersion ->
		try {
			def script = "$tableGroup${String.format('%03d', toVersion)}.sql"
			System.err.print "Migrating $script ..."
			def timer = new Timer()
			sql.execute "runscript from $script" as String
			System.err.println " done in ${timer}"
		} catch (IOException ioe) {/*probably doesn't exist*/}
	}

	static connections = [:]
}
