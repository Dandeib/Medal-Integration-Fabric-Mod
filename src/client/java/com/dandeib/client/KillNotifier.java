package com.dandeib.client;

import com.dandeib.MedalIntegrationFabric;
import net.minecraft.client.MinecraftClient;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

/**
 * Single sink for confirmed kills from both detectors. The debounce prevents the chat
 * and entity paths from firing the same kill twice.
 */
public final class KillNotifier {

    private static final long DEBOUNCE_MS = 1500L;

    private static String lastVictim = "";
    private static long lastTime = 0L;

    private KillNotifier() {}

    public static synchronized void onKill(String victim, String source) {
        long now = System.currentTimeMillis();
        if (victim.equals(lastVictim) && (now - lastTime) < DEBOUNCE_MS) {
            return;
        }
        lastVictim = victim;
        lastTime = now;

        MinecraftClient client = MinecraftClient.getInstance();
        client.execute(() -> {
            MedalIntegrationFabric.LOGGER.info("[Medal] Kill ({}): {}", source, victim);

            if (client.player != null) {
                client.player.sendMessage(
                    Text.literal("⚔ Kill: ").formatted(Formatting.RED)
                        .append(Text.literal(victim).formatted(Formatting.WHITE)),
                    true
                );
                client.player.playSound(SoundEvents.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0f, 1.2f);
            }

            MedalClipTrigger.onKill(victim);
        });
    }
}
