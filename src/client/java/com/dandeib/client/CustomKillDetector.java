package com.dandeib.client;

import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.text.Text;

import java.util.Locale;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** Kill detection from servers' custom kill lines (chat + action bar). Heuristic. */
public final class CustomKillDetector {

    // Add server-specific terms here. Ambiguous words ("beat", "won") are left out on purpose.
    private static final String[] VERBS = {
        "killed", "slain", "slew", "murdered", "executed", "eliminated", "defeated",
        "destroyed", "knocked", "sniped", "headshot", "assassinated", "obliterated", "rekt"
    };

    // Filler skipped when guessing the victim name.
    private static final Set<String> STOPWORDS = Set.of(
        "was", "were", "is", "has", "have", "been", "got", "the", "a", "an", "by", "to", "in", "into", "off", "player"
    );

    private static final Pattern NAME = Pattern.compile("[A-Za-z0-9_]{1,16}");

    private CustomKillDetector() {}

    public static void detect(Text message, boolean overlay, ClientPlayerEntity player) {
        String text = strip(message.getString()).trim();
        if (text.isEmpty()) {
            return;
        }
        String lower = text.toLowerCase(Locale.ROOT);

        int verb = -1;
        int verbLen = 0;
        for (String v : VERBS) {
            int i = wordIndex(lower, v);
            if (i >= 0 && (verb < 0 || i < verb)) {
                verb = i;
                verbLen = v.length();
            }
        }
        if (verb < 0) {
            return;
        }

        String self = player.getGameProfile().name();
        String display = player.getDisplayName() != null ? player.getDisplayName().getString() : self;

        // Passive ("X slain by you"): killer after "by". Active ("you killed X"): killer before the verb.
        int by = wordIndex(lower, "by");
        String killer = by >= 0 ? text.substring(by + 2) : text.substring(0, verb);
        String victim = by >= 0 ? text.substring(0, verb) : text.substring(Math.min(verb + verbLen, text.length()));

        // Action bar is our own HUD, so a killer-less line ("Killed X") is ours.
        boolean weKilled = isSelf(killer, self, display) || (overlay && !NAME.matcher(killer).find());
        if (!weKilled || isSelf(victim, self, display)) {
            return;
        }

        String name = firstName(victim);
        KillNotifier.onKill(name.isEmpty() ? "Player" : name, overlay ? "ActionBar" : "Chat");
    }

    private static boolean isSelf(String part, String self, String display) {
        String lower = strip(part).toLowerCase(Locale.ROOT);
        return mentions(part, self) || mentions(part, display)
                || wordIndex(lower, "you") >= 0 || wordIndex(lower, "your") >= 0;
    }

    // First name-like token that isn't filler. Victim name is cosmetic, so a rough guess is fine.
    private static String firstName(String part) {
        Matcher m = NAME.matcher(strip(part));
        while (m.find()) {
            if (!STOPWORDS.contains(m.group().toLowerCase(Locale.ROOT))) {
                return m.group();
            }
        }
        return "";
    }

    private static boolean mentions(String text, String name) {
        return name != null && !name.isEmpty() && wordIndex(strip(text), name) >= 0;
    }

    private static int wordIndex(String text, String word) {
        Matcher m = Pattern.compile("(?<![A-Za-z0-9_])" + Pattern.quote(word) + "(?![A-Za-z0-9_])").matcher(text);
        return m.find() ? m.start() : -1;
    }

    private static String strip(String s) {
        return s == null ? "" : s.replaceAll("§.", "");
    }
}
