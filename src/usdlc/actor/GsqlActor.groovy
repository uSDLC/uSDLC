package usdlc.actor

import usdlc.db.Database

/**
 * User: Paul Marrington
 * Date: 5/05/11
 * Time: 9:16 PM
 *
 * jdbc:h2:/data/test;AUTO_SERVER=TRUE
 * jdbc:h2:mem:test:db1;DB_CLOSE_DELAY=-1
 */
class GsqlActor extends GroovyActor {
	Map dsl = [
			firstRow: { GString sql -> database.sql.firstRow(sql) },
			execute: { GString sql -> database.sql.execute(sql) },
			executeUpdate: { GString sql -> database.sql.executeUpdate(sql) },
	]

	Database database

	void run() {
		Database.connection { db ->
			database = db
			super.run()
		}
	}
}
