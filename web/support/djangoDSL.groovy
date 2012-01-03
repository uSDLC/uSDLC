application = 'default'

startapp = { String app = application -> }
sqlclear = { String app = application -> if (!production) {}}
syncdb = { '--noinput' }
settings = { Map settings -> }
