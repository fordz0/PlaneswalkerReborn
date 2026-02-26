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

import io.github.apace100.apoli.action.ActionConfiguration;
import io.github.apace100.apoli.action.context.EntityActionContext;
import io.github.apace100.apoli.action.type.EntityActionType;
import io.github.apace100.apoli.component.PowerHolderComponent;
import io.github.apace100.apoli.data.ApoliDataTypes;
import io.github.apace100.apoli.data.TypedDataObjectFactory;
import io.github.apace100.apoli.power.PowerReference;
import io.github.apace100.apoli.power.type.VariableIntPowerType;
import io.github.apace100.apoli.registry.ApoliRegistries;
import io.github.apace100.calio.data.SerializableData.Instance;
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
import java.util.function.BiConsumer;
import org.jetbrains.annotations.NotNull;

public class TCEntityActions {

    private static ActionConfiguration<PlaneswalkerEntityActionType> register(Identifier id, SerializableData dataSchema, BiConsumer<Instance, Entity> action) {
        final ActionConfiguration<PlaneswalkerEntityActionType>[] holder = new ActionConfiguration[1];
        TypedDataObjectFactory<PlaneswalkerEntityActionType> factory = TypedDataObjectFactory.simple(
                dataSchema,
                data -> new PlaneswalkerEntityActionType(holder[0], data, action),
                (actionType, serializableData) -> actionType.data
        );
        ActionConfiguration<PlaneswalkerEntityActionType> configuration = ActionConfiguration.of(id, factory);
        holder[0] = configuration;
        Registry.register((Registry) ApoliRegistries.ENTITY_ACTION_TYPE, configuration.id(), configuration);
        return configuration;
    }

