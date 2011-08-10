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
package usdlc

import static groovy.io.FileType.FILES
import static init.Config.config

/**
 Not all platforms that will use uSDLC will have access to a traditional file system. Google Appengine, for example, only allows data to be stored in BigTable - the database. For this reason, all uSDLC uses Store for persistence. The long-term plan is to more the base persistence calls the platform specific code.
 * User: Paul Marrington
 * Date: 23/11/10
 * Time: 5:31 PM
 */
class Store {
	/**
	 * Build a store object for a path relative to the web root
	 */
	static Store base(String path = '') {
		def store = new Store()
		store.file = new File(config.baseDirectory, camelCase(path))
		store
	}
	/**
	 * Sometimes we get a now store based on an old location
	 */
	Store rebase(String more = '') {
		def dir = file.isDirectory() ? path : parent
		def store = new Store()
		store.file = new File(config.baseDirectory, dir + camelCase(more))
		store
	}
	/**
	 * If we need to read a source as a stream...
	 * <code>
	 * def dir = Store.base("rt")
	 * def lines
	 * dir.withInputStream('pasteList.html.groovy') { stream -> stream.readLines() }
	 * </code>
	 */
	InputStream withInputStream(String fileName, Closure closure) { new File(file, fileName).withInputStream(closure) }
	/**
	 * Process a closure with a FileReader as the only parameter
	 * <code>
	 * list = []
	 * store.withReader { list << it }
	 * </code>
	 */
	Reader withReader(Closure closure) { new FileReader(file).withReader(closure) }
	/**
	 * build up directories underneath if they don't yet exist
	 */
	private mkdirs() {
		new File(config.baseDirectory, parent).mkdirs()
	}

	static URI baseDirectoryURI = new File(config.baseDirectory).toURI()
	/** Directory in which file/directory resides */
	@Lazy String parent = pathFromBase(file.parent)
	@Lazy String path = pathFromBase(file.path)
	@Lazy String absolutePath = file.path
	@Lazy String relativePath = "$config.baseDirectory/$path"
	@Lazy def uri = file.toURI()
	@Lazy def url = uri.toURL()
	/**
	 * Return a string being the file path relative to the base directory.
	 */
	private static String pathFromBase(File file) {
		baseDirectoryURI.relativize(file.toURI()).path
	}
	/**
	 * Return a string being the file path relative to the base directory.
	 */
	private static String pathFromBase(String path) {
		pathFromBase(new File(path))
	}
	/**
	 * Public method used to read the file.
	 * @return File contents as a byte array.
	 */
	byte[] read() {
		byte[] bytes
		if (file.exists()) {
			bytes = file.bytes
		} else {
			bytes = new byte[0]
		}
		bytes
	}
	/**
	 * Public method used to read the file into a string.
	 * @return File contents as a string.
	 */
	String text() {
		new String(read())
	}
	/**
	 * Public method to write file contents.
	 * @param contents Byte array of contents to write.
	 */
	void write(contents) {
		mkdirs()
		file.bytes = contents
	}
	/**
	 * Public method to write file contents - appending to existing contents.
	 * @param contents Byte array of contents to write.
	 */
	Store append(byte[] contents) {
		mkdirs()
		file << contents
		this
	}

	Store append(String contents) { append contents.bytes }

	Store append(long contents) { append contents.toString() }
	/**
	 * Check the size of the file.
	 * @return bytes in file.
	 */
	int size() { file.size() }
	/**
	 * See if the file exists
	 */
	boolean exists() { file.exists() }
	/**
	 * Fetch a list of matching contents of a directory - names only, no path
	 * @param mask - anything with isCase - typically a regular expression (~/re/)
	 * @param closure - code to execute for each file in the directory
	 */
	void dir(mask, Closure closure) {
		file.eachFileMatch mask, { closure(pathFromBase(it)) }
	}
	/**
	 * Fetch a list of all contents of a directory
	 * @param closure - code to execute for each file in the directory
	 */
	void dir(Closure closure) { dir(~/.*/, closure) }
	/**
	 * Fetch a list of matching contents of a directory
	 * @param mask - anything with isCase - typically a regular expression (~/re/)
	 * @return list of matching file paths
	 */
	List dir(mask) {
		def list = []
		dir(mask) { list << it }
		list
	}
	/**
	 * Fetch a list of all matching contents of a directory
	 * @return list of all files in the directory
	 */
	List dir() { dir(~/.*/) }
	/**
	 * Call a closure for the contents of a directory tree (as in dir /s)
	 * @param mask - anything with isCase - typically a regular expression (~/re/)
	 * @param closure - for every file that matches
	 */
	void dirs(mask, closure) {
		file.traverse(type: FILES, nameFilter: mask) { File file ->
			closure(pathFromBase(file))
		}
	}
	/**
	 * Fetch a list of contents of a directory tree (as in dir /s)
	 * @param mask - anything with isCase - typically a regular expression (~/re/)
	 * @return Array of results (empty if none)
	 */
	List dirs(mask) {
		def list = []
		dirs(mask) { list << it.replaceAll(sloshRE, '/') }
		list
	}

