/*
 * This file is part of World Downloader: A mod to make backups of your
 * multiplayer worlds.
 * http://www.minecraftforum.net/forums/mapping-and-modding/minecraft-mods/2520465
 *
 * Copyright (c) 2014 nairol, cubic72
 * Copyright (c) 2018 Pokechu22, julialy
 *
 * This project is licensed under the MMPLv2.  The full text of the MMPL can be
 * found in LICENSE.md, or online at https://github.com/iopleke/MMPLv2/blob/master/LICENSE.md
 * For information about this the MMPLv2, see http://stopmodreposts.org/
 *
 * Do not redistribute (in modified or unmodified form) without prior permission.
 */
package wdl.settings;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import net.minecraft.crash.CrashReportCategory;

/**
 * WDL configuration.  Right now, just a thin wrapper around {@link Properties}.
 */
public class Configuration implements IConfiguration {
	private static final Logger LOGGER = LogManager.getLogger();

	private final IConfiguration parent;
	private final Properties properties;

	public Configuration() {
		this(null);
	}

	public Configuration(@Nullable IConfiguration parent) {
		if (parent != null) {
			this.parent = parent;
		} else {
			this.parent = new DefaultConfiguration();
		}
		this.properties = new Properties();
	}

	@Override
	public <T> void setValue(Setting<T> setting, T value) {
		this.properties.setProperty(setting.getConfigurationKey(), setting.serializeToString(value));
	}

	@Override
	public <T> T getValue(Setting<T> setting, IConfiguration config) {
		Optional<T> override = setting.overrideFromContext(config);
		if (override.isPresent()) {
			return override.get();
		}
		String key = setting.getConfigurationKey();
		if (this.containsKey(key)) {
			String value = this.properties.getProperty(key);
			try {
				T t = setting.deserializeFromString(value);
				if (t == null) {
					throw new NullPointerException("deserializeFromString returned null");
				}
				return t;
			} catch (Exception ex) {
				LOGGER.warn("Failed to deserialize {} from configuration {} with '{}'='{}'", setting, this, key, value, ex);
				LOGGER.warn("Clearing the value and using parent config now...");
				this.clearValue(setting);
			}
		}
		return parent.getValue(setting, config);
	}

	@Override
	public <T> void clearValue(Setting<T> setting) {
		this.remove(setting.getConfigurationKey());
	}

	// Rework slightly, and maybe rename

	public void load(Reader reader) throws IOException {
		this.properties.load(reader);
	}

	public void store(Writer writer, String comments) throws IOException {
		this.properties.store(writer, comments);
	}

	/**
	 * Gets a map of gamerules to values set in this configuration. This includes ones
	 * inherited from the parent.
	 */
	@Override
	public Map<String, String> getGameRules() {
		return this.properties.stringPropertyNames().stream()
				.filter(s -> s.startsWith("GameRule."))
				.collect(Collectors.toMap(
						s -> s.substring("GameRule.".length()),
						s -> getProperty(s)));
	}

	/**
	 * Puts the contents of this configuration into the given crash report category.
	 */
	@Override
	public void addToCrashReport(CrashReportCategory category, String name) {
		if (!properties.isEmpty()) {
			for (Map.Entry<Object, Object> e : properties.entrySet()) {
				if (!(e.getKey() instanceof String)) {
					LOGGER.warn("Non-string key " + e.getKey() + " in " + name);
					continue;
				}
				category.addCrashSection((String)e.getKey(), e.getValue());
			}
		} else {
			category.addCrashSection("-", "empty");
		}
	}

	// Keep but rename

	public void setProperty(String key, String value) {
		this.properties.setProperty(key, value);
	}

	@Nullable
	public String getProperty(String key) {
		return this.properties.getProperty(key);
	}

	@Nonnull
	public String getProperty(String key, @Nonnull String defaultValue) {
		return this.properties.getProperty(key, defaultValue);
	}

	public boolean containsKey(String key) {
		return this.properties.containsKey(key);
	}

	public void remove(String key) {
		this.properties.remove(key);
	}

	// Things to definitely get rid of - smelly

	public void putAll(Configuration conf) {
		this.properties.putAll(conf.properties);
	}
}
