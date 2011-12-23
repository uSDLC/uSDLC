package usdlc.actor

import groovy.transform.AutoClone
import java.util.regex.Pattern
import usdlc.Log
import usdlc.Page
import usdlc.Store
import static usdlc.MimeTypes.mimeType
import static usdlc.config.Config.config

@AutoClone class SectionRunnerActor extends Actor {
	/**
	 * Override to run the section runner script.
	 */
	void run(Store script) {
		def user = exchange.request.user
		if (!user.authorised(script, 'run')) {
			reportError "$user.id is not authorised to execute on this page"
			return
		}

		rerun = (script.pathFromWebBase == 'last.sectionRunner')
		if (rerun) {
			if (lastRunner) {
				exchange.request.session.persist.lastSectionRunner =
					lastRunner
			} else {
				lastRunner = exchange.request.session.persist
						.lastSectionRunner
			}
		} else {
			def rq = exchange.request
			lastRunner = [
					url: rq.header.with { "http://$host$uri?$query" },
					page: rq.query.page,
					sections: rq.query.sections
			]
		}
		rerun = rerun || exchange.request.query.rerun
		if (rerun) {
			// dump command line and redirect output to server.
			println "$lastRunner.url&rerun=true"
			exchange.response.out = System.out
		}
		runSectionsOnPage(Store.base(lastRunner.page), lastRunner.sections)
	}

	static lastRunner
	boolean rerun
	/**
	 * Given a page reference and an optional list of sections, parse html and
	 * process each section looking for sub-pages to run and actors to run
	 * afterwards. If no sections are provided, all sections processed.
	 */
	void runSectionsOnPage(Store page, String sectionsToRunCsv = '') {
		currentPage = page
		Page html = new Page(page)
		def sections = html.sections
		if (sectionsToRunCsv) {
			sections = sections.select(
					'div#' + sectionsToRunCsv.replaceAll(/,/, ',div#'))
		}
		def linkSelector = 'a[action=page],a[action=runnable]'
		def links = sections.select(linkSelector)
		if (links.size()) {
			write """<html><head>
					<link type='text/css' rel='stylesheet'
						href='$config.urlBase/rt/outputFrame.css'>
				</head><body>
					<div id='output'>"""
			def actors = []
			def hrefs = [:]
			// Walk through each section specified and run linked pages
			links.each { link ->
				String href = link.attr('href')
				hrefs[href] = link
				def linkStore = page.rebase(href)
				switch (link.attr('action')) {
					case 'page':
						def clone = this.clone()
						clone.onScreen = false
						clone.runSectionsOnPage(linkStore)
						break
					case 'runnable':
						actors.push(linkStore)
						break
					default:
						break
				}
			}
			wrapOutput([
					'<div class="gray">',
					'</div>'
			]) { write "Page $currentPage.parent" }
			// afterwards run any actors in the referenced sections
			if (actors) runActorsOnPage(actors)
			// modify source with results of the runs for this page
			if (onScreen) {
				linkStates.each { href, state ->
					js("parent.usdlc.actorState('$href', '$state')")
					if (href in hrefs) {
						hrefs[href].attr('class', "usdlc sourceLink $state")
					}
				}
			}
			String text = html.select('body').html()
			def path = exchange.store.pathFromWebBase
			if (path.endsWith('.sectionRunner')) {
				exchange.store = Store.base(path[0..-15])
			}
			exchange.save(text)
			write '</div></body></html>'
		}
	}

	def onScreen = true
	def currentPage
	def linkStates = [:]
	/**
	 * Called when running an actor in-context by using Setup and Cleanup.
	 */
	void runActorsOnPage(List<Store> actors) {
		try {
			def base = Store.base(actors[0].parent)
			runFiles(~/^Setup\..*/, base)
			actors.each { Store actor -> runActor(actor) }
			runFiles(~/^Cleanup\..*/, base)
		} catch (AssertionError assertion) {
			reportException(assertion)
		} catch (Throwable throwable) {
			reportException(throwable)
			throwable.printStackTrace()
		} finally {
			context.each { key, value ->
				if (value?.metaClass?.respondsTo('close')) {
					actorState "finalise $key"
					try { value.close() } catch (throwable) {
						reportException(throwable)
						throwable.printStackTrace()
					}
					actorState = 'succeeded'
				}
			}
			resizeOutputFrame()
		}
	}

