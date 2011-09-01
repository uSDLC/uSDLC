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

class CompilingClassLoader extends ClassLoader {
	public interface Compiler {
		void compile(Store sourceFile)
	}

	public CompilingClassLoader(String sourceExt, Compiler compiler) {
		super(CompilingClassLoader.class.classLoader)
		dotSourceExt = '.' + sourceExt;
		this.compiler = compiler;
	}

	public void newInstance(String script) {
		try {
			loadClass(script).constructor.newInstance();
		} catch (e) {
			throw new RuntimeException(e);
		}
	}

	private final String dotSourceExt;
	private final Compiler compiler;

	@SuppressWarnings("unchecked")
	public Class loadClass(String name) { return findClass(name); }

	@SuppressWarnings("unchecked")
	public Class findClass(String name) {
		Store sourceFile = Store.base(name.replace('.', '/') + dotSourceExt);
		Class<?> classReference;
		Store classFile = Store.base(basePath(sourceFile) + '.class');
		if (sourceFile.exists()) {
			if (sourceFile.newer(classFile)) compiler.compile(sourceFile);
			byte[] contents = classFile.contents;
			CompilingClassLoader instance = new CompilingClassLoader(dotSourceExt, compiler);
			classReference = instance.defineClass(name, contents, 0, contents.length);
		} else {
			// no source...
			classReference = findSystemClass(name.replace('/', '.'));
		}
		return classReference;
	}

	private static String basePath(Store store) {
		String path = store.path;
		return path.substring(0, path.length() - store.fullExt.length() - 2);
	}
}
