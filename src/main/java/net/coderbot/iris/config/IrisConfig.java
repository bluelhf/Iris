package net.coderbot.iris.config;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.util.Identifier;

/**
 * A class dedicated to storing the config values of shaderpacks. Right now it only stores the path to the current shaderpack
 */
public class IrisConfig {
	private static final String COMMENT =
		"This file stores configuration options for Iris, such as the currently active shaderpack";

	/**
	 * The path to the current shaderpack. Null if the internal shaderpack is being used.
	 */
	private String shaderPackName;

	/**
	 * For legacy shader pack support, this maps dimension Identifiers to legacy raw ids defined in shaders
	 */
	private final Map<Identifier, Integer> dimensionOverrides = new HashMap<>();

	private final Path propertiesPath;

	public IrisConfig() {
		shaderPackName = null;
		propertiesPath = FabricLoader.getInstance().getConfigDir().resolve("iris.properties");
	}

	/**
	 * Initializes the configuration, loading it if it is present and creating a default config otherwise.
	 *
	 * @throws IOException file exceptions
	 */
	public void initialize() throws IOException {
		load();
		if (!Files.exists(propertiesPath)) {
			save();
		}
	}

	/**
	 * returns whether or not the current shaderpack is internal
	 *
	 * @return if the shaderpack is internal
	 */
	public boolean isInternal() {
		return shaderPackName == null;
	}

	/**
	 * Returns the name of the current shaderpack
	 *
	 * @return shaderpack name. If internal it returns "(internal)"
	 */
	public String getShaderPackName() {
		if (shaderPackName == null) {
			return "(internal)";
		}

		return shaderPackName;
	}

	/**
	 * loads the config file and then populates the string, int, and boolean entries with the parsed entries
	 *
	 * @throws IOException if the file cannot be loaded
	 */

	public void load() throws IOException {
		if (!Files.exists(propertiesPath)) {
			return;
		}

		Properties properties = new Properties();
		properties.load(Files.newInputStream(propertiesPath));
		shaderPackName = properties.getProperty("shaderPack");

		if (shaderPackName != null && shaderPackName.equals("(internal)")) {
			shaderPackName = null;
		}

		this.dimensionOverrides.clear();
		for(String s : properties.stringPropertyNames()) {
			if(s.startsWith("dimension.")) {
				s = s.replace("dimension.", "");
				Identifier id = Identifier.tryParse(s);
				if(id != null) {
					try {
						int r = Integer.parseInt(properties.getProperty(s));
						this.dimensionOverrides.put(id, r);
					} catch(NumberFormatException ignored) {}
				}
			}
		}
	}

	/**
	 * Serializes the config into a file. Should be called whenever any config values are modified.
	 *
	 * @throws IOException file exceptions
	 */
	public void save() throws IOException {
		Properties properties = new Properties();
		properties.setProperty("shaderPack", getShaderPackName());
		properties.store(Files.newOutputStream(propertiesPath), COMMENT);
	}
}
