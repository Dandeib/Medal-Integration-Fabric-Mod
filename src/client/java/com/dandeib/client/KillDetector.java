package com.dandeib.client;

import com.dandeib.MedalIntegrationFabric;
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.text.TextContent;
import net.minecraft.text.TranslatableTextContent;

import java.util.regex.Pattern;

/**
 * Detects local-player kills from vanilla death messages (works on any server that
 * sends them). Death messages are translatable text with a "death." key; argument 0
 * is always the victim, later arguments are the killer.
 */
public final class KillDetector {

    private KillDetector() {}

    public static void register() {
        ClientReceiveMessageEvents.GAME.register((message, overlay) -> handleMessage(message));
        MedalIntegrationFabric.LOGGER.info("[Medal] Kill detection active (death messages).");
    }

    private static void handleMessage(Text message) {
        ClientPlayerEntity player = MinecraftClient.getInstance().player;
        if (player == null) {
            return;
        }

        TranslatableTextContent death = findDeathContent(message);
        if (death == null) {
            return;
        }

        Object[] args = death.getArgs();
        if (args.length == 0) {
            return;
        }

        String selfName = player.getGameProfile().name();
        String selfDisplay = player.getDisplayName() != null ? player.getDisplayName().getString() : selfName;

        // Argument 0 is the victim: if that is us, we died - not a kill.
        String victim = plain(args[0]);
        if (mentions(victim, selfName) || mentions(victim, selfDisplay)) {
            return;
        }

        // Any later argument is the killer: if that is us, we got the kill.
        for (int i = 1; i < args.length; i++) {
            String killer = plain(args[i]);
            if (mentions(killer, selfName) || mentions(killer, selfDisplay)) {
                KillNotifier.onKill(stripFormatting(victim), "Chat");
                return;
            }
        }
    }

    private static TranslatableTextContent findDeathContent(Text text) {
        if (text == null) {
            return null;
        }
        TextContent content = text.getContent();
        if (content instanceof TranslatableTextContent ttc && ttc.getKey().startsWith("death.")) {
            return ttc;
        }
        for (Text sibling : text.getSiblings()) {
            TranslatableTextContent found = findDeathContent(sibling);
            if (found != null) {
                return found;
            }
        }
        if (content instanceof TranslatableTextContent ttc) {
            for (Object arg : ttc.getArgs()) {
                if (arg instanceof Text argText) {
                    TranslatableTextContent found = findDeathContent(argText);
                    if (found != null) {
                        return found;
                    }
                }
            }
        }
        return null;
    }

    private static String plain(Object arg) {
        if (arg instanceof Text text) {
            return text.getString();
        }
        return arg == null ? "" : arg.toString();
    }

    private static String stripFormatting(String input) {
        return input == null ? "" : input.replaceAll("§.", "");
    }

    // Whole-word match so team prefixes ("[Red] daniel") still hit while "daniela" does not.
    private static boolean mentions(String haystack, String name) {
        if (haystack == null || name == null || name.isEmpty()) {
            return false;
        }
        String cleanHay = stripFormatting(haystack);
        if (cleanHay.equals(name)) {
            return true;
        }
        Pattern pattern = Pattern.compile("(?<![A-Za-z0-9_])" + Pattern.quote(name) + "(?![A-Za-z0-9_])");
        return pattern.matcher(cleanHay).find();
    }
}
