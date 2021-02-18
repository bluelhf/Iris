package net.coderbot.iris.shaderpack;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.util.*;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import net.coderbot.iris.Iris;
import net.coderbot.iris.gl.texture.InternalTextureFormat;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.texture.NativeImageBackedTexture;
import net.minecraft.util.Identifier;
import net.minecraft.util.InvalidIdentifierException;
import net.minecraft.util.registry.BuiltinRegistries;
import net.minecraft.util.registry.DynamicRegistryManager;
import net.minecraft.util.registry.Registry;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.world.World;
import org.apache.logging.log4j.Level;
import org.jetbrains.annotations.Nullable;

public class ShaderPack {
	private final PackDirectives packDirectives;
	private final ProgramSource gbuffersBasic;
	private final ProgramSource gbuffersBeaconBeam;
	private final ProgramSource gbuffersTextured;
	private final ProgramSource gbuffersTexturedLit;
	private final ProgramSource gbuffersTerrain;
	private final ProgramSource gbuffersDamagedBlock;
	private final ProgramSource gbuffersWater;
	private final ProgramSource gbuffersSkyBasic;
	private final ProgramSource gbuffersSkyTextured;
	private final ProgramSource gbuffersClouds;
	private final ProgramSource gbuffersWeather;
	private final ProgramSource gbuffersEntities;
	private final ProgramSource gbuffersEntitiesGlowing;
	private final ProgramSource gbuffersGlint;
	private final ProgramSource gbuffersEntityEyes;
	private final ProgramSource gbuffersBlock;
	private final ProgramSource[] composite;
	private final ProgramSource compositeFinal;
	private final IdMap idMap;
	private final ShaderPackConfig config;
	private final ShaderProperties shaderProperties;
	private final Map<String, Map<String, String>> langMap;

	private static final Map<RegistryKey<World>, Integer> RAW_ID_DIMS = Util.make(() -> {
		Map<RegistryKey<World>, Integer> m = new HashMap<>();
		m.put(World.NETHER, -1);
		m.put(World.OVERWORLD, 0);
		m.put(World.END, 1);
		return m;
	});

	public static final ShaderPack NO_OP;

	public ShaderPack(@Nullable Path root) throws IOException {
		this.shaderProperties = root != null ? loadProperties(root, "shaders.properties")
				.map(ShaderProperties::new)
				.orElseGet(ShaderProperties::empty) : new ShaderProperties(new Properties());
		this.config = new ShaderPackConfig(Iris.getIrisConfig().getShaderPackName());

		config.load();

		this.packDirectives = new PackDirectives();

		Path shaders = root;

		if(root != null) {
			List<Integer> vanillaDims = new ArrayList<>();
			List<Identifier> newDims = new ArrayList<>();
			Files.walk(root).forEach(p -> {
				if(Files.isDirectory(p)) {
					String name = p.getFileName().toString();
					boolean added = false;
					if(name.startsWith("world-")) {
						String[] idParts = name.replace("world-", "").split("#");
						if(idParts.length == 2) {
							newDims.add(new Identifier(idParts[0], idParts[1]));
							added = true;
						}
					}
					if(name.startsWith("world") && !added) {
						String world = String.copyValueOf(Arrays.copyOfRange(name.toCharArray(), 5, name.length() - 1));
						try {
							vanillaDims.add(Integer.parseInt(world));
						} catch (NumberFormatException ignored) {}
					}
				}
			});
			RegistryKey<World> dim;
			if(MinecraftClient.getInstance().world != null) {
				dim = MinecraftClient.getInstance().world.getRegistryKey();
				if(RAW_ID_DIMS.containsKey(dim) && vanillaDims.contains(RAW_ID_DIMS.get(dim))) {
					shaders = root.resolve("world"+ RAW_ID_DIMS.get(dim));
				} else {
					// TODO: Make a more sustainable and backwards compatible alternative to this
					/*
					 * Custom dimension shaders are stored in a specially named folder, ex: "the_aether:the_aether" would be in folder "world-the_aether#the_aether"
					 * This is because dimension raw ids are way too volatile in post 1.16 versions to be used for identifying dimensions
					 * For legacy shaderpacks with mod support, I might add a way the user can configure which id goes to which registered dimension
					 */
					Identifier id = dim.getValue();
					if(newDims.contains(id)) {
						shaders = root.resolve("world-"+id.getNamespace()+"#"+id.getPath());
					}
				}
			}
		}

		this.gbuffersBasic = readProgramSource(root, shaders, "gbuffers_basic", this);
		this.gbuffersBeaconBeam = readProgramSource(root, shaders, "gbuffers_beaconbeam", this);
		this.gbuffersTextured = readProgramSource(root, shaders, "gbuffers_textured", this);
		this.gbuffersTexturedLit = readProgramSource(root, shaders, "gbuffers_textured_lit", this);
		this.gbuffersTerrain = readProgramSource(root, shaders, "gbuffers_terrain", this);
		this.gbuffersDamagedBlock = readProgramSource(root, shaders, "gbuffers_damagedblock", this);
		this.gbuffersWater = readProgramSource(root, shaders, "gbuffers_water", this);
		this.gbuffersSkyBasic = readProgramSource(root, shaders, "gbuffers_skybasic", this);
		this.gbuffersSkyTextured = readProgramSource(root, shaders, "gbuffers_skytextured", this);
		this.gbuffersClouds = readProgramSource(root, shaders, "gbuffers_clouds", this);
		this.gbuffersWeather = readProgramSource(root, shaders, "gbuffers_weather", this);
		this.gbuffersEntities = readProgramSource(root, shaders, "gbuffers_entities", this);
		this.gbuffersEntitiesGlowing = readProgramSource(root, shaders, "gbuffers_entities_glowing", this);
		this.gbuffersGlint = readProgramSource(root, shaders, "gbuffers_armor_glint", this);
		this.gbuffersEntityEyes = readProgramSource(root, shaders, "gbuffers_spidereyes", this);
		this.gbuffersBlock = readProgramSource(root, shaders, "gbuffers_block", this);

		this.composite = new ProgramSource[16];

		for (int i = 0; i < this.composite.length; i++) {
			String suffix = i == 0 ? "" : Integer.toString(i);

			this.composite[i] = readProgramSource(root, shaders, "composite" + suffix, this);
		}

		this.compositeFinal = readProgramSource(root, shaders, "final", this);

		this.idMap = new IdMap(root);
		this.langMap = parseLangEntries(root);

		config.save();
	}

