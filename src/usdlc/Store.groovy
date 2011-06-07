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



package usdlc

import static groovy.io.FileType.FILES

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
		store.file = new File(Config.baseDirectory, path)
		return store
	}
	/**
	 * Sometimes we get a now store based on an old location
	 */
	Store rebase(String more = '') {
		def store = new Store()
		store.file = new File(path + more)
		return store
	}
	/**
	 * If we need to read a source as a stream...
	 * <code>
	 * def dir = Store.base("rt")
	 * def lines
	 * dir.withInputStream('template.html.groovy') { stream -> stream.readLines() }* </code>
	 */
	InputStream withInputStream(String fileName, Closure closure) {
		return new File(file, fileName).withInputStream(closure)
	}
	/**
	 * build up directories underneath if they don't yet exist
	 */
	private def mkdirs() {
		new File(Config.baseDirectory + parent).mkdirs()
	}

	static URI baseDirectoryURI = new File(Config.baseDirectory).toURI()
	@Lazy String parent = pathFromBase(file.parent)
	@Lazy String path = pathFromBase(file.path)
	@Lazy String absolutePath = file.path
	@Lazy def uri = file.toURI()
	@Lazy def url = uri.toURL()
	/**
	 * Return a string being the file path relative to the base directory.
	 */
	private static String pathFromBase(File file) {
		return baseDirectoryURI.relativize(file.toURI()).path
	}
	/**
	 * Return a string being the file path relative to the base directory.
	 */
	private static String pathFromBase(String path) {
		return pathFromBase(new File(path))
	}
	/**
	 * Public method used to read the file.
	 * @return File contents as a byte array.
	 */
	public byte[] read() {
		byte[] bytes
		if (file.exists()) {
			bytes = file.bytes
		} else {
			bytes = new byte[0]
		}
		return bytes
	}
	/**
	 * Public method used to read the file into a string.
	 * @return File contents as a string.
	 */
	public String text() {
		return new String(read())
	}
	/**
	 * Public method to write file contents.
	 * @param contents Byte array of contents to write.
	 */
	public write(contents) {
		mkdirs()
		file.bytes = contents
	}
	/**
	 * Public method to write file contents - appending to existing contents.
	 * @param contents Byte array of contents to write.
	 */
	public append(contents) {
		mkdirs()
		file << contents
	}
	/**
	 * Check the size of the file.
	 * @return bytes in file.
	 */
	public size() {
		return file.size()
	}
	/**
	 * See if the file exists
	 * @return True if the file is available
	 */
	public exists() {
		return file.exists()
	}
	/**
	 * Fetch a list of matching contents of a directory - names only, no path
	 * @param mask - anything with isCase - typically a regular expression (~/re/)
	 * @param closure - code to execute for each file in the directory
	 */
	void dir(mask, Closure closure) {
		//noinspection GroovyEmptyCatchBlock
		try { file.eachFileMatch mask, { closure(pathFromBase(it)) } } catch (e) {}
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
		dir(mask, { list << it })
		return list
	}
	/**
	 * Fetch a list of all matching contents of a directory
	 * @return list of all files in the directory
	 */
	public dir() { dir(~/.*/) }
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
		dirs(mask, { list << it.replaceAll(sloshRE, '/') })
		return list
	}

	def lastModified() { return file.lastModified() }
	/**
	 * Store.toString(), implicit or explicit will return the full path to the file or directory
	 */
	String toString() { return path }

	private File file

	/**
	 * Find a directory that doesn't exist - based on a timestamp (i.e. 2011-02-05_14-55-38-489_5). This path will sort correctly for creation date.
	 * @return A string representation of the path - being the same base as the current store.
	 */
	String uniquePath(end) {
		def timestamp = new Date().format(dateStampFormat)
		def uniquifier = 0
		def uniquePath = "${timestamp}_${uniquifier}_${end.replaceAll(cleanFileNameRE, '')}"
		def unique
		while ((unique = new File(file, uniquePath)).exists()) {
			uniquifier++;
		}
		return pathFromBase(unique)
	}

	static cleanFileNameRE = ~/\W/
	/**
	 * Later we will want to gather information from the unique name created
	 * @param uniqueName unique name created by uniquePath()
	 * @return object with date and title elements
	 */
	static parseUnique(uniqueName) {
		//noinspection GroovyUnusedAssignment
		def unique
		def matcher = uniqueRE.matcher(uniqueName)
		if (matcher && matcher.size() > 0 && matcher[0].size() >= 4) {
			def (all, dateString, uniquifier, title) = matcher[0]
			unique = [
					date: Date.parse(dateStampFormat, dateString),
					title: decamel(title),
					path: uniqueName.replaceAll(sloshRE, '/')
			]
		} else {
			unique = null
		}
		return unique
	}

	static uniqueRE = ~/(\d{4}-\d{2}-\d{2}_\d{2}-\d{2}-\d{2})_(\d+)_(\w*)/
	static decamelRE = ~/\B([A-Z])/
	static sloshRE = ~"\\\\"
	static dateStampFormat = "yyyy-MM-dd_HH-mm-ss"
	/**
	 * Turn a camel-case name back into a sentence
	 */
	static decamel(camelCase) { return camelCase.replaceAll(decamelRE, ' $1') }
	/**
	 * Split a fully qualified storage name into path, name ane extension
	 * @return [path : path, name : name, ext : ext]
	 */
	static split(path) {
		def matcher = splitRE.matcher(path)[0]
		return [path: matcher[1], name: matcher[2], ext: matcher[3]]
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
