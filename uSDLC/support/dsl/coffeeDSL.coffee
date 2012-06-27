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
  delete: -> @store.delete()
  dir: (mask) -> javaArray @store.dir(mask ? '.*')
  exists: -> @store.exists()
  grep: (regex) -> javaArray @store.grep(regex)
  moveTo: (target) -> @store.moveTo(target)
  purge: (mask) -> fs(entry).delete() for entry in @dir(mask)
  renameTo: (target) -> @store.renameTo(target)
  text: -> @store.getText()
  unique: (name) -> @store.unique(name ? null)

fs = (path) -> new Store usdlc.Store.base path

dsl 'gwtDSL'
