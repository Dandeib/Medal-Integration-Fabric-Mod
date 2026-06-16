package com.dandeib.client;

import com.dandeib.MedalIntegrationFabric;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;

public class MedalIntegrationFabricClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		// Try vanilla death messages first, then fall back to servers' custom kill lines.
		ClientReceiveMessageEvents.GAME.register((message, overlay) -> {
			ClientPlayerEntity player = MinecraftClient.getInstance().player;
			if (player == null) {
				return;
			}
			if (!VanillaKillDetector.detect(message, player)) {
				CustomKillDetector.detect(message, overlay, player);
			}
		});
		MedalIntegrationFabric.LOGGER.info("[Medal-Integration] Kill detection active.");
	}
}
