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
package usdlc.actors

import javax.tools.ToolProvider
import usdlc.Config
import usdlc.Filer
import usdlc.actors.JavaFileObjects.ClassFileManager

/**
 * Java actor super-class. Implement run() in your sub-class to do the real work. You will have access to the environment and browser.
 * User: Paul Marrington
 * Date: 14/01/11
 * Time: 3:54 PM
 */
class JavaActor {
	/**
	 * Called by uSDLC to start a java actor. It will compile the source if it is out of date, load the class,
	 * instantiate it and call the run() method. Class needs to have a constructor
	 * that receives one Map parameter for the environment.
	 *
	 * public class MyActor {*     MyActor(Map env) { ... }*}*/
	public void run(String script) {
		JavaClassLoader.instance.loadClass(new Filer(script)).newInstance(env.doc)
	}

	public static class JavaClassLoader extends ClassLoader {
		JavaClassLoader() {
			super(JavaClassLoader.class.classLoader)
		}

		@Lazy static JavaClassLoader instance = new JavaClassLoader()

		public Class loadClass(Filer filer) {
			return _loadClass(filer, basePath(filer).replaceAll('/', '.'))
		}

		private Filer javaFile

		public Class loadClass(String name) {
			return _loadClass(new Filer(name.replaceAll('.', '/') + '.java'), name)
		}

		private Class _loadClass(Filer filer, String className) {
			javaFile = filer
			return findClass(className)
		}

		public Class findClass(String name) {
			// For efficiency we can assume a loaded class won't change - but will it?
			Class classReference
			if (Config.allwaysCheckForRecompile && name in classes) {
				classReference = readClass(name)
			} else {
				classReference = (Class) classes[name]
			}
			return classReference
		}

		private classes = [:]

		private readClass(name) {
			def classFile = new Filer(basePath(javaFile) + ".class")
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

		private String basePath(Filer filer) {
			return filer.store.path[0..-(filer.fullExt.size() + 2)]
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
			boolean needsCompile
			if (classFile.store.exists()) {
				// Check to see if source is more recent than class - if so, must compile
				needsCompile = javaFile.store.lastModified() > classFile.store.lastModified()
			} else {
				needsCompile = true    // no class - must compile
			}
			return needsCompile
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
