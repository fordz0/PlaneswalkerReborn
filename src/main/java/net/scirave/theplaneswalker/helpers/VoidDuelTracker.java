/*
 * The Planeswalker
 * Copyright (c) 2026 SciRave
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * This Source Code may also be made available under the following Secondary
 * Licenses when the conditions for such availability set forth in the Eclipse
 * Public License, v. 2.0 are satisfied: GNU General Public License, version 2
 * with the GNU Classpath Exception which is
 * available at https://www.gnu.org/software/classpath/license.html.
 *
 * SPDX-License-Identifier: EPL-2.0 OR GPL-2.0 WITH Classpath-exception-2.0
 */

package net.scirave.theplaneswalker.helpers;

import java.util.Iterator;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.entity.Entity;
import net.minecraft.registry.RegistryKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraft.network.packet.s2c.play.PositionFlag;
import java.util.EnumSet;

public final class VoidDuelTracker {

    private static final int RETURN_TICKS = 200;

    private static final Map<UUID, ReturnEntry> PENDING_RETURNS = new ConcurrentHashMap<>();

    private VoidDuelTracker() {
    }

    public static void scheduleReturn(Entity entity) {
        if (entity == null || entity.getWorld().isClient()) {
            return;
        }
        ReturnEntry entry = new ReturnEntry(
                entity.getWorld().getRegistryKey(),
                entity.getPos(),
                entity.getYaw(),
                entity.getPitch(),
                RETURN_TICKS
        );
        PENDING_RETURNS.put(entity.getUuid(), entry);
    }

    public static void tick(MinecraftServer server) {
        Iterator<Map.Entry<UUID, ReturnEntry>> iterator = PENDING_RETURNS.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<UUID, ReturnEntry> entry = iterator.next();
            ReturnEntry data = entry.getValue();
            data.remainingTicks -= 1;
            if (data.remainingTicks > 0) {
                continue;
            }
            Entity entity = findEntity(server, entry.getKey());
            if (entity != null && !entity.isRemoved()) {
                ServerWorld world = server.getWorld(data.worldKey);
                if (world != null) {
                    teleportEntity(world, entity, data.pos, data.yaw, data.pitch);
                }
            }
            iterator.remove();
        }
    }

    private static Entity findEntity(MinecraftServer server, UUID uuid) {
        for (ServerWorld world : server.getWorlds()) {
            Entity entity = world.getEntity(uuid);
            if (entity != null) {
                return entity;
            }
        }
        return null;
    }

    private static void teleportEntity(ServerWorld world, Entity entity, Vec3d pos, float yaw, float pitch) {
        if (entity instanceof ServerPlayerEntity player) {
            player.teleport(world, pos.x, pos.y, pos.z, yaw, pitch);
            player.fallDistance = 0;
            player.onTeleportationDone();
            return;
        }
        entity.teleport(world, pos.x, pos.y, pos.z, EnumSet.noneOf(PositionFlag.class), yaw, pitch);
        entity.fallDistance = 0;
    }

    private static final class ReturnEntry {
        private final RegistryKey<World> worldKey;
        private final Vec3d pos;
        private final float yaw;
        private final float pitch;
        private int remainingTicks;

        private ReturnEntry(RegistryKey<World> worldKey, Vec3d pos, float yaw, float pitch, int remainingTicks) {
            this.worldKey = worldKey;
            this.pos = pos;
            this.yaw = yaw;
            this.pitch = pitch;
            this.remainingTicks = remainingTicks;
        }
    }
}
