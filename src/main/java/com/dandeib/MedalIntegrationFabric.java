package com.dandeib;

import net.fabricmc.api.ModInitializer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MedalIntegrationFabric implements ModInitializer {
	public static final String MOD_ID = "medal-integration-fabric";

	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		LOGGER.info("Medal Integration loaded.");
	}
}
