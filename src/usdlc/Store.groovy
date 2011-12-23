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
import static usdlc.config.Config.config

/**
 * Not all platforms that will use uSDLC will have access to a traditional
 * file system.
 * Google Appengine, for example, only allows data to be stored in BigTable -
 * the database.
 * For this reason, all uSDLC uses Store for persistence.
 * The long-term plan is to more the base persistence calls the platform
 * specific code.
 */
class Store {
	/**
	 * Build a store object for a path relative to the web root
	 */
	static Store base(String path = '') {
		new Store(camelCase(path.replaceFirst(~/.*~/, config.home as String)))
	}
	private Store(String pathFromWebBase) {
		if (pathFromWebBase[0] == '/') {
			if (pathFromWebBase.size() > 1) {
			pathFromWebBase = pathFromWebBase[1..-1]
			} else {
				pathFromWebBase = ''
			}
		}
		this.pathFromWebBase = pathFromWebBase
		file = new File(config.baseDirectory as String, pathFromWebBase)
	}
	/**
	 * Return a Store for a new (non-existent) file. It will refer to a new file
	 * in /tmp. Use the ID to provide a reference name and file extension. All
	 * files here are deleted every time uSDLC is started.
	 */
	static Store tmp(String id = '.txt') {
		Store.base tmpDir.uniquePath("/$id")
	}

	@Lazy static Store tmpDir = Store.base('/tmp').rmdir()
	/**
	 * Sometimes we get a new store based on an old location
	 */
	Store rebase(String more = '') {
		new Store("$parent/${camelCase(more)}")
	}
	/**
	 * If we need to read a source as a stream...
	 * <code>
	 * def dir = Store.base("rt")
	 * def lines
	 * dir.withInputStream('pasteList.html.groovy') { stream -> stream
	 * .readLines() }* </code>
	 */
	InputStream withInputStream(String fileName, Closure closure) {
		new File(file, fileName).withInputStream(closure)
	}

	InputStream withInputStream(Closure closure) {
		file.withInputStream(closure)
	}
	/**
	 * Process a closure with a FileReader as the only parameter
	 * <code>
	 * list = []
	 * store.withReader { list << it }* </code>
	 */
	Reader withReader(Closure closure) {
		new FileReader(file).withReader(closure)
	}
	/**
	 * build up directories underneath if they don't yet exist
	 */
	public mkdirs() {
		new File(config.baseDirectory, parent).mkdirs()
	}

	static URI baseDirectoryURI = new File(config.baseDirectory).toURI()
	/** Directory in which file/directory resides  */
	@Lazy String parent = file.isDirectory() ? pathFromWebBase : calcParent()
	@Lazy String name = file.name
	@Lazy String absolutePath = file.absolutePath
	@Lazy URI uri = file.toURI()
	@Lazy URL url = uri.toURL()
	@Lazy parts = split(pathFromWebBase)

	private String calcParent() {
		int drop = file.path.size() - file.parent.size() + 1
		if (drop >= pathFromWebBase.size()) {
			return ''
		} else {
			return pathFromWebBase[0..-drop]
		}
	}
	/**
	 * Return a string being the file path relative to the base directory.
	 */
	private static String pathFromBase(String path) {
		if (path.startsWith(config.baseDirectory)) {
			path = path[config.baseDirectory.size()..-1]
		}
		return path
	}
	/**
	 * Relative path from some base directory (store may be of file in
	 * directory).
	 */
	String pathFrom(Store from) {
		File fromFile = from.file.isDirectory() ?
			from.file : from.file.parentFile
		fromFile.toURI().relativize(uri).path
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
	String getText() {
		file.text
	}
	/**
	 * Set the contents of the file as a string (use write for binary)
	 */
	void setText(String contents) {
		mkdirs()
		file.text = contents
	}
	/**
	 * Public method to write file contents.
	 * @param contents Byte array of contents to write.
	 */
	void write(byte[] contents) {
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

	Store append(String contents) {
		append contents.bytes
	}

	Store append(long contents) {
		append contents.toString()
	}
	/**
	 * Check the size of the file.
	 * @return bytes in file.
	 */
	long size() {
		file.size()
	}
	/**
	 * See if the file exists
	 */
	boolean exists() {
		file.exists()
	}
	/**
	 * Fetch a list of matching contents of a directory - names only, no path
	 * @param mask - anything with isCase - typically a regular expression
	 * (~/re/)
	 * @param closure - code to execute for each file in the directory
	 */
	void dir(mask, Closure closure) {
		file.eachFileMatch mask, { File file ->
			closure(pathFromBase(file.path))
		}
	}
	/**
	 * Fetch a list of all contents of a directory
	 * @param closure - code to execute for each file in the directory
	 */
	void dir(Closure closure) {
		dir(~/.*/, closure)
	}
	/**
	 * Fetch a list of matching contents of a directory
	 * @param mask - anything with isCase - typically a regular expression
	 * (~/re/)
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
	List dir() {
		dir(~/.*/)
	}
	/**
	 * Call a closure for the contents of a directory tree (as in dir /s)
	 */
	void dirs(mask, Closure closure) {
		file.traverse(type: FILES, nameFilter: mask) { File file ->
			closure(new Store(pathFromBase(file.path)))
		}
	}
	/**
	 * Used to see if a compile/processing action is required because the
	 * source is newer than the destination.
	 * lastModified returns 0 if the file does not exist,
	 * so if the destination does not exist then the source
	 * is flagged as newer.
	 */
	boolean newer(Store than) {
		file.lastModified() > than.file.lastModified()
	}
	/**
	 * Store.toString(), implicit or explicit will return the full path to the
	 * file or directory
	 */
	String toString() {
		pathFromWebBase
	}

