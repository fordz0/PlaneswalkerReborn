/*
 * Origins: Planeswalker Reborn
 * Copyright (c) 2026 SciRave, fordz0
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

import io.github.apace100.apoli.condition.EntityCondition;
import io.github.apace100.apoli.power.PowerConfiguration;
import io.github.apace100.apoli.power.type.PowerType;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtString;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.world.World;
import java.util.Optional;
import org.jetbrains.annotations.NotNull;

public class DimensionPower extends PowerType {

    public final RegistryKey<World> focusKey;

    public ServerWorld worldFocus;

    public ServerWorld lastWorld;

    public DimensionPower(RegistryKey<World> key, Optional<EntityCondition> condition) {
        super(condition);
        this.focusKey = key;
    }

    @Override
    public void onInit() {
        if (getHolder().getWorld().isClient) {
            return;
        }
        MinecraftServer server = getHolder().getWorld().getServer();
        if (server != null) {
            worldFocus = server.getWorld(focusKey);
        }
        updateWorld((ServerWorld) getHolder().getWorld());
    }

    public void updateWorld(ServerWorld world) {
        if (world != null && world != worldFocus) {
            lastWorld = world;
        }
    }

    @Override
    public NbtElement toTag() {
        if (lastWorld == null) {
            return NbtString.of(focusKey.getValue().toString());
        }
        return NbtString.of(lastWorld.getRegistryKey().getValue().toString());
    }

    @Override
    public void fromTag(NbtElement tag) {
        RegistryKey<World> key = RegistryKey.of(RegistryKeys.WORLD, Identifier.of(tag.asString()));
        if (key != null) {
            MinecraftServer server = getHolder().getWorld().getServer();
            if (server != null) {
                updateWorld(server.getWorld(key));
            }
        }
    }

    @Override
    public @NotNull PowerConfiguration<?> getConfig() {
        return TCPowers.DIMENSION;
    }

}