	// TODO: Copy-paste from IdMap, find a way to deduplicate this
	private static Optional<Properties> loadProperties(Path shaderPath, String name) {
		Properties properties = new Properties();

		try {
			properties.load(Files.newInputStream(shaderPath.resolve(name)));
		} catch (IOException e) {
			Iris.logger.debug("An " + name + " file was not found in the current shaderpack");

			return Optional.empty();
		}

		return Optional.of(properties);
	}

	public IdMap getIdMap() {
		return idMap;
	}

	public Optional<ProgramSource> getGbuffersBasic() {
		return gbuffersBasic.requireValid();
	}

	public Optional<ProgramSource> getGbuffersBeaconBeam() {
		return gbuffersBeaconBeam.requireValid();
	}

	public Optional<ProgramSource> getGbuffersTextured() {
		return gbuffersTextured.requireValid();
	}

	public Optional<ProgramSource> getGbuffersTexturedLit() {
		return gbuffersTexturedLit.requireValid();
	}

	public Optional<ProgramSource> getGbuffersTerrain() {
		return gbuffersTerrain.requireValid();
	}

	public Optional<ProgramSource> getGbuffersDamagedBlock() {
		return gbuffersDamagedBlock.requireValid();
	}

	public Optional<ProgramSource> getGbuffersWater() {
		return gbuffersWater.requireValid();
	}

	public Optional<ProgramSource> getGbuffersSkyBasic() {
		return gbuffersSkyBasic.requireValid();
	}

	public Optional<ProgramSource> getGbuffersSkyTextured() {
		return gbuffersSkyTextured.requireValid();
	}

	public Optional<ProgramSource> getGbuffersClouds() {
		return gbuffersClouds.requireValid();
	}

	public Optional<ProgramSource> getGbuffersWeather() {
		return gbuffersWeather.requireValid();
	}

	public Optional<ProgramSource> getGbuffersEntities() {
		return gbuffersEntities.requireValid();
	}

	public Optional<ProgramSource> getGbuffersEntitiesGlowing() {
		return gbuffersEntitiesGlowing.requireValid();
	}

	public Optional<ProgramSource> getGbuffersGlint() {
		return gbuffersGlint.requireValid();
	}

	public Optional<ProgramSource> getGbuffersEntityEyes() {
		return gbuffersEntityEyes.requireValid();
	}

	public Optional<ProgramSource> getGbuffersBlock() {
		return gbuffersBlock.requireValid();
	}

	public ProgramSource[] getComposite() {
		return composite;
	}

	public Optional<ProgramSource> getCompositeFinal() {
		return compositeFinal.requireValid();
	}

