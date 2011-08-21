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
package usdlc.actor

import javax.tools.ToolProvider
import usdlc.CompilingClassLoader
import usdlc.Store
import usdlc.actor.JavaFileObjects.ClassFileManager
import static usdlc.Config.config

/**
 * Java actor super-class. Implement run() in your sub-class to do the real work. You will have access to the environment and browser.
 * User: Paul Marrington
 * Date: 14/01/11
 * Time: 3:54 PM
 */
class JavaActor extends Actor {
	/**
	 * Called by uSDLC to start a java actor. It will compile the source if it is out of date, load the class,
	 * instantiate it and call the run() method. Class needs to have a constructor
	 * that receives one Map parameter for the environment.
	 */
	void run() { javaClassLoader.newInstance(exchange.store.absolutePath) }

	static CompilingClassLoader javaClassLoader = new CompilingClassLoader('java', new JavaCompiler())

	private static class JavaCompiler implements CompilingClassLoader.Compiler {
		/**
		 * Use the compile API to recompile the file to disk as a class file. Strangely enough we do this with a groovy script - so we can steal the classpath.
		 * @return true if compile behaved.
		 private compile(name) {
		 Filer script = Store.template('java/compile.groovy')
		 Binding context = [actor : this]
		 gse.run script.path, context
		 return context.classBuffer
		 }
		 */
		@Override
		void compile(Store source) {
			ClassFileManager classFileManager = new ClassFileManager(javaCompiler, javaClassLoader)
			def options = ['-classpath', config.classPathString]
			def unitsToCompile = [new JavaFileObjects.FromString(source.absolutePath, source.text())]
			javaCompiler.getTask(null, classFileManager, null, options, null, unitsToCompile).call()
		}

		javax.tools.JavaCompiler javaCompiler = ToolProvider.systemJavaCompiler
	}
}
