package usdlc

import usdlc.config.Config

import java.security.MessageDigest

import static groovy.io.FileType.FILES
import static usdlc.config.Config.config
import java.util.regex.Pattern

class Store {
	/**
	 * Build a store object for a path relative to the web root
	 */
	static Store base(path, project = null) {
		path = path.toString().replaceAll('\\\\', '/').replaceAll('//', '/')
		def homeIndex = path.indexOf(config.home)
		if (homeIndex == 0 || homeIndex == 1) {
			path = '~' + path[config.home.size() + homeIndex..-1]
		}
		def matcher = pathRE.matcher(path)
		path = camelCase(path)
		if (matcher) {
			//noinspection GroovyUnusedAssignment
			def (all, core, home, rest) = matcher[0]
			project = Config.project(home, project ?: Config.project('uSDLC'))
			path = "$project.home$rest"
			return new Store(path, project)
		}
		if (path.startsWith('./') && path.size() > 2) path = path[2..-1]
		new Store(path, Config.project('uSDLC'))
	}

	static pathRE = ~/^(.*)~\/?(\w*)(.*)$/

	private Store(String path, project) {
		if (path[0] == '/') path = ".$path"
		this.path = path
		this.project = project
		file = new File(path)
	}
	String path
	/**
	 * Return a Store for a non-existent file. It will refer to a new file
	 * in /tmp. Use the ID to provide a reference name and file extension. All
	 * files here are deleted every time uSDLC is started.
	 */
	static Store tmp(String name) { Store.base tmpDir.unique(name) }

	@Lazy static Store tmpDir = Store.base('tmp').rmdir()
	/**
	 * Sometimes we get a new store based on an old location
	 */
	Store rebase(String more = '') {
		new Store(glue(dir, camelCase(more)), project)
	}
	/**
	 * Glue strings together to make a path - with the correct number of /s
	 */
	def glue(Object[] parts) { parts.join('/').replaceAll('//', '/') }
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
	void mkdirs() { new File(dir).mkdirs() }

