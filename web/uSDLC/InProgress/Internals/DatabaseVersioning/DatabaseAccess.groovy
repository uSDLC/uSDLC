package usdlc.test
import usdlc.db.Database

class DatabaseAccess {
    static database = new Database(name:'usdlc-database-test', version: 1)

    def inc() {
	connection { db ->
	    def value = db.firstRow('select * from usdlcdatabasetest')?.VALUE
	    if (value) {
		value += 1
		db.executeUpdate("update usdlcdatabasetest set value=$value")
	    } else {
		value = 1
		db.executeUpdate("insert into usdlcdatabasetest values(1)")
	    }
	}
    }	    
}