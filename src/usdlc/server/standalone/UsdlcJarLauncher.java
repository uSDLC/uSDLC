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
import java.io.InputStream;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class UsdlcJarLauncher {
	private      String myClassName = null;
	static final String MANIFEST    = "META-INF/MANIFEST.MF";
	private      String version     = null;

	public static void main(String... args) {
		new UsdlcJarLauncher().go(args);
	}

	private void go(String[] args) {
		ZipFile archive;
		try {
			version = this.getClass().getPackage().getSpecificationVersion();
			System.out.println("uSDLC release " + version);
			archive = new ZipFile(new File(getJarFileName()));
			if (newVersion(archive)) {
				extract(archive);
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
			Method m = mainClass.getMethod("main", new Class[]{String[].class});
			m.invoke(null, new Object[]{args});
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	private URL[] classpaths() throws Exception {
		ArrayList<URL> list = new ArrayList<URL>();
		list.add(new File("bin").toURI().toURL());
		list.addAll(lib("required"));
		list.addAll(lib("optional"));
		URL[] classpath = new URL[list.size()];
		return list.toArray(classpath);
	}

	private List<URL> lib(String dir) {
		List<URL> list = new ArrayList<URL>();
		new File("web/lib/jars", dir).listFiles(new JarFilter(list));
		return list;
	}

	static class JarFilter implements FileFilter {
		private List<URL> list;

		JarFilter(List<URL> list) {
			this.list = list;
		}

		@Override
		public boolean accept(File file) {
			if (file.getName().endsWith(".jar")) {
				try {
					list.add(file.toURI().toURL());
				} catch (MalformedURLException e) {
					throw new RuntimeException(e);
				}
			}
			return false;
		}
	}

	private String getJarFileName() {
		myClassName = this.getClass().getName().replaceAll("\\.", "/") + ".class";
		URL urlJar = ClassLoader.getSystemResource(myClassName);
		String urlStr = urlJar.toString();
		int from = "jar:file:".length();
		int to = urlStr.indexOf("!/");
		return urlStr.substring(from, to);
	}

	private boolean newVersion(ZipFile zf) {
		return getOutFile(zf.getEntry(MANIFEST)) != null;
	}

	private File getOutFile(ZipEntry entry) {
		if (!entry.isDirectory()) {
			String pathname = entry.getName();
			if (!myClassName.equals(pathname)) {
				Date archiveTime = new Date(entry.getTime());
				File outFile = new File(pathname);
				if (!outFile.exists() ||
				    archiveTime.after(new Date(outFile.lastModified()))) {
					return outFile;
				}
			}
		}
		return null;
	}

	public void extract(ZipFile zf) throws Exception {
		byte[] buf = new byte[1024];

		FileOutputStream out = null;
		InputStream in = null;
		File outFile = null;
		System.out.println("Updating uSDLC...");

		try {
			BufferedWriter log = new BufferedWriter(
					                                       new FileWriter("install.log",
					                                                      true));
			log.write("uSDLC " + version + " installed " + new Date().toString()
			          + "\n");

			Enumeration<? extends ZipEntry> entries = zf.entries();

			while (entries.hasMoreElements()) {
				ZipEntry entry = (ZipEntry) entries.nextElement();
				outFile = getOutFile(entry);
				if (outFile == null)
					continue;
				log.write('\t' + outFile.getPath() + '\n');

				File parent = outFile.getParentFile();
				if (parent != null && !parent.exists()) {
					parent.mkdirs();
				}
				in = zf.getInputStream(entry);
				out = new FileOutputStream(outFile);

				while (true) {
					int nRead = in.read(buf, 0, buf.length);
					if (nRead <= 0)
						break;
					out.write(buf, 0, nRead);
				}
				in.close();
				out.close();
				outFile.setLastModified(entry.getTime());
			}
			log.close();
		} catch (Exception e) {
			if (out != null)
				out.close();
			if (outFile != null)
				outFile.delete();
			throw e;
		}
	}

	@Override
	public String toString() {
		return "UsdlcJarLauncher{" +
		       "myClassName='" + myClassName + '\'' +
		       ", version='" + version + '\'' +
		       '}';
	}
}