	@Lazy String href = path.replaceFirst(~/^\.\.\//, '/~')
	@Lazy String dir = isDirectory ? path : calcParent()
	@Lazy String name = file.name
	@Lazy String absolutePath = file.canonicalPath
	@Lazy URI uri = file.toURI()
	@Lazy URL url = uri.toURL()
	@Lazy parts = split(path)
	@Lazy fromProjectHome = path.startsWith(project.home) ?
			path[project.home.size()..-1] : path
	@Lazy boolean isDirectory = file.isDirectory() ||
					(!file.exists() && !parts.ext)
	@Lazy boolean isHtml = isDirectory || parts.ext == 'html'

	private String calcParent() {
		def parent = file.parent
		if (!parent) return '.'
		int drop = file.path.size() - file.parent.size() + 1
		if (drop >= path.size()) return ''
		path[0..-drop]
	}
	/**
	 * Relative path from some base directory (store may be of file in
	 * directory).
	 */
	String pathBetweenFiles(Store from) {
		File fromFile = from.isDirectory ? from.file : from.file.parentFile
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
		try { file.text } catch (FileNotFoundException fne) { '' }
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
	Store append(String contents) { append contents.bytes }
	Store append(long contents) { append contents.toString() }
	/**
	 * Check the size of the file.
	 * @return bytes in file.
	 */
	long size() { file.size() }
	/**
	 * See if the file exists
	 */
	boolean exists() { file.exists() }
	/**
	 * Return store if exists on disk - otherwise null
	 */
	def ifExists() { file.exists() ? this : null }
 	/**
	 * Fetch a list of matching contents of a directory - names only, no path
	 * @param mask - anything with isCase - typically a regular expression
	 * (~/re/)
	 * @param closure - code to execute for each file in the directory
	 */
	void dir(Pattern mask, Closure closure) {
		try {
			file.eachFileMatch mask, { File file -> closure(file.path) }
		} catch(e) {}
	}
	void dir(mask, Closure closure) { dir(~/${mask ?: '.*'}/, closure) }
	/**
	 * Fetch a list of all contents of a directory
	 * @param closure - code to execute for each file in the directory
	 */
	void dir(Closure closure) { dir(~/.*/, closure) }
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
	List dir() { dir(~/.*/) }
	/**
	 * Call a closure for the contents of a directory tree (as in dir /s)
	 */
	void dirs(mask, Closure closure) {
		dirs(~/$mask/, closure)
	}
	void dirs(Pattern mask, Closure closure) {
		try {
			file.traverse(type: FILES, nameFilter: mask) { File found ->
				closure(new Store(found.path, project))
			}
		} catch (e) {}
	}
	void dirs(Closure closure) { dirs(~/.*/, closure) }
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
	String toString() { path }

	File file
	Map project

	static int uniqifier = 0
	/**
	 * Find a file that doesn't exist - based on a timestamp (i.e.
	 * 2011-02-05_14-55-38-489_5).
	 * This path will sort correctly for creation date.
	 * e.g. assert Store.base('/tmp').unique('test.txt') ==~ /[\d\-_]_test
	 * .txt/
	 */
	String unique(name) {
		if (!name) return unique()
		name = camelCase(name)
		def timestamp = new Date().format(dateStampFormat)
		def uniquePath = "${timestamp}_${uniqifier}_${name}"
		def unique
		while ((unique = rebase(uniquePath)).exists()) {
			uniquePath = "${timestamp}_${uniqifier++}_${name}"
		}
		return unique.path
	}
	String unique() { Store.base(dir, project).unique(name) }
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
	static decamelRE = ~/([a-z0-9]{2})([A-Z])/
	static sloshRE = ~'\\\\'
	static dateStampFormat = 'yyyy_MM_dd__HH_mm_ss'
	/**
	 * Turn a camel-case name back into a sentence
	 */
	static decamel(camelCase) {
		camelCase.replaceAll(decamelRE, '$1 $2')
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
	def copyTo(to) {
		to = Store.base(to).absolutePath
		def includes = (file.directory) ? "**/*" : file.name
		ant.copy(todir: to) { fileset(dir: dir, includes: includes) }
	}
	/**
	 * moveTo a file or directory to a target directory.
	 */
	def moveTo(to) {
		to = Store.base(to).absolutePath
		def includes = (file.directory) ? "**/*" : file.name
		ant.move(todir: to) { fileset(dir: dir, includes: includes) }
	}
	/**
	 * Rename a file or directory
	 */
	def renameTo(to) {
		if (to.indexOf('/') == -1) to = "${file.parent ?: '.'}/$to"
		ant.move(file:"$file.path", tofile:"$to")
	}
	/**
	 * Remove the path created for this store - if it is a directory
	 */
	Store rmdir() {
		ant.delete(dir: file.path, includeemptydirs: true)
		return this
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
		if (isDirectory) {
			rmdir()
		} else {
			ant.delete(file: file.path)
		}
	}

	@Override int hashCode() { path.hashCode() }

	boolean equals(Object obj) { path == obj.path }

	@Lazy ant = Ant.builder(Log.file('store'), 2)
	/**
	 * Retrieve the root page file for each project (excluding uSDLC)
	 */
	static getProjectIndexes() {
		return projectRoots.findAll() {
			it.rebase('index.html').ifExists() ?:
				it.rebase('index.gsp').ifExists()
		}
	}
	static getProjectRoots() {
		def roots = []
		Store.base('~/').file.eachDir { File file ->
			def store = Store.base("~/$file.name/usdlc")
			if (store.exists()) roots << store
		}
		return roots
	}
	/**
	 * @return Retrieve the root page of the uSDLC project
	 */
	static Store getUsdlcRoot() { Store.base('usdlc/frontPage/index.html') }
	/**
	 * Return first matching file searching up parent tree.
	 */
	Store onParentPath(Closure test) {
		def home = project.home
		def path = dir
		while (path && path != home) {
			def store = Store.base(path)
			def result = test(store)
			if (result) return result
			path = store.calcParent()
		}
		return null
	}
	/**
	 * Calculate a SHA-256 digest for the file referenced here
	 */
	def sha256() {
		MessageDigest md = MessageDigest.getInstance("SHA-256")
		md.update(read())
		def integer = new BigInteger(1, md.digest())
		return integer.toString(16)
	}
	/**
	 * Search text file for string matches
	 */
	def grep(String pattern) { grep ~/$pattern/ }
	def grep(Pattern pattern) {
		def results = []
		file.eachLine { line, lno ->
			if (line =~ pattern) {
				results << new GrepResult(lineNumber: lno, contents: line)
			}
		}
		return results
	}
	class GrepResult { int lineNumber; String contents; }
	/**
	 * Return a list of projects (or potential projects)
	 */
	static projects(potentials = false) {
		def projects = []
		base('~/').file.eachDir { File file ->
			def store = Store.base("~/$file.name/usdlc")
			if (store.exists() != potentials) projects << file.name
		}
		return projects
	}
}
