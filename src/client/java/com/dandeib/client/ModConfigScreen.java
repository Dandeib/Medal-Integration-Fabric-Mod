package com.dandeib.client;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.SliderWidget;
import net.minecraft.text.Text;

public class ModConfigScreen extends Screen {

    private final Screen parent;
    private final ModConfig cfg;

    private boolean enabled;
    private int clipDuration;
    private int clipDelayMs;

    public ModConfigScreen(Screen parent) {
        super(Text.literal("Medal Integration"));
        this.parent = parent;
        this.cfg = ModConfig.get();
        this.enabled = cfg.enabled;
        this.clipDuration = cfg.clipDuration;
        this.clipDelayMs = cfg.clipDelayMs;
    }

    @Override
    protected void init() {
        int cx = width / 2;
        int y = height / 4;

        addDrawableChild(ButtonWidget.builder(
                Text.literal("Enabled: " + (enabled ? "ON" : "OFF")),
                btn -> {
                    enabled = !enabled;
                    btn.setMessage(Text.literal("Enabled: " + (enabled ? "ON" : "OFF")));
                })
                .dimensions(cx - 100, y, 200, 20)
                .build());

        // Clip duration: 5–120 seconds
        addDrawableChild(new SliderWidget(cx - 100, y + 30, 200, 20,
                Text.literal("Clip Duration: " + clipDuration + "s"),
                (clipDuration - 5) / 115.0) {
            @Override
            protected void updateMessage() {
                setMessage(Text.literal("Clip Duration: " + ModConfigScreen.this.clipDuration + "s"));
            }
            @Override
            protected void applyValue() {
                ModConfigScreen.this.clipDuration = 5 + (int) Math.round(value * 115);
            }
        });

        // Clip delay: 0–5000 ms, step 100 ms
        addDrawableChild(new SliderWidget(cx - 100, y + 60, 200, 20,
                Text.literal("Clip Delay: " + clipDelayMs + "ms"),
                clipDelayMs / 5000.0) {
            @Override
            protected void updateMessage() {
                setMessage(Text.literal("Clip Delay: " + ModConfigScreen.this.clipDelayMs + "ms"));
            }
            @Override
            protected void applyValue() {
                ModConfigScreen.this.clipDelayMs = (int) Math.round(value * 50) * 100;
            }
        });

        addDrawableChild(ButtonWidget.builder(Text.literal("Save & Close"), btn -> {
            cfg.enabled = enabled;
            cfg.clipDuration = clipDuration;
            cfg.clipDelayMs = clipDelayMs;
            ModConfig.save();
            client.setScreen(parent);
        }).dimensions(cx - 102, y + 100, 100, 20).build());

        addDrawableChild(ButtonWidget.builder(Text.literal("Cancel"), btn ->
                client.setScreen(parent)
        ).dimensions(cx + 2, y + 100, 100, 20).build());
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        // The background (and its blur) is already drawn by Screen.renderWithTooltip
        // before render() is called; calling renderBackground() again here throws
        // "Can only blur once per frame" on 1.21.11.
        super.render(context, mouseX, mouseY, delta);
        context.drawCenteredTextWithShadow(textRenderer, title, width / 2, height / 4 - 20, 0xFFFFFF);
    }

    @Override
    public void close() {
        client.setScreen(parent);
    }
}
