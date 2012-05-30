package usdlc.actor

import usdlc.Store

/**
 * uSDLC supports actor and filters. This filter provides HTML templating. By
 * default it runs templates/pasteList.html.groovy that provides header,
 * body and scrolling elements.
 *
 * User: Paul Marrington
 * Date: 23/11/10
 * Time: 6:22 PM
 */
class HtmlActor extends Actor {
	/**
	 Use to generate HTML to display on the screen.
	 */
	void run(Store script) {
		Map query = exchange.request.query
		switch (query.action) {
			case 'cut':
				transfer { Store store, String to -> store.moveTo(to) }
				break
			case 'copy':
				transfer { Store store, String to -> store.copyTo(to) }
				break
			case 'paste':
				paste()
				break
			default:
				exchange.response.write exchange.store.read()
				exchange.response.write bootstrapJs
				break
		}
	}
	static bootstrapJs =
		"<script src='/rt/js/bootstrap.coffeescript'></script>".bytes

	private void transfer(Closure transfer) {
		Store clipboard = Store.base('.store/clipboard')
		String targetName = clipboard.uniquePath(exchange.request.query['title'])
		exchange.request.query['dependents'].tokenize(', ').each {
			String dependent ->
			transfer(exchange.store.rebase(dependent),
					".store/clipboard/$targetName");
		}
		def contents = exchange.request.body().bytes
		clipboard.base("$targetName/Section.html").write(contents)
		String title = exchange.request.query['title'].replaceAll(/'/, /\\'/)
		def action = exchange.request.query['action']
		out.println "usdlc.${action}SectionSuccessful('$title','/$targetName')"
	}

	private void paste() {
		def target = exchange.store.parent
		def from = Store.base(".store/clipboard/${exchange.request.query['from']}")
		from.dir(~/[^.]*/) { String dir -> from.rebase(dir).moveTo(target) }
		out.println new String(Store.base(
				"${exchange.request.query['from']}/Section.html").read())
		from.rmdir()
	}
}
