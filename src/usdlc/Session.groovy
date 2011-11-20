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

import usdlc.db.Database

class Session {
	static Map load(String key) {
		boolean isNewSession = ! key
		if (isNewSession) {
			long before = lastKey
			lastKey = System.currentTimeMillis()
			if (lastKey == before) lastKey++
			key = lastKey.toString()
		}
		if (!sessions.containsKey(key)) {
			sessions[key] = [
						key : key,
						isNewSession : isNewSession,
						created : System.currentTimeMillis(),
						instances : [:],
						instance : { Class ofClass ->
							def name = ofClass.name
							Map instances = sessions[key].instances
							if (! instances.containsKey(name)) {
								instances[name] = ofClass.newInstance()
								//noinspection GroovyEmptyCatchBlock
								try {
									instances[name].session = sessions[key]
								} catch (exception) {}
							}
							instances[name]
						},
					]
			sessions[key].session = sessions[key]
			sessions[key].persist = new Session(session:sessions[key])
		}
		return sessions[key]
	}
	static Map<String,Map> sessions = [:]
	static long lastKey = 0

	def session

	def propertyMissing(String name) {
		Database.connection { db ->
			def sql = "select * from sessions where session=$key and key=$name"
			db.sql.firstRow(sql)?.value
		}
	}

	def propertyMissing(String name, value) {
		Database.connection { db ->
			def sql = """update sessions set value=$value
						where session=$key and key=$name"""
			if (!db.sql.executeUpdate(sql)) {
				sql = "insert into sessions values($key,$name,$value)"
			}
		}
	}
}
