package uSDLC.Actors.Java;

import net.usdlc.BrowserBuilder;
import net.usdlc.Environment;

import java.util.Map;

class StandAloneWithMarkup {
	StandAloneWithMarkup(BrowserBuilder doc) {
		Map env = Environment.data();
		doc.tag("div class='message'").tag("span").
				text("\n    Test Java Script\n").
				tag("script").text("usdlc.highlight('blue')");
		// no easy equivalent of include
		// include 'StandAloneSendingBackRawHtml.htm.groovy'
	}
}