	File file
	String pathFromWebBase

	/**
	 * Find a directory that doesn't exist - based on a timestamp (i.e.
	 * 2011-02-05_14-55-38-489_5).
	 * This path will sort correctly for creation date.
	 */
	String uniquePath(id) {
		def timestamp = new Date().format(dateStampFormat)
		def uniquePath = "${timestamp}_${uniqifier}_${camelCase(id)}"
		def unique
		while ((unique = new File(file, uniquePath)).exists()) {
			uniquePath = "${timestamp}_${uniqifier++}_${camelCase(id)}"
		}
		pathFromBase(unique.path)
	}

	static int uniqifier = 0
	/**
	 * Find a file that doesn't exist - based on a timestamp (i.e.
	 * 2011-02-05_14-55-38-489_5).
	 * This path will sort correctly for creation date.
	 * e.g. assert Store.base('/tmp').unique('test.txt') ==~ /[\d\-_]_test.txt/
	 */
	Store unique(name) {
		Store.base uniquePath(id)
	}
	/**
	 * Convert any sentence into a single camel-case word. It remove all
	 * punctuation and makes the start of each work a capital letter.
	 * So "My friend Charlie (Watson-Smith-jones)" becomes
	 * MyFriendCharlieWatsonSmithJones
	 */
	static String camelCase(String text) {
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
	static dateStampFormat = 'yyyy_MM_dd__HH_mm_ss'
	/**
	 * Turn a camel-case name back into a sentence
	 */
	static decamel(camelCase) {
		camelCase.replaceAll(decamelRE, ' $1')
	}
	/**
	 * Split a fully qualified storage name into path, name and extension
	 * @return [path : path, name : name, ext : ext]
	 */
	static Map split(String path) {
		def matcher = splitRE.matcher(path)[0]
		[path: matcher[1], name: matcher[2], ext: matcher[3]]
	}

	static splitRE = ~/^(?:(.*)[\/\\])?(.*?)(?:\.([^\.]*))?$/
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
	def move(to) {
		ant.move(toDir: to) {
			fileset(dir: file.parent, includes: "$file.name/*")
		}
	}
	/**
	 * Remove the path created for this store - if it is a directory
	 */
	Store rmdir() {
		ant.delete(dir: file.path, includeemptydirs: true)
		this
	}
	/**
	 * Delete the named file that lives under the defined Store
	 */
	def delete(name) {
		ant.delete {
			fileset(dir: file.path, includes: name)
		}
	}
	/**
	 * Delete the file pointed to by the store.
	 */
	def delete() {
		ant.delete(file: file.path)
	}

	@Override
	public int hashCode() {
		return pathFromWebBase.hashCode()
	}

	@Override
	public boolean equals(Object obj) {
		return pathFromWebBase.equals(obj.pathFromWebBase)
	}

	@Lazy ant = Ant.builder(Log.file('store'), 2)
}
