package com.dandeib.client;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.event.player.AttackEntityCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.util.ActionResult;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Detects local-player kills from entity state, independent of chat. Players we damage
 * are added to {@link #watched}; if one dies within the window it counts as our kill.
 * Damage sources: melee ({@link AttackEntityCallback}) and our own projectiles that
 * vanish next to a player (a hit).
 */
public final class EntityKillTracker {

    // 20 client ticks = 1 second.
    private static final int WATCH_TICKS = 100;       // how long a hit player is watched for death
    private static final int PROJECTILE_TTL = 200;    // how long an own projectile is tracked in flight
    private static final double HIT_RADIUS_SQ = 2.0 * 2.0;

    private static final Map<Entity, Integer> watched = new HashMap<>();
    private static final Map<Entity, Integer> projectiles = new HashMap<>();

    private EntityKillTracker() {}

    public static void register() {
        AttackEntityCallback.EVENT.register((player, world, hand, entity, hitResult) -> {
            if (world.isClient() && entity instanceof PlayerEntity && entity != player) {
                watched.put(entity, 0);
            }
            return ActionResult.PASS;
        });

        ClientTickEvents.END_CLIENT_TICK.register(EntityKillTracker::tick);
    }

    private static void tick(MinecraftClient client) {
        ClientWorld world = client.world;
        PlayerEntity self = client.player;
        if (world == null || self == null) {
            watched.clear();
            projectiles.clear();
            return;
        }

        discoverOwnProjectiles(world, self);
        updateProjectiles(world, self);
        checkWatchedForDeath();
    }

    // getOwner() resolves client-side because ProjectileEntity.onSpawnPacket sets it from the spawn packet.
    private static void discoverOwnProjectiles(ClientWorld world, PlayerEntity self) {
        for (Entity entity : world.getEntities()) {
            if (entity instanceof ProjectileEntity projectile
                    && !projectiles.containsKey(entity)
                    && projectile.getOwner() == self) {
                projectiles.put(entity, 0);
            }
        }
    }

    // A projectile that disappears next to a player is treated as a hit; projectiles stuck
    // in blocks or flying into the void vanish far from players or expire via PROJECTILE_TTL.
    private static void updateProjectiles(ClientWorld world, PlayerEntity self) {
        Iterator<Map.Entry<Entity, Integer>> it = projectiles.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<Entity, Integer> entry = it.next();
            Entity projectile = entry.getKey();

            if (projectile.isRemoved()) {
                for (PlayerEntity target : world.getPlayers()) {
                    if (target != self
                            && !target.isSpectator()
                            && projectile.squaredDistanceTo(target) <= HIT_RADIUS_SQ) {
                        watched.put(target, 0);
                    }
                }
                it.remove();
                continue;
            }

            int age = entry.getValue() + 1;
            if (age > PROJECTILE_TTL) {
                it.remove();
            } else {
                entry.setValue(age);
            }
        }
    }

    // On death the client sets health to 0 (LivingEntity.handleStatus(3)) and it stays 0 for
    // the whole death animation, so polling getHealth() <= 0 reliably catches it.
    private static void checkWatchedForDeath() {
        Iterator<Map.Entry<Entity, Integer>> it = watched.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<Entity, Integer> entry = it.next();
            Entity entity = entry.getKey();

            if (entity instanceof LivingEntity living && living.getHealth() <= 0.0F) {
                it.remove();
                KillNotifier.onKill(entity.getName().getString(), "Entity");
                continue;
            }

            if (entity.isRemoved()) {
                it.remove();
                continue;
            }

            int age = entry.getValue() + 1;
            if (age > WATCH_TICKS) {
                it.remove();
            } else {
                entry.setValue(age);
            }
        }
    }
}
