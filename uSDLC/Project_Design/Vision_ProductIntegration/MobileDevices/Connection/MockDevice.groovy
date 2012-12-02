package usdlc;
import net.usdlc.android.SocketListener

// SocketListener is the class included in real Android and Blackberry units
public class MockSimpleCommandListener extends SocketListener {
	public MockSimpleCommandListener() {
		super("http://127.0.0.1:9002/usdlc/instrument?name=mock");
	}

	@Override
	public String process(String code) {
		println "Device processing '$code'"
		if (code.equals("exit")) {
			//terminate();  // normally terminate to stop - but then no return
			return "ok\r\nexit";    // normally ok, exit is to tell server
		}
		return "error $code";
	}
}

println "Starting Mock Command Listener..."
cl = new MockSimpleCommandListener()
cl.start()

// Before we kick it off we need a listener inside uSDLC
new InstrumentationListener(9002).run()

println "...Terminating Mock Command Listener"
