package de.vonfelix.tileserver.image;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.core.env.Environment;
import org.yaml.snakeyaml.Yaml;

import de.vonfelix.tileserver.exception.YamlConfigurationException;

public class YamlConfiguration {
	private Environment env;
	private String name;
	static Logger logger = LogManager.getLogger();
	private HashMap<String, Object> configuration = new HashMap<>();

	private Boolean isLoaded = false;

	public YamlConfiguration(String name, Environment env) {
		this.name = name;
	}

	// read image info from YAML
	// check format
	// add tile_size from configuration
	private void loadConfiguration() throws YamlConfigurationException {
		try {
			logger.debug("reading YAML for " + name);

			InputStream input = new FileInputStream(env.getProperty("tilebuilder.source_image_dir") + name + ".yaml");
			Yaml yaml = new Yaml();
			configuration = (HashMap<String, Object>) yaml.load(input);

			HashMap project = (HashMap) ((HashMap<String, Object>) configuration).get("project");

			List yamlstacks = (List) project.get("stacks");

			/*
			 * READ STACKS
			 */
			for (Object s : yamlstacks) {
				HashMap<?, ?> st = (HashMap<?, ?>) s;

				/*
				 * Read Data from the list of "mirrors". If at least one entry
				 * is a mirror with tile source type 9, this is the stack
				 * description for TileBuilder. If no mirror with tile source
				 * type 9 is found, the image is not a valid TileBuilder image
				 * and an error is thrown.
				 */
				List mirrors = (List) st.get("mirrors");
				if (mirrors == null) {
					throw new YamlConfigurationException("Invalid YAML configuration for image '" + name
							+ "': YAML must define a valid 'mirrors' section for each stack.", configuration);
				}

				String id = null;
				String title = null;
				HashMap<String, String> m = null;
				for (Object mirror : mirrors) {
					m = (HashMap) mirror;
					String tileSourceType = (String) m.get("tile_source_type");
					if (tileSourceType.equals(env.getProperty("tilebuilder.tile_source_type", "9"))) {
						id = (String) m.get("folder");
						title = (String) m.get("title");
						break;
					}
				}
				if (m != null && (id == null || title == null)) {
					throw new YamlConfigurationException("Invalid YAML configuration for image '" + name
							+ "': Each stack must have exactly one mirror for tile source type 9 and contain 'folder' and 'title' keys. This is the stack in question: "
							+ mirrors, configuration);
				}
				m.put("tile_width", env.getProperty("tilebuilder.tile_size"));
				m.put("tile_height", env.getProperty("tilebuilder.tile_size"));
			}

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		isLoaded = true;
	}

	public HashMap<String, Object> getConfiguration() {
		if (!isLoaded()) {
			try {
				loadConfiguration();
			} catch (YamlConfigurationException e) {
				logger.error(e.getMessage());
				// logger.debug(e.d);
				System.out.println("caught");
			}
		}
		return configuration;
	}

	public HashMap<String, Object> getProject() {
		return (HashMap<String, Object>) getConfiguration().get("project");
	}

	public Boolean isLoaded() {
		return isLoaded;
	}
}
