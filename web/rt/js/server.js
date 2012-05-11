$(function() {
	var queues = {}
	window.usdlc.splitUrl = function(url) {
		var result = {}
		var slash = url.lastIndexOf('/')
		if (slash == -1) {
			result.path = ''
		} else {
			result.path = url.substring(0, slash)
			url = url.substring(slash + 1)
		}
		var dot = url.indexOf('.')
		if (dot == -1) {
			result.name = url
			result.ext = ''
		} else {
			result.name = url.substring(0, dot)
			result.ext = url.substring(dot + 1)
		}
		return result
	}
	window.usdlc.removeDomain = function(url) {
		return url.replace(/\w+:\/\/[^\/]+\//, "")
	}
	window.usdlc.mimeType = function(href) {
		var extensions = href.match(/\.\w+/g)
		var rc = {}
		if (extensions && extensions[0]) {
			rc.clientExt = extensions[0].substring(1)
			rc.serverExt = (extensions.length > 1 && extensions[1]) ? extensions[1].substring(1) : rc.clientExt
		} else {
			rc = {
				clientExt : 'txt',
				serverExt : 'txt'
			}
		}
		return rc
	}
	window.usdlc.serverActionUrl = function(where, what) {
		if (!where || where == '/') where = '/index.html'
		var sep = (where.indexOf('?') == -1) ? '?' : '&'
		return where + sep + "action=" + what
	}
	/**
	 * Load a page into an iframe that is displayed in a dialog box on the same page. Options are those for jquery
	 * dialog plus: url: url to load in iframe action: action to perform (action=xxx) height: percentage of window
	 * height can be used as well as pixels
	 *
	 * @returns reference to the dialog object
	 */
	window.usdlc.iframe = function(options) {
		var url = usdlc.serverActionUrl(options.url, options.action)
		return usdlc.dialog('<iframe/>', options).attr('src', url)
	}
	window.usdlc.window = function(name, url, options) {
		options = $.extend({}, {
					status : 0,
					toolbar : 0,
					location : 0,
					titlebar : 0,
					menubar : 0,
					resizable : 1,
					scrollbars : 1,
					width : 1024,
					height : 600
				}, options)
		var os = 'directories=0'
		$.each(options, function(k, v) {
			os += ',' + k + '=' + v
		})
		window.open(url, options.name, os)
	}
	/**
	 * While designed for ajax commands, queue() works for any function that works asynchronously, takes time and has a
	 * complete() option that it calls when done.
	 *
	 * @param action -
	 *            function to call in its place and when it is due
	 * @param options -
	 *            options provided to the function for it's operation, plus: queueName - name of queue to work with
	 *            (defaultQueue if empty) complete - the method the function calls when done parallel - run this
	 *            function in parallel with others on the same queue. Functions that do not have options.parallel set
	 *            will run one after another from first to last. All parallel functions run when they are called. Until
	 *            all parallel tasks flag complete no queued serialised tasks will be fired off.
	 */
	window.usdlc.queue = function(action, options) {
		var queueName = options.queueName || 'defaultQueue'
		var queue = queues[queueName] || (queues[queueName] = {
			parallel : 0,
			waiting : []
		})
		// use parallel as a semaphore to stop another serialised task kicking
		// off of completion if this task creates a parallel task.
		var priorComplete = options.complete

		function runPriorComplete(self, args) {
			if (priorComplete) {
				priorComplete.apply(self, args);
			}
		}

		if (options.parallel) { // Function is asynchronous.
			// Parallel task count is kept to prevent early kick-off of dependents.
			queue.parallel++
			options.complete = function() { // Call old
				// complete if the options had one.
				runPriorComplete(this, arguments)
				if (--queue.parallel === 0 && queue.waiting.length > 0) {
					queue.waiting.shift()() // run the first dependent task
				}
			}
			action(options) // Action kicks off immediately.
		} else if (queue.waiting.length === 0 && queue.parallel.length === 0) {
			// dependent with nothing to depend on...
			action(options) // Action kicks off immediately.
		} else { // dependent with something to be done
			// first (parallel or earlier dependent)
			queue.waiting.push(function() {
				action(options)
			})
		}
	}
})
