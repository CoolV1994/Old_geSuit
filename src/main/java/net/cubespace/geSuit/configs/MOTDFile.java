package net.cubespace.geSuit.configs;

import net.cubespace.geSuit.geSuit;

import java.io.*;

public class MOTDFile {
	private File mFile;
	private String mMOTD;

	public MOTDFile(String name) {
		mFile = new File(geSuit.instance.getDataFolder(), name);
	}

	public void init() {
		try {
			// Save the default motd file
			if (!mFile.exists()) {
				InputStream stream = geSuit.instance.getResourceAsStream(mFile.getName());
				FileOutputStream out = new FileOutputStream(mFile);
				byte[] buffer = new byte[2048];
				int read = 0;
				while ((read = stream.read(buffer)) != -1) {
					out.write(buffer, 0, read);
				}

				out.close();
				stream.close();
			}

			StringBuilder builder = new StringBuilder();
			BufferedReader reader = new BufferedReader(new FileReader(mFile));
			while (reader.ready()) {
				if (builder.length() != 0) {
					builder.append('\n');
				}
				builder.append(reader.readLine());
			}

			reader.close();
			mMOTD = builder.toString();
		} catch (IOException e) {
			e.printStackTrace();
			mMOTD = "";
		}
	}

	public String getMOTD() {
		return mMOTD;
	}
}
