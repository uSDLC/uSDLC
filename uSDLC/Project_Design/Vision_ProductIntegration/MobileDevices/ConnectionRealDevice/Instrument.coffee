include 'Instrument'
remote = usdlc.RemoteComms.get('handset')
remote.clear()

gwt /A connected mobile device/, (all) ->
gwt /I send "(.*)"/, (all, code) ->
  remote.fromUSDLC.put(code)
gwt /I receive an "(.*)" result/, (all, result) ->
  response = String(remote.toUSDLC.take())
  throw "bad response - #{response}" if response isnt result