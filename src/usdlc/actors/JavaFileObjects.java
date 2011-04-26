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

package usdlc.actors;

import javax.tools.*;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.util.regex.Pattern;

/**
 * User: Paul Marrington
 * Date: 23/01/11
 * Time: 1:01 PM
 */
@SuppressWarnings({"StringConcatenation", "ImplicitCallToSuper", "HardcodedFileSeparator"})
public class JavaFileObjects {
	private JavaFileObjects() {
	}

	@SuppressWarnings({"PublicInnerClass"})
	public static class FromString extends SimpleJavaFileObject {
		public FromString(CharSequence path, String contents) {
			//noinspection HardcodedFileSeparator
			super(URI.create(normaliseUri(path)), Kind.SOURCE);
			code = contents;
		}

		@Override
		public CharSequence getCharContent(boolean ignoreEncodingErrors) {
			return code;
		}

		private final String code;
	}

	@SuppressWarnings({"PublicInnerClass", "RawUseOfParameterizedType", "InstanceVariableOfConcreteClass", "InstanceVariableMayNotBeInitialized", "unchecked", "PublicField", "ParameterHidesMemberVariable", "UnnecessarilyQualifiedInnerClassAccess"})
	public static class ClassFileManager extends ForwardingJavaFileManager {
		/**
		 * Instance of JavaClassObject that will store the compiled byte-code of our class
		 */
		public JavaClassObject classObject;
		private final ClassLoader classLoader;

		/**
		 * Will initialize the manager with the specified standard java file manager
		 *
		 * @param compiler    for compiler.getStandardFileManager(null, null, null)
		 * @param classLoader used to load this class
		 */
		public ClassFileManager(JavaCompiler compiler, ClassLoader classLoader) {
			super(compiler.getStandardFileManager(null, null, null));
			this.classLoader = classLoader;
		}

		/**
		 * Will be used by us to get the class loader for our compiled class.
		 */
		@Override
		public ClassLoader getClassLoader(Location location) {
			return classLoader;
		}

		/**
		 * Gives the compiler an instance of the JavaClassObject so that the compiler can write the byte code into it.
		 */
		@Override
		public JavaFileObject getJavaFileForOutput(Location location, String className, JavaFileObject.Kind kind, FileObject sibling) throws IOException {
			classObject = new JavaClassObject(className, kind);
			return classObject;
		}
	}

	@SuppressWarnings({"PublicInnerClass", "ParameterHidesMemberVariable", "PublicMethodNotExposedInInterface", "ProtectedField"})
	public static class JavaClassObject extends SimpleJavaFileObject {
		/**
		 * Registers the compiled class object under URI
		 * containing the class full name
		 *
		 * @param name Full name of the compiled class
		 * @param kind Kind of the data. It will be CLASS in our case
		 */
		public JavaClassObject(CharSequence name, Kind kind) {
			super(URI.create(normaliseUri(name)), kind);
		}

		/**
		 * Will be used by our file manager to get the byte code that
		 * can be put into memory to instantiate our class
		 *
		 * @return compiled byte code
		 */
		public byte[] getBytes() {
			return buffer.toByteArray();
		}

		/**
		 * Will provide the compiler with an output stream that leads
		 * to our byte array. This way the compiler will write everything
		 * into the byte array that we will instantiate later
		 */
		@Override
		public OutputStream openOutputStream() throws IOException {
			return buffer;
		}

		protected final ByteArrayOutputStream buffer = new ByteArrayOutputStream();
	}

	static String normaliseUri(CharSequence uri) {
		return "file:///" + slosh.matcher(uri).replaceAll("/");
	}

	private static final Pattern slosh = Pattern.compile("\\\\");
}
