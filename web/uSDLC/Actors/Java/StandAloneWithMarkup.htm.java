package uSDLC.Actors.Java;

import usdlc.BrowserBuilder;

class StandAloneWithMarkup {
	StandAloneWithMarkup(BrowserBuilder doc) {
		doc.tag("div class='message'").tag("span").
				text("\n    Test Java Script\n").
				tag("script").text("usdlc.highlight('blue')");
		// no easy equivalent of include
		// include 'StandAloneSendingBackRawHtml.htm.groovy'
	}
}
