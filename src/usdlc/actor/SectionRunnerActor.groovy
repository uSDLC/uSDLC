package usdlc.actor

import groovy.transform.AutoClone
import usdlc.Log
import usdlc.Page
import usdlc.Store
import usdlc.drivers.JavaScript

import java.util.regex.Pattern

import static usdlc.MimeTypes.mimeType
import com.google.common.base.CaseFormat
import javax.swing.text.html.HTML
import usdlc.CSV
import sun.jvm.hotspot.debugger.posix.elf.ELFSectionHeader
import usdlc.PageState

@AutoClone class SectionRunnerActor extends Actor {
	/**
	 * Override to run the section runner script from a UI command
	 */
	void run(Store script) {
		// Make sure scripts have a clean slate
		exchange.request.session.instance(JavaScript).globalScope = null

		if (exchange.request.query.mode == 'text') { textMode = true }
		if (exchange.request.query.action == 'rerun') {
			def bookmark = lastRunner.url
			if (!textMode) {
				bookmark = bookmark.replaceAll('&', '&amp;')
			}
			// dump command line and redirect output to server.
//			exchange.response.out = System.out
			write("$bookmark\n")
			runSectionsOnPage(Store.base(lastRunner.page), lastRunner.sections)
		} else {
			def user = exchange.request.user
			if (!user.authorised(script, 'run')) {
				reportError "$user.userName is not authorised to execute on this page"
				return
			}
			def rq = exchange.request
			lastRunner = [
					url: rq.header.with { "http://$host$uri?$query" },
					page: "usdlc/$rq.query.page",
					sections: rq.query.sections?.split(',') as Set
			]
			runSectionsOnPage(Store.base(lastRunner.page), lastRunner.sections)
		}
	}

	static lastRunner
	def textMode = false, pageState
	/**
	 * Given a page reference and an optional list of sections, parse html and
	 * process each section looking for sub-pages to run and actors to run
	 * afterwards. If no sections are provided, all sections processed.
	 */
	void runSectionsOnPage(Store page, Set sectionsToRun = null) {
		pageState = new PageState(page)
		def p = page.dir.replaceFirst(~'^.*/usdlc/', '').split('/').collect {
			Store.decamel(it).replaceFirst(~'.*_', '')
		}.join(' -> ')
		wrapOutput([
				"<div class='gray'>Page: $p ->",
				"End: $p</div>"]) {
			currentPage = page
			Page html = new Page(page)
			def sections = html.sections
			if (sectionsToRun) {
				sections = sections.findAll { it.id in sectionsToRun }
			}
			def empty = true
			sections.findAll{!it.isDeleted()}.each { section ->
				def actors = []

				section.select('a[action=page],a[action=runnable]').
						each { link ->
					if (empty) { writeLinkHeader(); empty = false }
					String href = link.attr('href')
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
				if (!empty) {
					if (actors) { runActorsOnPage(actors) }
					if (exchange.data.problems) {
						reportError(exchange.data.problems.join('\n'))
						actorState = 'failed'
					}
					if (onScreen) {
						if (exchange.data.refresh) {
							return
						}
					}
				}
			}
		}
		pageState.save()
		// so screen can display current actor states
		js("parent.usdlc.actorStates()")
	}

	private writeLinkHeader() {
		if (!textMode) write """
			<html><head>
			<link type='text/css' rel='stylesheet'
			href='/usdlc/rt/outputFrame.css'>
			</head><body><div id='output'>"""
	}

	def onScreen = true
	def currentPage
	/**
	 * Called when running an actor in-context by using Setup and Cleanup.
	 */
	private runActorsOnPage(List<Store> actors) {
		try {
			def base = Store.base(actors[0].dir)
			runFiles(~/^Setup\..*/, base)
			actors.each { Store actor ->
				if (!exchange.data.rerun) runActor(actor)
			}
			runFiles(~/^Cleanup\..*/, base)
		} catch (AssertionError assertion) {
			reportException(assertion)
		} catch (throwable) {
			reportException(throwable)
			throwable.printStackTrace()
		} finally {
			context.each { key, value ->
				if (value?.metaClass?.respondsTo('close')) {
					actorState = "finalising $key"
					try { value.close() } catch (throwable) {
						reportException(throwable)
						throwable.printStackTrace()
					}
					actorState = 'succeeded'
				}
			}
			if (onScreen) {
				if (exchange.data.rerun) {
					exchange.data.remove('Rerun')
					runActorsOnPage(actors)
				} else if (exchange.data.refresh) {
					exchange.data.remove('refresh')
					js('parent.usdlc.refreshPage()')
				} else {
					resizeOutputFrame()
				}
			}
		}
	}

	void resizeOutputFrame() { js('parent.usdlc.resizeOutputFrame()') }
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
		currentActor = actorStore.pathBetweenFiles(currentPage)
		actorState = 'running'
		def actor = load(actorStore)
		if (actor) {
			wrapOutput(currentActor) { actor.run(context) }
			exchange.response.print("\0")
			actorState = 'succeeded'
		} else {
			actorState = 'data'
		}
	}
	/**
	 * If the actor is on the currently displayed page, show the state in the
	 * browser. If on another page, display as part of the output
	 * In all cases update the state in the page for persistence.
	 */
	void setActorState(String to) {
		pageState.linkStates[currentActor] = to
		String elapsed = timer
		wrapOutput([
				'<span class="gray" style="padding-left:2em;">',
				'</span><br>'
		]) { write "$currentActor: $to $elapsed" }
		Log.csv("${currentPage.path}.timings",
				"$currentActor,$to,$timer.elapsed,$elapsed")
	}

	String currentActor
	usdlc.Timer timer = new usdlc.Timer(
			title: 'in ', minimum: 100, autoReset: true)
	/**
	 * Special to run the script while wrapping the output for best effort.
	 */
//	void runScript(Map binding) {
//		wrapOutput(script.pathFromWebBase) { delegate.run(binding) }
//	}
	/**
	 * If an exception is thrown we need to display the error in a
	 * user-friendly
	 * way and flag the actor as having failed it's job.
	 */
	def reportException(Throwable throwable) {
		def messages = []
		while (throwable) {
			def message = throwable.message
			messages << message

			def match = (message =~ /^.*\((.+?)#(\d+)\).{0,2}$/)
			if (match) {
				def lno = match[0][2].toInteger()
				def lines = new File(match[0][1]).readLines()
				if (lno > 1) lno -= 2
				def end = lno + 3
				while (lno++ < end) messages << "    $lno: ${lines[lno]}"
			}
			throwable = throwable.cause
		}
		reportError { write messages.join('\n') }
		actorState = 'failed'
	}
	/**
	 * If we detect an error that is not an exception,
	 * we had better still treat
	 * it as something bad to tell the user.
	 */
	def reportError(String text) {
		reportError {write text}
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
		wrapOutput(['<script>', '</script>']) {
			write content.toString()
		}
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
		if (!textMode) {
			write wrapper[0]
			try {
				closure()
			} finally {
				write wrapper[1]
			}
		} else {
			closure()
			write("\n")
		}
	}

	static mimeTypeWrappers = [
			'text/html': ['<pre style="padding-left:4em;color:blue;">',
					'</pre>'],
			'application/javascript': ['<script>', '</script>'],
			'text/plain': ['<pre style="padding-left:4em;color:blue;">', '</pre>']]
	/**
	 * Write text to response output stream
	 */
	def write(String text) { exchange.response.write text }
}
