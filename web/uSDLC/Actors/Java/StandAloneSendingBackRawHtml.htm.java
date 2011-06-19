package uSDLC.Actors.Java;

import usdlc.BrowserBuilder;
import usdlc.Environment;

class StandAloneSendingBackRawHtml {
	StandAloneSendingBackRawHtml(BrowserBuilder doc) {
		Environment env = Environment.session();
		doc.text("<div>More text in it's own block</div><div>Referrer: ").
				text(env.propertyMissing("referer")).text("</div>");
	}
}
