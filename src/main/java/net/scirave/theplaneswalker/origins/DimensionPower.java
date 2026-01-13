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

package net.scirave.theplaneswalker.origins;

import io.github.apace100.apoli.power.Power;
import io.github.apace100.apoli.power.PowerType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtString;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.world.World;

public class DimensionPower extends Power {

    public ServerWorld worldFocus;

    public ServerWorld lastWorld;

    public DimensionPower(PowerType<?> type, LivingEntity entity, RegistryKey<World> key) {
        super(type, entity);
        if (entity == null || entity.getWorld().isClient) {
            return;
        }
        MinecraftServer server = entity.getWorld().getServer();
        if (server != null) {
            worldFocus = server.getWorld(key);
        }
        updateWorld((ServerWorld) entity.getWorld());
    }

    public void updateWorld(ServerWorld world) {
        if (world != worldFocus) {
            lastWorld = world;
        }
    }

    @Override
    public NbtElement toTag() {
        return NbtString.of(lastWorld.getRegistryKey().getValue().toString());
    }

    @Override
    public void fromTag(NbtElement tag) {
        RegistryKey<World> key = RegistryKey.of(RegistryKeys.WORLD, new Identifier(tag.asString()));
        if (key != null) {
            MinecraftServer server = entity.getWorld().getServer();
            if (server != null) {
                updateWorld(server.getWorld(key));
            }
        }
    }

}
