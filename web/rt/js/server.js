/*
 * Copyright 2011 Paul Marrington for http://usdlc.net
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

$(function() {
	$.extend(true, window.usdlc, {
				splitUrl : function(url) {
					var result = {}
					var slash = url.lastIndexOf('/')
					if (slash != -1) {
						result.path = url.substring(0, slash)
						url = url.substring(slash + 1)
					} else {
						result.path = ''
					}
					var dot = url.indexOf('.')
					if (dot != -1) {
						result.name = url.substring(0, dot)
						result.ext = url.substring(dot + 1)
					} else {
						result.name = url
						result.ext = ''
					}
					return result
				},
				mimeType : function(href) {
					var extentions = href.match(/\.\w+/g)
					var rc = {}
					if (extentions && extentions[0]) {
						rc.clientExt = extentions[0].substring(1)
						rc.serverExt = (extentions.length > 1 && extentions[1]) ? extentions[1].substring(1) : rc.clientExt
					} else {
						rc = {clientExt : 'txt', serverExt : 'txt' }
					}
					rc.syntax = (rc.serverExt in syntaxes) ? syntaxes[rc.serverExt] : 'groovy'
					return rc
				},

				serverActionUrl : function(where, what) {
					if (! where || where == '/') {
						where = '/index.html'
					}
					var sep = (where.indexOf('?') == -1) ? '?' : '&'
					return where + sep + "action=" + what
				},
				/**
				 Load a page into an iframe that is displayed in a dialog box on the same page.
				 Options are those for jquery dialog plus:
				 url: url to load in iframe
				 action: action to perform (action=xxx)
				 height: percentage of window height can be used as well as pixels
				 @returns reference to the dialog object
				 */
				iframe: function(options) {
					var url = usdlc.serverActionUrl(options.url, options.action)
					return usdlc.dialog('<iframe/>', options).attr('src', url)
				},
				window : function(name, url, options) {
					options = $.extend({}, {
								status: 0, toolbar: 0, location: 0, titlebar: 0, menubar: 0,
								resizable: 1, scrollbars: 1, width: 600, height: 600
							}, options)
					var os = 'directories=0'
					$.each(options, function(k, v) {
						os += ',' + k + '=' + v
					})
					window.open(url, options.name, os)
				},
				/**
				 * While designed for ajax commands, queue() works for any function that works asynchronously, takes time and has a complete() option that it calls when done.
				 * @param action - function to call in its place and when it is due
				 * @param options - options provided to the function for it's operation, plus:
				 *      queueName - name of queue to work with (defaultQueue if empty)
				 *      complete - the method the function calls when done
				 *      parallel - run this function in parallel with others on the same queue. Functions that do not have options.parallel set will run one after another from first to last. All parallel functions run when they are called. Until all parallel tasks flag complete no queued serialised tasks will be fired off.
				 */
				queue : function(action, options) {
					var queueName = options.queueName || 'defaultQueue'
					var queue = queues[queueName] || (queues[queueName] = { parallel : 0, waiting: [] })
					// use parallel as a semaphore to stop another serialised task kicking off of completion if this task creates a parallel task.
					var priorComplete = options.complete

					function runPriorComplete(self, args) {
						if (priorComplete) {
							priorComplete.apply(self, args);
						}
					}

					if (options.parallel) { // Function is asynchronous. Parallel task count is kept to prevent early kick-off of dependents.
						queue.parallel++
						options.complete = function() {   // Call old complete if the options had one.
							runPriorComplete(this, arguments)
							if (--queue.parallel === 0 && queue.waiting.length > 0) {
								queue.waiting.shift()() // run the first dependent task
							}
						}
						action(options) // Action kicks off immediately.
					} else if (queue.waiting.length === 0 && queue.parallel.length === 0) {  // dependent with nothing to depend on...
						action(options) // Action kicks off immediately.
					} else {    // dependent with something to be done first (parallel or earlier dependent)
						queue.waiting.push(function() {
							action(options)
						})
					}
				}
			})

	var queues = {}
	var syntaxes = {
		basic: 'basic', c: 'c', h: 'c', cpp: 'cpp', hpp: 'cpp', css: 'css', groovy: 'groovy', html: 'html', htm: 'html', htmlunit: 'groovy', java: 'java', js: 'js', pas: 'pas', perl: 'perl', php: 'php', py: 'python', ruby: 'ruby', sql: 'sql', vb: 'vb', xml: 'xml'
	}
})
