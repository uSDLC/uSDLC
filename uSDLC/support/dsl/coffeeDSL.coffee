globals = (-> (-> this)())()
usdlc = Packages.usdlc
groovy = Packages.groovy

print = (text) -> exchange.response.print "#{text}\n"
output = (text) -> exchange.response.print text
session = exchange.getRequest().session
include = run = dsl = (script) -> support.include script

assert = (test, msg) -> throw msg or 'assert failed' if not test

startTime = java.lang.System.currentTimeMillis()
timer = ->
	Math.floor((java.lang.System.currentTimeMillis() - startTime) / 1000)
sleep = (seconds) -> support.$sleep(seconds)

javaArray = (arrayList) ->
	arrayList.get(index) for index in [0...arrayList.size()]

javaMap = (javaMap) ->
  map = {}
  for key in javaMap.keySet().toArray()
    map[String(key)] = String(javaMap.get(key).toString())
  return map

# convert an array of Java strings to a Javascript array
strings = (array) -> String(string) for string in array

class Store
  absolutePath: -> @store.file.getCanonicalPath()
  constructor: (@path) -> @store = usdlc.Store.base(path)
  copyTo: (target) -> @store.copyTo(target)
  moveTo: (target) -> @store.moveTo(target)
  renameTo: (target) -> @store.renameTo(target)
  delete: -> @store.delete()
  dir: -> javaArray @store.dir()
  exists: -> @store.exists()
  purge: -> fs(entry).delete() for entry in @dir()
  unique: (name) -> @store.unique(name ? null)
  grep: (regex) -> javaArray @store.grep(regex)

fs = (path) -> new Store usdlc.Store.base path

dsl 'gwtDSL'
