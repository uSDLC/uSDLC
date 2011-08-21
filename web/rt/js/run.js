/*
 * Copyright 2011 Paul Marrington for http://usdlc.net
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

$(function() {
	$.extend(true, window.usdlc, {
		passed : function() {
			usdlc.highlight('green')
		},
		failed : function() {
			usdlc.highlight('red')
		},
		/**
		 * Command to run the whole page - as in from drop-down in header or a
		 * link/button
		 */
		runPage : function() {
			runSections($('div.section'))
		},
		/**
		 * Command to run the page section.
		 */
		runSectionInFocus : function() {
			if (usdlc.inFocus)
				runSections(usdlc.inFocus)
			else
				usdlc.alert('selectSectionFirst.htm')
		},
		/**
		 * Command to run a section of the page or from the page section to the
		 * end of the page.
		 */
		runFromSectionInFocus : function() {
			if (usdlc.inFocus)
				runSections(usdlc.inFocus.nextAll('.section').andSelf())
			else
				usdlc.alert('selectSectionFirst.htm')
		},
		deleteOutput : function() {
			$('iframe.output').remove()
		},
		resizeOutputFrame : function() {
			setTimeout(resizeOutputFrameImmediate, 1000)
		},
		actorState : function(href, state) {
			$('a[href="'+href+'"]').
				removeClass('running failed succeeded').addClass(state)
		}
	})

	function resizeOutputFrameImmediate() {
		var frame = $('iframe.output').last()
		var height = $("div#output", frame.contents()).height() + 33
		frame.height((height < 40) ? 0 : height)
	}

	function runSections(sections) {
		if (sections.length) {
			usdlc.saveSource()
			usdlc.deleteOutput()
			var outputSection = sections.first()
			sections = sections.map(function() {
				return $(this).attr('id')
			}).get().join(',')
			var pc = usdlc.pageContentsURL
			var pcf = usdlc.removeUrlBase(pc)
			var url = pc + '.sectionRunner?page=' + pcf + '&sections=' + sections
			outputFrame = $('<iframe/>').addClass('output').attr('src', url)
			outputSection.append(outputFrame)
		}
	}
})
