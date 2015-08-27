/*
 * opsu! - an open-source osu! client
 * Copyright (C) 2014, 2015 Jeffrey Han
 *
 * opsu! is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * opsu! is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with opsu!.  If not, see <http://www.gnu.org/licenses/>.
 */

package itdelatrisu.opsu;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.CodeSource;
import java.util.Enumeration;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * Native loader, based on the JarSplice launcher.
 *
 * @author http://ninjacave.com
 */
public class NativeLoader {
	/** Directory where natives are unpacked. */
	public static final File NATIVE_DIR = new File("Natives/");

	/**
	 * Unpacks natives for the current operating system to the natives directory.
	 * @throws IOException
	 */
	public void loadNatives() throws IOException {
		if (!NATIVE_DIR.exists())
			NATIVE_DIR.mkdir();

		CodeSource src = NativeLoader.class.getProtectionDomain().getCodeSource();
		if (src != null) {
			URI jar = null;
			try {
				jar = src.getLocation().toURI();
			} catch (URISyntaxException e) {
				e.printStackTrace();
				return;
			}

			JarFile jarFile = new JarFile(new File(jar), false);
			Enumeration<JarEntry> entries = jarFile.entries();

			while (entries.hasMoreElements()) {
				JarEntry e = entries.nextElement();
				if (e == null)
					break;

				File f = new File(NATIVE_DIR, e.getName());
				if (isNativeFile(e.getName()) && !e.isDirectory() && e.getName().indexOf('/') == -1 && !f.exists()) {
					InputStream in = jarFile.getInputStream(jarFile.getEntry(e.getName()));
					OutputStream out = new FileOutputStream(f);

					byte[] buffer = new byte[65536];
					int bufferSize;
					while ((bufferSize = in.read(buffer, 0, buffer.length)) != -1)
						out.write(buffer, 0, bufferSize);

					in.close();
					out.close();
				}
			}

			jarFile.close();
		}
	}

	/**
	 * Returns whether the given file name is a native file for the current operating system.
	 * @param entryName the file name
	 * @return true if the file is a native that should be loaded, false otherwise
	 */
	private boolean isNativeFile(String entryName) {
		String osName = System.getProperty("os.name");
		String name = entryName.toLowerCase();

		if (osName.startsWith("Win")) {
			if (name.endsWith(".dll"))
				return true;
		} else if (osName.startsWith("Linux")) {
			if (name.endsWith(".so"))
				return true;
		} else if (((osName.startsWith("Mac")) || (osName.startsWith("Darwin"))) && ((name.endsWith(".jnilib")) || (name.endsWith(".dylib")))) {
			return true;
		}

		return false;
	}
}