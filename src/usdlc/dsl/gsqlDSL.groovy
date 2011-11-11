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
package usdlc.dsl

defaultDatabase = usdlc.db.Database.usdlcDatabase
def connection(String dbName) {
	connection(dbName) {}
}
def connection(Closure sqlCommands) {
	connection defaultDatabase, sqlCommands	
}
def connection(String dbName, Closure sqlCommands) {
	defaultDatabase = dbName
	usdlc.db.Database.connection dbName, sqlCommands 
}

database = this.&connection
select = { sql, actions = null -> database { select(sql, actions) } }
first = { sql -> database { first(sql) } }

