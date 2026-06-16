package com.dandeib.client;

import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableTextContent;

import java.util.regex.Pattern;

/** Kill detection from vanilla death messages (translatable "death." keys). */
public final class VanillaKillDetector {

    private VanillaKillDetector() {}

    /** Returns true if the message was a death message (handled here) so the custom detector skips it. */
    public static boolean detect(Text message, ClientPlayerEntity player) {
        TranslatableTextContent death = findDeath(message);
        if (death == null) {
            return false;
        }

        Object[] args = death.getArgs();
        if (args.length == 0) {
            return true;
        }

        String self = player.getGameProfile().name();
        String display = player.getDisplayName() != null ? player.getDisplayName().getString() : self;

        // Arg 0 is the victim; if that's us, we died.
        String victim = plain(args[0]);
        if (mentions(victim, self) || mentions(victim, display)) {
            return true;
        }

        // A later arg is the killer; if that's us, it's our kill.
        for (int i = 1; i < args.length; i++) {
            if (mentions(plain(args[i]), self) || mentions(plain(args[i]), display)) {
                KillNotifier.onKill(strip(victim), "Chat");
                break;
            }
        }
        return true;
    }

    private static TranslatableTextContent findDeath(Text text) {
        if (text == null) {
            return null;
        }
        if (text.getContent() instanceof TranslatableTextContent t && t.getKey().startsWith("death.")) {
            return t;
        }
        for (Text sibling : text.getSiblings()) {
            TranslatableTextContent found = findDeath(sibling);
            if (found != null) {
                return found;
            }
        }
        if (text.getContent() instanceof TranslatableTextContent t) {
            for (Object arg : t.getArgs()) {
                if (arg instanceof Text argText) {
                    TranslatableTextContent found = findDeath(argText);
                    if (found != null) {
                        return found;
                    }
                }
            }
        }
        return null;
    }

    private static String plain(Object arg) {
        return arg instanceof Text t ? t.getString() : (arg == null ? "" : arg.toString());
    }

    // Whole-word match so prefixes ("[Red] player") hit but "playera" doesn't.
    private static boolean mentions(String text, String name) {
        if (name == null || name.isEmpty()) {
            return false;
        }
        return Pattern.compile("(?<![A-Za-z0-9_])" + Pattern.quote(name) + "(?![A-Za-z0-9_])")
                .matcher(strip(text)).find();
    }

    private static String strip(String s) {
        return s == null ? "" : s.replaceAll("§.", "");
    }
}
