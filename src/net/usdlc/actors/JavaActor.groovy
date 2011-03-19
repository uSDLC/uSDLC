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
package net.usdlc.actors

import javax.tools.ToolProvider
import net.usdlc.Config
import net.usdlc.Environment
import net.usdlc.Filer
import net.usdlc.HtmlBuilder
import net.usdlc.actors.JavaFileObjects.ClassFileManager

/**
 * Java actor super-class. Implement run() in your sub-class to do the real work. You will have access to the environment and browser.
 * User: Paul Marrington
 * Date: 14/01/11
 * Time: 3:54 PM
 */
abstract class JavaActor {
	/**
	 * Environment as documented in /uSDLC/TechnicalArchitecture/Actors/
	 */
	protected HashMap env = Environment.data()
	/**
	 * Instance of browser object for sending results back.
	 * @see HtmlBuilder
	 */
	protected HtmlBuilder doc = env.doc
	/**
	 * Called by uSDLC to start a java actor. It will compile the source if it is out of date, load the class,
	 * instantiate it and call the runScript() method.
	 * @return True if the run worked
	 */
	static run(String script) {
		try {
			// load, instantiate and run - all in one.
			JavaClassLoader.instance.loadClass(new Filer(script)).newInstance().runScript()
		} catch (e) {
			e.printStackTrace()
			return false
		}
		return true
	}

	public static class JavaClassLoader extends ClassLoader {
		JavaClassLoader() {
			super(JavaClassLoader.class.classLoader)
		}

		@Lazy static JavaClassLoader instance = new JavaClassLoader()

		public Class loadClass(Filer filer) {
			javaFile = filer
			return findClass(filer.basePath.replaceAll('/', '.'))
		}

		private Filer javaFile

		public Class loadClass(String name) {
			javaFile = new Filer(name.replaceAll('.', '/') + '.java')
			return findClass(name)
		}

		public Class findClass(String name) {
			// For efficiency we can assume a loaded class won't change - but will it?
			if (!Config.web.allwaysCheckForRecompile && name in classes) {
				return (Class) classes[name]
			}
			return readClass(name)
		}

		private classes = [:]

		private readClass(name) {
			def classFile = new Filer(javaFile.basePath + ".class")
			def found = classes[name]
			// If there is source and the source is newer than the class (or there is no class)...
			if (!javaFile.store.exists()) {
				// NO java - try for a pre-compiled class file
				found = findSystemClass(name.replaceAll('/', '.'))
			} else if (needsCompile(javaFile, classFile)) {
				// Java is newer than class file generated
				classes.remove(name)    // force a reload
				found = ClassReloader.instance(this, classFile.contents = compile(name))
			} else if (!found) {   // read from disk if not already cached
				// class is newer than java - read it from disk
				found = ClassReloader.instance(this, classFile.contents)
			}
			return classes[name] = found
		}
		/**
		 * A special class-loader we can throw away every time we want to recompile and use a java class.
		 */
		static class ClassReloader extends JavaClassLoader {
			ClassReloader(JavaClassLoader parent) { super() }

			static instance(parent, buffer) {
				return new ClassReloader(parent).defineClass(buffer, 0, buffer.length)
			}
		}
		/**
		 * Check to see if a recompile is on the cards.
		 * @param javaFile Name of source
		 * @param classFile Name of resulting byte-code
		 * @return True if we need to recompile
		 */
		private needsCompile(javaFile, classFile) {
			if (!classFile.store.exists()) { return true }    // no class - must compile
			// Lastly, check to see if source is more recent than class - if so, must compile
			return javaFile.store.lastModified() > classFile.store.lastModified()

		}
		/**
		 * Use the compile API to recompile the file to disk as a class file. Strangely enough we do this with a groovy script - so we can steal the classpath.
		 * @return true if compile behaved.
		 private compile(name) {
		 Filer script = Store.template('java/compile.groovy')
		 Binding binding = [actor : this]
		 gse.run script.path, binding
		 return binding.classBuffer
		 }
		 */
		private compile(name) {
			def classFileManager = new ClassFileManager(compiler, JavaClassLoader.instance)
			def options = ["-classpath", Config.classPathString]
			def unitsToCompile = [new JavaFileObjects.FromString(javaFile.path, new String(javaFile.contents))]
			def ok = compiler.getTask(null, classFileManager, null, options, null, unitsToCompile).call()
			return ok ? classFileManager.classObject.bytes : []
		}

		@Lazy def compiler = ToolProvider.systemJavaCompiler

		@Lazy static gse = new GroovyScriptEngine(Config.classPath as String[])
	}
}
