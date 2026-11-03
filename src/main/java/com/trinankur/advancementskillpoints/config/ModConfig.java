package com.trinankur.advancementskillpoints.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.trinankur.advancementskillpoints.AdvancementSkillPoints;
import net.fabricmc.loader.api.FabricLoader;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.List;

public class ModConfig {
	private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
	private static ModConfig INSTANCE = new ModConfig();

	// Attributes
	public int defaultMaxRank = 20;
	public double strengthUnarmedBase = 1.0;
	public double strengthUnarmedPerRank = 0.30;
	public double strengthWeaponMultiplierPerRank = 0.15;
	public double vitalityHealthBase = 20.0;
	public double vitalityHealthPerRank = 3.0;
	public double enduranceReductionPerRank = 0.03;
	public double enduranceMaxReduction = 0.60;
	public int staminaFoodBase = 20;

	// Advancement Weights
	public int taskFrameWeight = 1;
	public int goalFrameWeight = 2;
	public int challengeFrameWeight = 4;

	// Modded Advancement Budget Parameters: floor(scale * N / (N + halfLife))
	public double moddedBudgetScale = 84.0;
	public double moddedBudgetHalfLife = 100.0;

	// Namespace Exclusions & Weapon Overrides
	public List<String> excludedNamespaces = new ArrayList<>();
	public List<String> customRegisteredWeapons = new ArrayList<>();
	public List<String> customExcludedWeapons = new ArrayList<>();

	private static File getConfigFile() {
		try {
			return new File(FabricLoader.getInstance().getConfigDir().toFile(), "advancement-skill-points.json");
		} catch (Throwable t) {
			return new File("config", "advancement-skill-points.json");
		}
	}

	public static ModConfig get() {
		return INSTANCE;
	}

	public static void load() {
		File file = getConfigFile();
		if (file.exists()) {
			try (FileReader reader = new FileReader(file)) {
				ModConfig loaded = GSON.fromJson(reader, ModConfig.class);
				if (loaded != null) {
					loaded.validate();
					INSTANCE = loaded;
					AdvancementSkillPoints.LOGGER.info("Loaded configuration from {}", file.getName());
					return;
				}
			} catch (Exception e) {
				AdvancementSkillPoints.LOGGER.error("Failed to load config, using validated defaults", e);
			}
		}
		INSTANCE = new ModConfig();
		INSTANCE.validate();
		save();
	}

	public static void save() {
		File file = getConfigFile();
		try {
			File parent = file.getParentFile();
			if (parent != null && !parent.exists()) {
				parent.mkdirs();
			}
			try (FileWriter writer = new FileWriter(file)) {
				GSON.toJson(INSTANCE, writer);
			}
		} catch (Exception e) {
			AdvancementSkillPoints.LOGGER.error("Failed to save config to {}", file.getName(), e);
		}
	}

	public void validate() {
		if (defaultMaxRank < 1) defaultMaxRank = 20;
		if (strengthUnarmedBase < 0.0) strengthUnarmedBase = 1.0;
		if (strengthUnarmedPerRank < 0.0) strengthUnarmedPerRank = 0.30;
		if (strengthWeaponMultiplierPerRank < 0.0) strengthWeaponMultiplierPerRank = 0.15;
		if (vitalityHealthBase < 1.0) vitalityHealthBase = 20.0;
		if (vitalityHealthPerRank < 0.0) vitalityHealthPerRank = 3.0;
		if (enduranceReductionPerRank < 0.0) enduranceReductionPerRank = 0.03;
		if (enduranceMaxReduction < 0.0 || enduranceMaxReduction > 1.0) enduranceMaxReduction = 0.60;
		if (staminaFoodBase < 1) staminaFoodBase = 20;
		if (taskFrameWeight < 1) taskFrameWeight = 1;
		if (goalFrameWeight < 1) goalFrameWeight = 2;
		if (challengeFrameWeight < 1) challengeFrameWeight = 4;
		if (moddedBudgetScale < 0.0) moddedBudgetScale = 84.0;
		if (moddedBudgetHalfLife < 1.0) moddedBudgetHalfLife = 100.0;
		if (excludedNamespaces == null) excludedNamespaces = new ArrayList<>();
		if (customRegisteredWeapons == null) customRegisteredWeapons = new ArrayList<>();
		if (customExcludedWeapons == null) customExcludedWeapons = new ArrayList<>();
	}
}
