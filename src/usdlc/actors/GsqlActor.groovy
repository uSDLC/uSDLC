package usdlc.actors

import groovy.sql.Sql
import usdlc.BrowserBuilder
import usdlc.Environment

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

/**
 * User: Paul Marrington
 * Date: 5/05/11
 * Time: 9:16 PM
 *
 * jdbc:h2:/data/test;AUTO_SERVER=TRUE
 * jdbc:h2:mem:test:db1;DB_CLOSE_DELAY=-1
 */
class GsqlActor extends GroovyActor {
	def bind() {
		binding.doc = BrowserBuilder.newInstance('text/text')
		ensure.gsql = GqlProcessor
		delegate = binding.gsql
		return this
	}
}

class GqlProcessor {
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
		def key = "$url::$Environment.query.session"
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

	static connections = [:]
}
