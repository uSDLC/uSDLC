include 'Instrument'
remote = usdlc.RemoteComms.get('mock')

gwt /a mock device running/, ->

gwt /wait for a connection/, ->
  
gwt /send "(.*)"/, (all, command) ->
  remote.fromUSDLC.put(command)
  
gwt /the device will disconnect/, ->
  response = String(remote.toUSDLC.take())
  throw "bad response - #{response}" if response isnt 'ok'
    
gwt /and wait for 2 seconds/, (all) ->
    