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

import io.github.apace100.apoli.component.PowerHolderComponent;
import io.github.apace100.apoli.data.ApoliDataTypes;
import io.github.apace100.apoli.power.PowerType;
import io.github.apace100.apoli.power.VariableIntPower;
import io.github.apace100.apoli.power.factory.action.ActionFactory;
import io.github.apace100.apoli.registry.ApoliRegistries;
import io.github.apace100.calio.data.SerializableData;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.world.World;
import net.scirave.theplaneswalker.ThePlaneswalker;
import net.scirave.theplaneswalker.helpers.ServerPlayerEntityInterface;
import net.scirave.theplaneswalker.helpers.TeleportHelper;
import net.scirave.theplaneswalker.helpers.VoidDuelTracker;
import net.minecraft.network.packet.s2c.play.PositionFlag;
import java.util.EnumSet;

public class TCEntityActions {

    private static void register(ActionFactory<Entity> actionFactory) {
        Registry.register(ApoliRegistries.ENTITY_ACTION, actionFactory.getSerializerId(), actionFactory);
    }


    public static void initialization() {
        register(new ActionFactory<>(new Identifier(ThePlaneswalker.MODID, "switch_dimension"), new SerializableData().add("dimension", ApoliDataTypes.POWER_TYPE).add("position", ApoliDataTypes.POWER_TYPE),
                (data, entity) -> {
                    if (entity instanceof ServerPlayerEntity player) {
                        PowerHolderComponent component = PowerHolderComponent.KEY.get(player);
                        DimensionPower power = (DimensionPower) component.getPower((PowerType<?>) data.get("dimension"));
                        power.updateWorld((ServerWorld) player.getWorld());
                        PositionPower position = (PositionPower) component.getPower((PowerType<?>) data.get("position"));
                        BlockPos pos = position.pos;
                        power.updateWorld((ServerWorld) player.getWorld());
                        double focusScale = power.worldFocus.getDimension().coordinateScale();
                        double lastScale = power.lastWorld.getDimension().coordinateScale();
                        double fraction;
                        if (entity.getWorld() == power.worldFocus) {
                            fraction = focusScale / lastScale;
                            Integer level = TeleportHelper.safeSpawn(power.lastWorld, (int) (pos.getX() * fraction), (int) (pos.getZ() * fraction));
                            if (level != null) {
                            player.teleport(power.lastWorld, (int) (pos.getX() * fraction) + 0.5, level, (int) (pos.getZ() * fraction) + 0.5, player.getYaw(), player.getPitch());
                                player.fallDistance = 0;
                                player.onTeleportationDone();
                            }
                        } else {
                            fraction = lastScale / focusScale;
                            Integer level = TeleportHelper.safeSpawn(power.worldFocus, (int) (pos.getX() * fraction), (int) (pos.getZ() * fraction));
                            if (level != null) {
                            player.teleport(power.worldFocus, (int) (pos.getX() * fraction) + 0.5, level, (int) (pos.getZ() * fraction) + 0.5, player.getYaw(), player.getPitch());
                                player.fallDistance = 0;
                                player.onTeleportationDone();
                            }
                        }
                    }
                }));

        register(new ActionFactory<>(new Identifier(ThePlaneswalker.MODID, "set_position"), new SerializableData().add("position", ApoliDataTypes.POWER_TYPE),
                (data, entity) -> {
                    PowerHolderComponent component = PowerHolderComponent.KEY.get(entity);
                    PositionPower power = (PositionPower) component.getPower((PowerType<?>) data.get("position"));
                    if (power == null) {
                        return;
                    }
                    power.pos = entity.getBlockPos();
                    PowerHolderComponent.syncPower(entity, power.getType());
                }));
        register(new ActionFactory<>(new Identifier(ThePlaneswalker.MODID, "sync_resource_position"), new SerializableData().add("position", ApoliDataTypes.POWER_TYPE).add("resource", ApoliDataTypes.POWER_TYPE),
                (data, entity) -> {
                    PowerHolderComponent component = PowerHolderComponent.KEY.get(entity);
                    PositionPower power = (PositionPower) component.getPower((PowerType<?>) data.get("position"));
                    if (power == null) {
                        return;
                    }

                    int distance = (int) Math.sqrt(power.pos.getSquaredDistance(entity.getX(), entity.getY(), entity.getZ()));

                    VariableIntPower resource = (VariableIntPower) component.getPower((PowerType<?>) data.get("resource"));
                    if (resource == null) {
                        return;
                    }
                    resource.setValue(distance);

                    PowerHolderComponent.syncPower(entity, resource.getType());

                }));
        register(new ActionFactory<>(new Identifier(ThePlaneswalker.MODID, "set_position_block"), new SerializableData().add("position", ApoliDataTypes.POWER_TYPE),
                (data, entity) -> {
                    if (entity instanceof ServerPlayerEntity player) {
                        PowerHolderComponent component = PowerHolderComponent.KEY.get(entity);
                        PositionPower power = (PositionPower) component.getPower((PowerType<?>) data.get("position"));
                        if (power == null) {
                            return;
                        }
                        power.pos = ((ServerPlayerEntityInterface) player).getLastInteracted();
                        PowerHolderComponent.syncPower(entity, power.getType());
                    }
                }));
        register(new ActionFactory<>(new Identifier(ThePlaneswalker.MODID, "sync_resource_position_inverse"), new SerializableData().add("position", ApoliDataTypes.POWER_TYPE).add("resource", ApoliDataTypes.POWER_TYPE),
                (data, entity) -> {
                    PowerHolderComponent component = PowerHolderComponent.KEY.get(entity);
                    PositionPower power = (PositionPower) component.getPower((PowerType<?>) data.get("position"));
                    if (power == null) {
                        return;
                    }

                    int distance = (int) Math.sqrt(power.pos.getSquaredDistance(entity.getX(), entity.getY(), entity.getZ()));

                    VariableIntPower resource = (VariableIntPower) component.getPower((PowerType<?>) data.get("resource"));
                    if (resource == null) {
                        return;
                    }
                    resource.setValue(resource.getMax() - distance);

                    PowerHolderComponent.syncPower(entity, resource.getType());

                }));
        register(new ActionFactory<>(new Identifier(ThePlaneswalker.MODID, "teleport_to_target"), new SerializableData(),
                (data, entity) -> {
                    if (entity instanceof ServerPlayerEntity player) {
                        LivingEntity lastAttacked = ((ServerPlayerEntityInterface) player).getLastAttacked();
                        if (lastAttacked != null) {
                            player.networkHandler.requestTeleport(lastAttacked.getX(), lastAttacked.getY(), lastAttacked.getZ(), lastAttacked.getYaw(), lastAttacked.getPitch());
                            player.fallDistance = 0;
                            player.onTeleportationDone();
                        }
                    }
                }));
        register(new ActionFactory<>(new Identifier(ThePlaneswalker.MODID, "teleport_target_to_position"), new SerializableData().add("position", ApoliDataTypes.POWER_TYPE),
                (data, entity) -> {
                    if (entity instanceof ServerPlayerEntity player) {
                        PowerHolderComponent component = PowerHolderComponent.KEY.get(entity);
                        PositionPower power = (PositionPower) component.getPower((PowerType<?>) data.get("position"));
                        LivingEntity lastAttacked = ((ServerPlayerEntityInterface) player).getLastAttacked();
                        if (lastAttacked != null) {
                            lastAttacked.requestTeleport(power.pos.getX(), power.pos.getY(), power.pos.getZ());
                        }
                    }
                }));
        register(new ActionFactory<>(new Identifier(ThePlaneswalker.MODID, "void_duel"), new SerializableData(),
                (data, entity) -> {
                    if (!(entity instanceof ServerPlayerEntity player)) {
                        return;
                    }
                    LivingEntity lastAttacked = ((ServerPlayerEntityInterface) player).getLastAttacked();
                    if (lastAttacked == null || lastAttacked == player || player.getServer() == null) {
                        return;
                    }
                    VoidDuelTracker.scheduleReturn(player);
                    VoidDuelTracker.scheduleReturn(lastAttacked);
                    RegistryKey<World> voidKey = RegistryKey.of(RegistryKeys.WORLD, new Identifier(ThePlaneswalker.MODID, "void"));
                    ServerWorld voidWorld = player.getServer().getWorld(voidKey);
                    if (voidWorld == null) {
                        return;
                    }
                    BlockPos targetPos = lastAttacked.getBlockPos();
                    Integer safeY = TeleportHelper.safeSpawn(voidWorld, targetPos.getX(), targetPos.getZ());
                    if (safeY == null) {
                        return;
                    }
                    double x = targetPos.getX() + 0.5;
                    double y = safeY;
                    double z = targetPos.getZ() + 0.5;
                    teleportEntity(voidWorld, player, x, y, z);
                    teleportEntity(voidWorld, lastAttacked, x, y, z);
                }));
    }

    private static void teleportEntity(ServerWorld world, Entity entity, double x, double y, double z) {
        if (entity instanceof ServerPlayerEntity player) {
            player.teleport(world, x, y, z, player.getYaw(), player.getPitch());
            player.fallDistance = 0;
            player.onTeleportationDone();
            return;
        }
        entity.teleport(world, x, y, z, EnumSet.noneOf(PositionFlag.class), entity.getYaw(), entity.getPitch());
        entity.fallDistance = 0;
    }

}
