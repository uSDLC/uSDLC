verbose = false
database = 'default'
cleanup = (database) ->  # removes old sessions from database
compilemessages = (locale = 'all') -> # compiles local specific messages
createcachetable = (database) -> # cache table used internally bu Django
dbshell = (database) -> # db specific command line client
diffsettings = -> # setting variation from standard
dumpdata = (application = '') -> # dump *all* of the data for in app db
flush = (database) -> # remove all data (but not schema) from db
inspectdb = (database) -> # create models from existing schema
loaddata = (database, fixture...) -> # loads database from serialised
makemessages = (locale = 'all') -> # extract strings from source
runserver = (address = 'localhost:8000') -> # start a local dev server
shell = -> # pthon interactive shell with django loaded
sql -> (application) -> # Prints the CREATE TABLE SQL statements for the given app name
sqlall -> (application) -> # Prints the CREATE and initial-data for the given app
sqlclear -> (application) -> # Prints the DROP TABLE SQL statements for the given app
sqlcustom -> (application) -> # Prints the custom SQL statements for the given app name
sqlflush -> (application) -> # Prints the SQL that would be executed for a flush
sqlindex -> (application) -> # Prints the CREATE INDEX SQL statements for the given app
sqlsequencereset (application) -> # rints the SQL for resetting sequences for the app
startapp -> (application) -> # Creates a Django app dir structure in the current dir
startproject -> (project) -> # Creates a Django project dir structure in the current dir
syncdb -> (database)-> # Creates new tables for all apps in INSTALLED_APPS
test (application) -> # Runs tests for all installed models
testserver (fixtures...) -> # Runs a Django development server using fixture data
validate (database) -> # Validates all installed models (INSTALLED_APPS setting)