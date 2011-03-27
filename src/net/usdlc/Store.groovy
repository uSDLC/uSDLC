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



package net.usdlc

import static groovy.io.FileType.FILES

/**
 Not all platforms that will use uSDLC will have access to a traditional file system. Google Appengine, for example, only allows data to be stored in BigTable - the database. For this reason, all uSDLC uses Store for persistence. The long-term plan is to more the base persistence calls the platform specific code.

 * User: Paul Marrington
 * Date: 23/11/10
 * Time: 5:31 PM
 */
class Store {
	/**
	 * All Store interfaces are created by static builders. Root is used for files relative to the web root.
	 *
	 * @param path Path and name of file to read or write
	 * @param from Add to web root to create a new base to read/write from/to
	 * @return A reference to a Store object used for further operations
	 */
	static Store root(path) {
		return base(path, "$webBase/")
	}
	/**
	 * Convenience method to access files from the usdlc directory.
	 *
	 * @param path Name of file in the usdlc directory.
	 * @return A reference to a Store object used for further operations
	 */
	static template(path) {
		return base(path, "$webBase/rt/")
	}
	/**
	 * Classpath variables are from the current directory when the program starts - not the web root.
	 * @param path Name of a file relative to the default starting directory
	 * @param from Add to base to create a new base such as root and template
	 * @return A reference to a Store object used for further operations
	 */
	static base(path, from = '') {
		def store = new Store()
		String ending = path
		if (ending[0] == '/') {
			ending = ending.substring(1)
		}
		store.file = new File("$from$ending")
		return store
	}

	static absolute(String path) {
		def store = new Store()
		store.file = new File(path)
		return store
	}

	static final webBase = {
		def wb = Config.web.webBase
		if (!wb) { return '.' }
		if (wb.startsWith('/')) { wb = wb[1..-1] }
		if (wb.endsWith('/')) { wb = wb[0..-2] }
		return wb
	}()
	/**
	 * build up directories underneath if they don't yet exist
	 */
	private def mkdirs() {
		if (parent) {
			new File(parent).mkdirs()
		}
	}

	@Lazy def parent = file.parent
	@Lazy def path = file.path
	/**
	 * Convenience method to access files from the lib directory.
	 *
	 * @param path Name of file in the lib directory.
	 * @return A reference to a Store object used for further operations
	 */
	public static lib(path) {
		return root(path, 'lib/')
	}

	/**
	 * Public method used to read the file.
	 * @return File contents as a byte array.
	 */
	public byte[] read() {
		return file.exists() ? file.bytes : [] as byte[]
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
	 * Fetch a list of contents of a directory
	 * @param mask - anything with isCase - typically a regular expression (~/re/)
	 * @return Array of results (empty if none)
	 */
	public dir(mask) {
		def list = []
		//noinspection GroovyEmptyCatchBlock
		try { file.eachFileMatch mask, { list << it.path } } catch (e) {}
		return list
	}
	/**
	 * Fetch a list of contents of a directory tree (as in dir /s)
	 * @param mask - anything with isCase - typically a regular expression (~/re/)
	 * @return Array of results (empty if none)
	 */
	public dirs(mask) {
		def list = []
		//noinspection GroovyEmptyCatchBlock
		try {
			file.traverse(type: FILES, nameFilter: mask) { list << it.path.replaceAll(sloshRE, '/') }
		} catch (e) {}
		return list
	}

	def lastModified() { return file.lastModified() }

	def toURL() { return file.toURL() }

	private File file

	/**
	 * Find a directory that doesn't exist - based on a timestamp (i.e. 2011-02-05_14-55-38-489_5). This path will sort correctly for creation date.
	 * @return A string representation of the path - being the same base as the current store.
	 */
	def uniquePath(end) {
		def timestamp = new Date().format(dateStampFormat)
		def uniquifier = 0
		def uniquePath = "${timestamp}_${uniquifier}_${end}"
		def unique
		while ((unique = new File(file, uniquePath)).exists()) {
			uniquifier++;
		}
		return unique.path
	}
	/**
	 * Later we will want to gather information from the unique name created
	 * @param uniqueName unique name created by uniquePath()
	 * @return object with date and title elements
	 */
	static parseUnique(uniqueName) {
		def (all, dateString, uniquifier, title) = uniqueRE.matcher(uniqueName)[0]
		return [
				date: Date.parse(dateStampFormat, dateString),
				title: title.replaceAll(decamelRE, ' $1'),
				path: uniqueName.replaceAll(sloshRE, '/')
		]
	}

	static uniqueRE = ~/(\d{4}-\d{2}-\d{2}_\d{2}-\d{2}-\d{2})_(\d+)_(.*)/
	static decamelRE = ~/\B([A-Z])/
	static sloshRE = ~"\\\\"
	static dateStampFormat = "yyyy-MM-dd_HH-mm-ss"
	/**
	 * Copy a file or directory to a target directory.
	 * @param to Directory to copy to.
	 */
	def copy(to) { ant.copy(file: file.path, toDir: to) }
	/**
	 * move a file or directory to a target directory.
	 * @param to Directory to move to.
	 */
	def move(to) { ant.move(file: file.path, toDir: to) }
	/**
	 * Remove the path created for this store - if it is a directory
	 */
	def rmdir() {
		capture {
			ant.delete(dir: file.path, includeemptydirs: true)
		}
	}

	@Lazy ant = new AntBuilder()

	def capture(Closure actions) {
		def out = System.out
		def buffer = new ByteArrayOutputStream()
		System.out = new PrintStream(buffer)
		actions()
		System.out = out
		return buffer.toString()
	}
}
