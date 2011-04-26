package uSDLC.Actors.Java;

import usdlc.BrowserBuilder;
import usdlc.Environment;

import java.util.Map;

class Consumer {
	Consumer(BrowserBuilder doc) {
		Map env = Environment.data();
		doc.tag("div").text("Input was").
				tag("div").text(env.get("in")).end().end();
	}
}