	void resizeOutputFrame() {
		js('parent.usdlc.resizeOutputFrame()')
	}
	/**
	 * Given a pattern, run all scripts that match it the base directory.
	 * Used to Setup and Cleanup scripts for a page
	 */
	void runFiles(Pattern pattern, Store base) {
		base.dir(pattern) { runActor(Store.base(it)) }
	}
	/**
	 * Give a reference to an actor, load it and run it - wrapping the output
	 * in HTML if needed.
	 */
	void runActor(Store actorStore) {
		currentActor = actorStore.pathFrom(currentPage)
		actorState = 'running'
		def actor = load(actorStore)
		if (actor) {
			wrapOutput(currentActor) {actor.run(context)}
			actorState = 'succeeded'
		}
	}
	/**
	 * If the actor is on the currently displayed page, show the state in the
	 * browser. If on another page, display as part of the output
	 * In all cases update the state in the page for persistence.
	 */
	void setActorState(String to) {
		linkStates[currentActor] = to
		String elapsed = timer
		wrapOutput([
				'<span class="gray" style="padding-left:2em;">',
				'</span><br>'
		]) { write "$currentActor: $to $elapsed" }
		Log.csv("${currentPage}.timings")(
				"$currentActor,$to,$timer.elapsed,$elapsed")
	}

	String currentActor
	usdlc.Timer timer = new usdlc.Timer(
			title: 'in ', minimum: 100, autoReset: true)
	/**
	 * Special to run the script while wrapping the output for best effort.
	 */
	void runScript(Map binding) {
		wrapOutput(script.pathFromWebBase) { delegate.run(binding) }
	}
	/**
	 * If an exception is thrown we need to display the error in a
	 * user-friendly
	 * way and flag the actor as having failed it's job.
	 */
	def reportException(Throwable throwable) {
		def trace = throwable.stackTrace.find {
			it.toString() ==~ ~/\w+\.run\(.*/
		} ?: throwable.stackTrace.find {
			it.fileName && it.lineNumber > 0 &&
					!internalExceptions.matcher(it.className).find()
		}
		reportError {
			write throwable.message
			if (trace) write "\n\n${trace.toString()}"
		}
		actorState = 'failed'
	}
	/**
	 * If we detect an error that is not an exception,
	 * we had better still treat
	 * it as something bad to tell the user.
	 */
	def reportError(String text) {
		reportError({write text})
	}

	def reportError(Closure writeContent) {
		wrapOutput([
				'<pre style="color:red;padding-left:4em;">',
				'</pre>'
		], writeContent)
		resizeOutputFrame()
	}
	/**
	 * Inject javascript into the output stream
	 */
	void js(String content) {
		wrapOutput(['<script>', '</script>']) { write content.toString() }
	}
	/**
	 * Wrap data of defined mime-type as if it were to be included in a
	 * HTML file
	 */
	def wrapOutput(String fileName, Closure closure) {
		def type = mimeType(fileName)
		wrapOutput(mimeTypeWrappers.get(type, ['<!--', '-->']), closure)
	}
	/**
	 * Wrap data in a HTML tag
	 */
	def wrapOutput(List<String> wrapper, Closure closure) {
		write wrapper[0]
		try {
			closure()
		} finally {
			write wrapper[1]
		}
	}

	static mimeTypeWrappers = [
			'text/html': ['<pre style="padding-left:4em;">', '\n</pre>'],
			'application/javascript': ['<script>', '</script>'],
			'text/plain': ['<pre style="padding-left:4em;">', '\n</pre>']]
	/**
	 * Write text to response output stream
	 */
	def write(String text) {
		if (!rerun) exchange.response.write text
	}
}
