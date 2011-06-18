timer = new usdlc.Timer1("Test Timer1")
sleep 1200
assert "elapsed time is $timer" =~ /\d+ s$/
assert (timer.log(".log/TestTimer.log") / 1000) as int == 1

logFile = usdlc.Store.base("store/log/TestTimer.log")
assert logFile.exists()

logFile.delete()
