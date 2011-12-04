/* ZipSelfExtractor.java */
/* Author: Z.S. Jin
 Updates: John D. Mitchell
 Converted to launcher for uSDLC: Paul Marrington */
package usdlc.server.standalone;

import org.codehaus.groovy.tools.RootLoader;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileFilter;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Date;
import java.util.Enumeration;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

@SuppressWarnings("MethodWithTooExceptionsDeclared")
public class UsdlcJarLauncher {
	private String myClassName = "";
	private String version = "";

	public static void main(String... args) throws
		ClassNotFoundException, IOException, InvocationTargetException,
		NoSuchMethodException, IllegalAccessException {
		new UsdlcJarLauncher().go(args);
	}

	@SuppressWarnings("IOResourceOpenedButNotSafelyClosed")
	private void go(String... args) throws
		InvocationTargetException, IOException, ClassNotFoundException,
		NoSuchMethodException, IllegalAccessException {
		version = getClass().getPackage().getSpecificationVersion();
		System.out.println("uSDLC release " + version);
		File jarFile = new File(getJarFileName());
		ZipFile archive = new ZipFile(jarFile);
		if (isNewVersion(archive)) {
			System.out.println("Updating uSDLC...");
			extract(archive);

			File optionalFile = new File("uSDLC-optional-jars.zip");
			if (optionalFile.lastModified() >= jarFile.lastModified()) {
				System.out.println("Updating Optional Jars...");
				archive.close();
				archive = new ZipFile(optionalFile);
				extract(archive);
			}
		}
		archive.close();

		String javaHomeProperty = System.getProperty("java.home");
		String toolsProperty = javaHomeProperty + "/../lib/tools.jar";
		System.setProperty("tools.jar", toolsProperty);
		String cd = new File(".").getAbsolutePath();
		String groovyHomeProperty = cd + "/lib/jars/required";
		System.setProperty("groovy.home", groovyHomeProperty);

		URLClassLoader loader = new RootLoader(classpaths(),
			ClassLoader.getSystemClassLoader());
		Thread.currentThread().setContextClassLoader(loader);
		String mainClassName = "usdlc.server.standalone.server";
		Class<?> mainClass = loader.loadClass(mainClassName);
		mainClass.
			getMethod("main", new Class[]{String[].class}).
			invoke(null, new Object[]{args});
	}

	private static URL[] classpaths() throws MalformedURLException {
		File[] files = new File("web/lib/jars").listFiles(new JarFilter());
		int items = files.length;
		URL[] classpath = new URL[items + 1];
		classpath[0] = new File("bin").toURI().toURL();
		for (int i = 0; i < items; i++) {
			classpath[i + 1] = files[i].toURI().toURL();
		}
		return classpath;
	}

	private static class JarFilter implements FileFilter {
		JarFilter() {}

		@Override
		public boolean accept(File file) {
			return file.getName().endsWith(".jar");
		}
	}

	private String getJarFileName() {
		myClassName = COMPILE.matcher(getClass().getName())
			.replaceAll("/") + ".class";
		URL urlJar = ClassLoader.getSystemResource(myClassName);
		String urlStr = urlJar.toString();
		int from = "jar:file:".length();
		int to = urlStr.indexOf("!/");
		return urlStr.substring(from, to);
	}

	private static final Pattern COMPILE = Pattern.compile("\\.");

	private boolean isNewVersion(ZipFile zf) {
		return getOutFile(zf.getEntry("META-INF/MANIFEST.MF")) != null;
	}

	private File getOutFile(ZipEntry entry) {
		File outFile = null;
		if (!entry.isDirectory()) {
			String pathname = entry.getName();
			if (!myClassName.equals(pathname)) {
				Date archiveTime = new Date(entry.getTime());
				outFile = new File(pathname);
				if (outFile.exists()) {
					Date targetTime = new Date(outFile.lastModified());
					boolean older = archiveTime.before(targetTime);
					boolean same = archiveTime.equals(targetTime);
					if (same || older) {
						outFile = null;
					}
				}
			}
		}
		return outFile;
	}

	@SuppressWarnings("MethodWithMoreThanThreeNegations")
	private void extract(ZipFile zf) throws IOException {
		File outFile = null;
		BufferedWriter log = new BufferedWriter(
			new FileWriter("install.log", true));
		try {
			log.write("uSDLC " + version + " installed " + new Date() + '\n');

			Enumeration<? extends ZipEntry> entries = zf.entries();

			while (entries.hasMoreElements()) {
				ZipEntry entry = entries.nextElement();
				outFile = getOutFile(entry);
				if (outFile != null) {
					log.write('\t' + outFile.getPath() + '\n');

					File parent = outFile.getParentFile();
					if ((parent != null) && !parent.exists()) {
						parent.mkdirs();
					}
					InputStream in = zf.getInputStream(entry);
					FileOutputStream out = new FileOutputStream(outFile);
					try {
						extractFile(out, in);
					} finally {
						out.close();
					}
					outFile.setLastModified(entry.getTime());
				}
			}
		} catch (IOException ioe) {
			if (outFile != null) { outFile.delete(); }
			throw ioe;
		} finally {
			log.close();
		}
	}

	private static void extractFile(FileOutputStream out, InputStream in)
		throws IOException {
		byte[] buf = new byte[1024];
		int nRead = in.read(buf, 0, buf.length);
		while (nRead > 0) {
			out.write(buf, 0, nRead);
			nRead = in.read(buf, 0, buf.length);
		}
		in.close();
	}

	@Override
	public String toString() {
		return "UsdlcJarLauncher{" +
			"myClassName='" + myClassName + '\'' +
			", version='" + version + '\'' +
			'}';
	}
}
