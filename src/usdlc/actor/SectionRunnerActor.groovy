/*
 * Copyright 2011 the Authors for http://usdlc.net
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package usdlc.actor

import java.util.List
import java.util.Map
import java.util.regex.Pattern

import org.jsoup.Jsoup

import usdlc.Log
import usdlc.Page
import usdlc.Store
import groovy.lang.Closure
import groovy.transform.AutoClone
import groovy.xml.StreamingMarkupBuilder
import static usdlc.MimeTypes.mimeType
import static usdlc.Config.config

@AutoClone class SectionRunnerActor extends Actor {
	/**
	 * Override to run the section runner script.
	 */
	/* (non-Javadoc)
	 * @see usdlc.actor.Actor#run(usdlc.Store)
	 */
	void run(Store script) {
		def user = exchange.request.user
		if (!user.authorised(script, 'run')){
			reportError "$user.id is not authorised to execute on this page"
			return
		}
		
		rerun = (script.path == 'last.sectionRunner')
		if (rerun) {
			if (lastRunner) {
				exchange.request.session.persist.lastSectionRunner = lastRunner
			} else {
				lastRunner = exchange.request.session.persist.lastSectionRunner
			}
		} else {
			def rq = exchange.request
			lastRunner = [
						url : rq.header.with { "http://$host$uri?$query" },
						page : rq.query.page,
						sections : rq.query.sections
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
		def html = new Page(page)
		def sections = html.sections
		if (sectionsToRunCsv) {
			sections = sections.select(
					'div#'+sectionsToRunCsv.replaceAll(/,/, ',div#'))
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
				}
			}
			wrapOutput([
				'<pre class="gray">',
				'</pre>'
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
			if (exchange.store.path.endsWith('.sectionRunner')) {
				exchange.store = Store.base(exchange.store.path[0..-15])
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
			actors.each { runActor(it) }
			runFiles(~/^Cleanup\..*/, base)
		} catch (AssertionError assertion) {
			reportException(assertion)
		} catch (Throwable throwable) {
			reportException(throwable)
			throwable.printStackTrace()
		} finally {
			context.each { key, value ->
				if (value?.metaClass.respondsTo('close')) value.close()
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
	def setActorState(to) {
		linkStates[currentActor] = to
		String elapsed = timer
		wrapOutput([
			'<pre class="gray">',
			'</pre>'
		]) { write "\t$currentActor: $to $elapsed" }
		Log.csv("${currentPage}.timings")(
				"$currentActor,$to,$timer.elapsed,$elapsed")
	}
	def currentActor
	usdlc.Timer timer = new usdlc.Timer(
	title : 'in ', minimum : 100, autoReset : true)
	/**
	 * Special to run the script while wrapping the output for best effort.
	 */
	void runScript(Map binding) {
		wrapOutput(script.path, binding) { delegate.run(binding) }
	}
	/**
	 * If an exception is thrown we need to display the error in a user-friendly 
	 * way and flag the actor as having failed it's job.
	 */
	def reportException(Throwable throwable) {
		def trace = throwable.stackTrace.find {
			it.toString() ==~ ~/\w+\.run\(.*/
		} ?: throwable.stackTrace.find {
			it.fileName && it.lineNumber > 0 &&
					!internalExceptions.matcher(it.className).find()
		}
		wrapOutput(['<pre>', '</pre>']) {
			write throwable.message
			if (trace) write "\n\n${trace.toString()}"
		}
		actorState = 'failed'
	}
	/**
	 * If we detect an error that is not an exception, we had better still treat 
	 * it as something bad to tell the user.
	 */
	def reportError(String text) {
		wrapOutput([
			'<span style="color:red;">',
			'</span>'
		]) { write text }
		resizeOutputFrame()
	}
	/**
	 * Inject javascript into the output stream
	 */
	void js(content) {
		wrapOutput(['<script>', '</script>']) { write content.toString() }
	}
	/**
	 * Wrap data of defined mime-type as if it were to be included in a HTML file
	 */
	def wrapOutput(String fileName, Closure closure) {
		def type = mimeType(fileName)
		wrapOutput(mimeTypeWrappers.get(
			mimeType(fileName), ['<!--', '-->']), closure)
	}
	/**
	 * Wrap data in a HTML tag
	 */
	def wrapOutput(List wrapper, Closure closure) {
		write wrapper[0]
		try {
			closure()
		} finally {
			write wrapper[1]
		}
	}
	static mimeTypeWrappers = [
		'text/html': ['', ''],
		'application/javascript': ['<script>', '</script>'],
		'text/plain': ['<pre>', '</pre>']]
	/**
	 * Write text to response output stream
	 */
	def write(text) {
		if (!rerun) exchange.response.write text
	}
}