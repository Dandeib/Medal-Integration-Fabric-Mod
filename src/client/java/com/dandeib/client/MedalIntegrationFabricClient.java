package com.dandeib.client;

import net.fabricmc.api.ClientModInitializer;

public class MedalIntegrationFabricClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		// Two complementary client-side detectors feeding the same kill sink.
		KillMessageDetector.register();      // death messages (covers ranged too)
		EntityKillTracker.register(); // entity death state (melee + own projectiles, chat-independent)
	}
}
