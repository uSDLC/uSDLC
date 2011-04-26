package uSDLC.Actors.Java;

import usdlc.BrowserBuilder;
import usdlc.Environment;

import java.util.Map;

class StandAloneSendingBackRawHtml {
	StandAloneSendingBackRawHtml(BrowserBuilder doc) {
		Map env = Environment.data();
		doc.text("<div>More text in it's own block</div><div>Referrer: ").
				text(env.get("referer")).text("</div>");
	}
}
