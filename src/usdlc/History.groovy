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
package usdlc

import java.util.regex.Pattern

/**
 * When changing pages, scripts or data we save the difference between versions. Expected creation is:
 *
 * new History(dir : directory, name: baseFileName, type: historyType)
 *
 * Where history type is 'change' for edit changes and 'run' for run results.
 *
 * User: Paul Marrington
 * Date: 12/01/11
 */
class History {
	def path = 'default'
	def type = "updates"
	static diff = new diff_match_patch()
	/**
	 * History file is stored in a special directory and named file.
	 * @return
	 */
	@Lazy Store store = Store.base(".history/$type/${path}.history")
	/**
	 * Whenever we save a file we can also save the changes from last time.
	 *
	 * @param userId Each change is labelled with the user who made it
	 * @param before Copy of file in storage
	 * @param after Copy of file to be placed in storage
	 */
	def save(userId, before, after) {
		def diffs = diff.diff_main(before, after)
		diff.diff_cleanupEfficiency diffs
		def patch = diff.patch_toText(diff.patch_make(diffs))

		String entry = "---- $userId -- ${new Date()} ----\n$patch"
		store.append(entry.bytes)
	}
	/**
	 * We can restore the file contents given an index into the list of history changes.
	 * Available from url as pageAddr?action=history&_index_=-2 (for example)
	 *
	 * @param to Index into history changes - use -1 for last change.
	 * @return Contents of the file at this point of time.
	 */
	def restore(int to) {
		def contents = ''
		patches[0..to].each({
			def successes   // pass/fail array for each patch
			(contents, successes) = diff.patch_apply(it.patch, contents)
		})
		return contents
	}
	/**
	 * Retrieve a list of patches for each prior save. Each element is a dictionary with the user Id of the user who made the change, the date of the change and the patches to apply.
	 *
	 * <pre>
	 * history.patches.each({*     println( "user %it.userId changed the file on %it.date")
	 *})
	 * </pre>
	 *
	 * @return patches array of dictionaries.
	 */
	@Lazy def patches = {
		def pl = []
		// Use regex to retrieve the text representation of each patch and convert it to a list of patches to apply for a history version. The array is from oldest to newest.
		new String(store.read() as byte[]).eachMatch(historyRE, { full, userId, date, textPatch ->
			def patch = diff.patch_fromText(textPatch)
			pl.push([userId: userId, date: date, patch: patch])
		})
		return pl
	}()
	static historyRE = Pattern.compile(/-- (\w+?) -- (.*?) ----\n(.*?)\n--/, Pattern.DOTALL)
}
