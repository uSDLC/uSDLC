package usdlc

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

class CompilingClassLoader {
	public interface Compiler {
		void compile(Filer sourceFile)

		;
	}

	public CompilingClassLoader(String sourceExt, Compiler compiler) {
		super(CompilingClassLoader.class.classLoader);
		dotSourceExt = '.' + sourceExt;
		this.compiler = compiler;
	}

	public void newInstance(String script) {
		try {
			loadClass(script).constructor.newInstance();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	private final String dotSourceExt;
	private final Compiler compiler;

	public Class loadClass(String name) {
		return findClass(name);
	}

	public Class findClass(String name) {
		Filer sourceFile = new Filer(name.replace('.', '/') + dotSourceExt);
		Class<?> classReference;
		Filer classFile = new Filer(basePath(sourceFile) + ".class");
		if (sourceFile.store.exists()) {
			if (needsCompile(sourceFile, classFile)) compiler.compile(sourceFile);
			byte[] contents = classFile.contents;
			CompilingClassLoader instance = new CompilingClassLoader(dotSourceExt, compiler);
			classReference = instance.defineClass(name, contents, 0, contents.length);
		} else {
			// no source...
			classReference = findSystemClass(name.replace('/', '.'));
		}
		return classReference;
	}

	private static String basePath(Filer filer) {
		String path = filer.store.path;
		return path.substring(0, path.length() - filer.fullExt.length() - 2);
	}

	/**
	 * Check to see if a recompile is on the cards.
	 */
	private static boolean needsCompile(Filer sourceFile, Filer classFile) {
		boolean needsCompile;
		Store classFileStore = classFile.store;
		if (classFileStore.exists()) {
			// Check to see if source is more recent than class - if so, must compile
			needsCompile = sourceFile.store.lastModified() > classFileStore.lastModified();
		} else {
			needsCompile = true;    // no class - must compile
		}
		return needsCompile;
	}
}