	public Map<String, Map<String, String>> getLangMap() {
		return langMap;
	}

	public PackDirectives getPackDirectives() {
		return packDirectives;
	}

	public ShaderPackConfig getConfig() {
		return config;
	}

	public ShaderProperties getShaderProperties() {
		return shaderProperties;
	}

	private static ProgramSource readProgramSource(Path root, Path shaders, String program, ShaderPack pack) throws IOException {
		String vertexSource = null;
		String fragmentSource = null;

		if(root == null || shaders == null) {
			return new ProgramSource(program, null, null, pack);
		}

		try {
			Path vertexPath = shaders.resolve(program + ".vsh");
			vertexSource = readFile(vertexPath);

			if (vertexSource != null) {
				vertexSource = ShaderPreprocessor.process(root, vertexPath, vertexSource, pack.getConfig());
			}
		} catch (IOException e) {
			// TODO: Better handling?
			throw e;
		}

		try {
			Path fragmentPath = shaders.resolve(program + ".fsh");
			fragmentSource = readFile(fragmentPath);

			if (fragmentSource != null) {
				fragmentSource = ShaderPreprocessor.process(root, fragmentPath, fragmentSource, pack.getConfig());
			}
		} catch (IOException e) {
			// TODO: Better handling?
			throw e;
		}

		return new ProgramSource(program, vertexSource, fragmentSource, pack);
	}

	private static String readFile(Path path) throws IOException {
		try {
			return new String(Files.readAllBytes(path), StandardCharsets.UTF_8);
		} catch (FileNotFoundException | NoSuchFileException e) {
			return null;
		}
	}

	private Map<String, Map<String, String>> parseLangEntries(Path root) throws IOException {
		if(root == null) return new HashMap<>();

		Path langFolderPath = root.resolve("lang");
		Map<String, Map<String, String>> allLanguagesMap = new HashMap<>();

		if (!Files.exists(langFolderPath)) {
			return allLanguagesMap;
		}
		//We are using a max depth of one to ensure we only get the surface level *files* without going deeper
		// we also want to avoid any directories while filtering
		//Basically, we want the immediate files nested in the path for the langFolder
		//There is also Files.list which can be used for similar behavior
		Files.walk(langFolderPath, 1).filter(path -> !Files.isDirectory(path)).forEach(path -> {

			Map<String, String> currentLanguageMap = new HashMap<>();
			//some shaderpacks use optifines file name coding which is different than minecraft's.
			//An example of this is using "en_US.lang" compared to "en_us.json"
			//also note that optifine uses a property scheme for loading language entries to keep parity with other optifine features
			String currentFileName = path.getFileName().toString().toLowerCase();
			String currentLangCode = currentFileName.substring(0, currentFileName.lastIndexOf("."));
			Properties properties = new Properties();

			try {
				properties.load(Files.newInputStream(path));
			} catch (IOException e) {
				Iris.logger.error("Error while parsing languages for shaderpacks! Expected File Path: {}", path);
				Iris.logger.catching(Level.ERROR, e);
			}

			properties.forEach((key, value) -> currentLanguageMap.put(key.toString(), value.toString()));
			allLanguagesMap.put(currentLangCode, currentLanguageMap);
		});

		return allLanguagesMap;
	}

	public static class ProgramSource {
		private final String name;
		private final String vertexSource;
		private final String fragmentSource;
		private final ProgramDirectives directives;
		private final ShaderPack parent;

		public ProgramSource(String name, String vertexSource, String fragmentSource, ShaderPack parent) {
			this.name = name;
			this.vertexSource = vertexSource;
			this.fragmentSource = fragmentSource;
			this.parent = parent;
			this.directives = new ProgramDirectives(this, parent.getShaderProperties());
		}

		public String getName() {
			return name;
		}

		public Optional<String> getVertexSource() {
			return Optional.ofNullable(vertexSource);
		}

		public Optional<String> getFragmentSource() {
			return Optional.ofNullable(fragmentSource);
		}

		public ProgramDirectives getDirectives() {
			return this.directives;
		}

		public ShaderPack getParent() {
			return parent;
		}

		public boolean isValid() {
			return vertexSource != null && fragmentSource != null;
		}

		public Optional<ProgramSource> requireValid() {
			if (this.isValid()) {
				return Optional.of(this);
			} else {
				return Optional.empty();
			}
		}
	}

	static {
		ShaderPack s;
		try {
			s = new ShaderPack(null);
		} catch (IOException e) {
			e.printStackTrace();
			s = null;
		}
		NO_OP = s;
	}
}
