globals = (-> (-> this)())()
usdlc = Packages.usdlc
groovy = Packages.groovy

print = (text) -> exchange.response.print "#{text}\n"
output = (text) -> exchange.response.print text

session = exchange.getRequest().session
include = run = dsl = (script) -> support.include script
# measure running time at any point (in seconds)
startTime = java.lang.System.currentTimeMillis()
timer = ->
  Math.floor((java.lang.System.currentTimeMillis() - startTime) / 1000)
# sleep for a specific number of seconds
sleep = (seconds) -> support.$sleep(seconds)
# get a random integer within a range
random = (below) -> Math.floor Math.random() * below
# pick a random item from a list - removing and returning it
pick = (list) -> list.splice(random(list.length), 1)[0]
# Do absolutely nothing when required
do_nothing = ->
# swallow exceptions when they don't matter
swallow = (action) ->
  try
    result = action()
  catch error
    result = do_nothing()
  return result

javaArray = (arrayList) ->
  return ([]) if not arrayList
  return (arrayList.get(index) for index in [0...arrayList.size()]) ? ([])

#  try
#    array = arrayList.get(index) for index in [0...arrayList.size()]
#  catch error
#    array = []
#  return array

javaMap = (javaMap) ->
  map = {}
  try
    for key in javaMap.keySet().toArray()
      map[String(key)] = String(javaMap.get(key).toString())
  catch error
    return map
  return map

# convert an array of Java strings to a Javascript array
strings = (array) -> String(string) for string in array

class Store
  absolutePath: -> @store.file.getCanonicalPath()
  constructor: (@path) -> @store = usdlc.Store.base(path)
  copyTo: (target) -> @store.copyTo(target)
  delete: -> @store.delete()
  dir: (mask) -> strings javaArray swallow => @store.dir(mask ? '.*')
  exists: -> @store.exists()
  grep: (regex) -> javaArray @store.grep(regex)
  moveTo: (target) -> @store.moveTo(target)
  name: -> String @store.file.getName()
  purge: (mask) -> fs(entry).delete() for entry in @dir(mask)
  renameTo: (target) -> @store.renameTo(target) if @store.exists()
  text: -> @store.getText()
  unique: (name) -> @store.unique(name ? null)
  write: (contents) -> @store.setText(contents)

fs = (path) -> new Store usdlc.Store.base path

dsl 'gwtDSL'
