include 'Instrument'
remote = usdlc.RemoteComms.get('webclient')
gwt /A browser open at "(.*)"/, (all, url) ->
  usdlc.Desktop.openURL(url)
gwt /I send "(.*)"/, (all, code) ->
  remote.fromUSDLC.put(code)
gwt /I receive an "(.*)" result/, (all, result) ->
  response = String(remote.toUSDLC.take())
  throw "bad response - #{response}" if response isnt result