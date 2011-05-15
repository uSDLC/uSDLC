timer = new usdlc.Timer("Test Timer")
sleep 1200
assert "elapsed time is $timer" =~ /\d+ s$/
assert (timer.log(".log/TestTimer.log") / 1000) as int == 1

logFile = usdlc.Store.root(".log/TestTimer.log")
assert logFile.exists()

logFile.delete()
