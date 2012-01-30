package usdlc.dsl
application = 'default'

startapp = { String app = application -> }
sqlclear = { String app = application -> if (!production) {print 'todo'}}
syncdb = { '--noinput' }
settings = { Map settings -> }