    public static void initialization() {
        register(Identifier.of(ThePlaneswalker.MODID, "switch_dimension"), new SerializableData().add("dimension", ApoliDataTypes.POWER_REFERENCE).add("position", ApoliDataTypes.POWER_REFERENCE),
                (data, entity) -> {
                    if (entity instanceof ServerPlayerEntity player) {
                        PowerReference dimensionRef = data.get("dimension");
                        PowerReference positionRef = data.get("position");
                        DimensionPower power = TCPowers.getPowerType(player, dimensionRef, DimensionPower.class);
                        PositionPower position = TCPowers.getPowerType(player, positionRef, PositionPower.class);
                        if (power == null || position == null) {
                            return;
                        }
                        power.updateWorld((ServerWorld) player.getWorld());
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
                });

        register(Identifier.of(ThePlaneswalker.MODID, "set_position"), new SerializableData().add("position", ApoliDataTypes.POWER_REFERENCE),
                (data, entity) -> {
                    PowerReference positionRef = data.get("position");
                    PositionPower power = TCPowers.getPowerType(entity, positionRef, PositionPower.class);
                    if (power == null) {
                        return;
                    }
                    power.pos = entity.getBlockPos();
                    PowerHolderComponent.syncPower(entity, positionRef);
                });
        register(Identifier.of(ThePlaneswalker.MODID, "sync_resource_position"), new SerializableData().add("position", ApoliDataTypes.POWER_REFERENCE).add("resource", ApoliDataTypes.RESOURCE_REFERENCE),
                (data, entity) -> {
                    PowerReference positionRef = data.get("position");
                    PowerReference resourceRef = data.get("resource");
                    PositionPower power = TCPowers.getPowerType(entity, positionRef, PositionPower.class);
                    if (power == null) {
                        return;
                    }

                    int distance = (int) Math.sqrt(power.pos.getSquaredDistance(entity.getX(), entity.getY(), entity.getZ()));

                    VariableIntPowerType resource = TCPowers.getPowerType(entity, resourceRef, VariableIntPowerType.class);
                    if (resource == null) {
                        return;
                    }
                    resource.setValue(distance);

                    PowerHolderComponent.syncPower(entity, resourceRef);

                });
        register(Identifier.of(ThePlaneswalker.MODID, "set_position_block"), new SerializableData().add("position", ApoliDataTypes.POWER_REFERENCE),
                (data, entity) -> {
                    if (entity instanceof ServerPlayerEntity player) {
                        PowerReference positionRef = data.get("position");
                        PositionPower power = TCPowers.getPowerType(entity, positionRef, PositionPower.class);
                        if (power == null) {
                            return;
                        }
                        power.pos = ((ServerPlayerEntityInterface) player).getLastInteracted();
                        PowerHolderComponent.syncPower(entity, positionRef);
                    }
                });
        register(Identifier.of(ThePlaneswalker.MODID, "sync_resource_position_inverse"), new SerializableData().add("position", ApoliDataTypes.POWER_REFERENCE).add("resource", ApoliDataTypes.RESOURCE_REFERENCE),
                (data, entity) -> {
                    PowerReference positionRef = data.get("position");
                    PowerReference resourceRef = data.get("resource");
                    PositionPower power = TCPowers.getPowerType(entity, positionRef, PositionPower.class);
                    if (power == null) {
                        return;
                    }

                    int distance = (int) Math.sqrt(power.pos.getSquaredDistance(entity.getX(), entity.getY(), entity.getZ()));

                    VariableIntPowerType resource = TCPowers.getPowerType(entity, resourceRef, VariableIntPowerType.class);
                    if (resource == null) {
                        return;
                    }
                    resource.setValue(resource.getMax() - distance);

                    PowerHolderComponent.syncPower(entity, resourceRef);

                });
        register(Identifier.of(ThePlaneswalker.MODID, "teleport_to_target"), new SerializableData(),
                (data, entity) -> {
                    if (entity instanceof ServerPlayerEntity player) {
                        LivingEntity lastAttacked = ((ServerPlayerEntityInterface) player).getLastAttacked();
                        if (lastAttacked != null) {
                            player.networkHandler.requestTeleport(lastAttacked.getX(), lastAttacked.getY(), lastAttacked.getZ(), lastAttacked.getYaw(), lastAttacked.getPitch());
                            player.fallDistance = 0;
                            player.onTeleportationDone();
                        }
                    }
                });
        register(Identifier.of(ThePlaneswalker.MODID, "teleport_target_to_position"), new SerializableData().add("position", ApoliDataTypes.POWER_REFERENCE),
                (data, entity) -> {
                    if (entity instanceof ServerPlayerEntity player) {
                        PowerReference positionRef = data.get("position");
                        PositionPower power = TCPowers.getPowerType(entity, positionRef, PositionPower.class);
                        if (power == null) {
                            return;
                        }
                        LivingEntity lastAttacked = ((ServerPlayerEntityInterface) player).getLastAttacked();
                        if (lastAttacked != null) {
                            lastAttacked.requestTeleport(power.pos.getX(), power.pos.getY(), power.pos.getZ());
                        }
                    }
                });
        register(Identifier.of(ThePlaneswalker.MODID, "void_duel"), new SerializableData(),
                (data, entity) -> {
                    if (!(entity instanceof ServerPlayerEntity player)) {
                        return;
                    }
                    LivingEntity lastAttacked = ((ServerPlayerEntityInterface) player).getLastAttacked();
                    if (lastAttacked == null || lastAttacked == player || player.getServer() == null) {
                        return;
                    }
                    if (VoidDuelTracker.isInDuel(player) || VoidDuelTracker.isInDuel(lastAttacked)) {
                        return;
                    }
                    RegistryKey<World> voidKey = RegistryKey.of(RegistryKeys.WORLD, Identifier.of(ThePlaneswalker.MODID, "void"));
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
                    VoidDuelTracker.scheduleReturn(player);
                    VoidDuelTracker.scheduleReturn(lastAttacked);
                    teleportEntity(voidWorld, player, x, y, z);
                    teleportEntity(voidWorld, lastAttacked, x, y, z);
                });
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

    private static final class PlaneswalkerEntityActionType extends EntityActionType {
        private final ActionConfiguration<?> configuration;
        private final Instance data;
        private final BiConsumer<Instance, Entity> action;

        private PlaneswalkerEntityActionType(ActionConfiguration<?> configuration, Instance data, BiConsumer<Instance, Entity> action) {
            this.configuration = configuration;
            this.data = data;
            this.action = action;
        }

        @Override
        public void accept(EntityActionContext context) {
            action.accept(data, context.entity());
        }

        @Override
        public @NotNull ActionConfiguration<?> getConfig() {
            return configuration;
        }
    }

}
