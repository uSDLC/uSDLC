package uSDLC.Actors.Java;

import net.usdlc.BrowserBuilder;
import net.usdlc.Environment;

import java.util.Map;

class Consumer {
	Consumer(BrowserBuilder doc) {
		Map env = Environment.data();
		doc.tag("div").text("Input was").
				tag("div").text(env.get("in")).end().end();
	}
}
