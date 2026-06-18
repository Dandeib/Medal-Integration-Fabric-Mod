package com.dandeib.client;

import com.dandeib.MedalIntegrationFabric;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class ModConfig {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path FILE = FabricLoader.getInstance().getConfigDir().resolve("medal-integration.json");

    private static ModConfig INSTANCE = new ModConfig();

    // --- Settings ---
    public boolean enabled = true;
    public int clipDuration = 30;   // seconds
    public int clipDelayMs = 10;    // milliseconds after the kill event

    public static ModConfig get() {
        return INSTANCE;
    }

    public static void load() {
        if (!Files.exists(FILE)) {
            save();
            return;
        }
        try {
            ModConfig loaded = GSON.fromJson(Files.readString(FILE), ModConfig.class);
            if (loaded != null) {
                INSTANCE = loaded;
            }
        } catch (IOException e) {
            MedalIntegrationFabric.LOGGER.error("[Medal-Integration] Failed to load config, using defaults.", e);
        }
    }

    public static void save() {
        try {
            Files.writeString(FILE, GSON.toJson(INSTANCE));
        } catch (IOException e) {
            MedalIntegrationFabric.LOGGER.error("[Medal-Integration] Failed to save config.", e);
        }
    }
}