	long lastModified() { file.lastModified() }
	/**
	 * Store.toString(), implicit or explicit will return the full path to the file or directory
	 */
	String toString() { path }

	private File file

	/**
	 * Find a directory that doesn't exist - based on a timestamp (i.e. 2011-02-05_14-55-38-489_5). This path will sort correctly for creation date.
	 * @return A string representation of the path - being the same base as the current store.
	 */
	String uniquePath(end) {
		def timestamp = new Date().format(dateStampFormat)
		def uniquifier = 0
		def uniquePath = "${timestamp}_${uniquifier}_${camelCase(end)}"
		def unique
		while ((unique = new File(file, uniquePath)).exists()) {
			uniquifier++;
		}
		pathFromBase(unique)
	}
	/**
	 * Convert any sentence into a single camel-case word. It remove all punctuation and makes the start of each work a capital letter. So "My friend Charlie (Watson-Smith-jones)" becomes MyFriendCharlieWatsonSmithJones
	 */
	static String camelCase(text) {
		text.replaceAll(~/([\s:\?\*%\|"<>]+)(\w)/) { it[2].toUpperCase() }
	}
	/**
	 * Later we will want to gather information from the unique name created
	 * @param uniqueName unique name created by uniquePath()
	 * @return object with date and title elements
	 */
	static parseUnique(uniqueName) {
		def unique
		def matcher = uniqueRE.matcher(uniqueName)
		if (matcher && matcher.size() > 0 && matcher[0].size() >= 4) {
			String dateString = matcher[0][1]
			String title = matcher[0][3]
			unique = [
					date: Date.parse(dateStampFormat, dateString),
					title: decamel(title),
					path: uniqueName.replaceAll(sloshRE, '/')
			]
		} else {
			unique = null
		}
		unique
	}

	static uniqueRE = ~/(\d{4}-\d{2}-\d{2}_\d{2}-\d{2}-\d{2})_(\d+)_(\w*)/
	static decamelRE = ~/\B([A-Z])/
	static sloshRE = ~'\\\\'
	static dateStampFormat = 'yyyy-MM-dd_HH-mm-ss'
	/**
	 * Turn a camel-case name back into a sentence
	 */
	static decamel(camelCase) { camelCase.replaceAll(decamelRE, ' $1') }
	/**
	 * Split a fully qualified storage name into path, name and extension
	 * @return [path : path, name : name, ext : ext]
	 */
	static Map split(path) {
		def matcher = splitRE.matcher(path)[0]
		[path: matcher[1], name: matcher[2], ext: matcher[3]]
	}

	static splitRE = ~/^(?:(.*)[\/\\])?([^\.]*)?(.*)?$/
	/**
	 * Copy a file or directory to a target directory.
	 */
	def copy(to) {
		def includes = (file.directory) ? "$file.name/*" : file.name
		ant.copy(toDir: to) { fileset(dir: file.parent, includes: includes) }
	}
	/**
	 * move a file or directory to a target directory.
	 */
	def move(to) { ant.move(toDir: to) { fileset(dir: file.parent, includes: "$file.name/*") } }
	/**
	 * Remove the path created for this store - if it is a directory
	 */
	def rmdir() { ant.delete(dir: file.path, includeemptydirs: true) }
	/**
	 * Delete the named file that lives under the defined Store
	 */
	def delete(name) { ant.delete { fileset(dir: file.path, includes: name)} }
	/**
	 * Delete the file pointed to by the store.
	 */
	def delete() { ant.delete(file: file.name) }

	@Lazy ant = Ant.builder(Log.file('store'), 2)
}
