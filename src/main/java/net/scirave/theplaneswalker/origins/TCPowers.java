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

import io.github.apace100.apoli.action.EntityAction;
import io.github.apace100.apoli.power.PowerConfiguration;
import io.github.apace100.apoli.power.PowerReference;
import io.github.apace100.apoli.power.type.PowerType;
import io.github.apace100.apoli.registry.ApoliRegistries;
import io.github.apace100.calio.data.SerializableData.Instance;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataTypes;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.entity.Entity;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.world.World;
import net.scirave.theplaneswalker.ThePlaneswalker;


public class TCPowers {

    public static final PowerReference FLYING = PowerReference.of(Identifier.of(ThePlaneswalker.MODID, "spatial_stride"));

    public static final PowerReference PHASESHIFT = PowerReference.of(Identifier.of(ThePlaneswalker.MODID, "phaseshift_drain"));

    public static final PowerReference DIMENSIONAL_RIFT = PowerReference.of(Identifier.of(ThePlaneswalker.MODID, "dimensional_rift_tether"));

    public static final PowerReference SOULFOOD = PowerReference.of(Identifier.of(ThePlaneswalker.MODID, "soulfood"));

    public static final PowerReference OVERSPECIALIZATION = PowerReference.of(Identifier.of(ThePlaneswalker.MODID, "overspecialization"));

    public static final PowerReference INSOMNIAC = PowerReference.of(Identifier.of(ThePlaneswalker.MODID, "insomniac"));

    public static final PowerReference VOID_VEINS = PowerReference.of(Identifier.of(ThePlaneswalker.MODID, "void_veins"));

    public static final PowerReference EMPTY_RESERVES = PowerReference.of(Identifier.of(ThePlaneswalker.MODID, "empty_reserves"));

    public static final PowerConfiguration<DimensionPower> DIMENSION = PowerConfiguration.of(
            Identifier.of(ThePlaneswalker.MODID, "dimension"),
            PowerType.createConditionedDataFactory(
                    new SerializableData().add("dimension", SerializableDataTypes.DIMENSION),
                    (data, condition) -> new DimensionPower((RegistryKey<World>) data.get("dimension"), condition),
                    (power, serializableData) -> serializableData.instance().set("dimension", power.focusKey)
            )
    );

    public static final PowerConfiguration<PositionPower> POSITION = PowerConfiguration.conditionedSimple(
            Identifier.of(ThePlaneswalker.MODID, "position"),
            condition -> new PositionPower(BlockPos.ORIGIN, condition)
    );

    public static final PowerConfiguration<ActivatedPositionPower> ACTIVATED_POSITION = PowerConfiguration.of(
            Identifier.of(ThePlaneswalker.MODID, "activated_position"),
            PowerType.createConditionedDataFactory(
                    new SerializableData().add("range", SerializableDataTypes.INT),
                    (data, condition) -> new ActivatedPositionPower(BlockPos.ORIGIN, data.getInt("range"), condition),
                    (power, serializableData) -> serializableData.instance().set("range", power.range)
            )
    );

    public static final PowerConfiguration<DimensionChangedPower> DIMENSION_CHANGED = PowerConfiguration.of(
            Identifier.of(ThePlaneswalker.MODID, "dimension_changed"),
            PowerType.createConditionedDataFactory(
                    new SerializableData().add("entity_action", EntityAction.DATA_TYPE),
                    (data, condition) -> new DimensionChangedPower(data.get("entity_action"), condition),
                    (power, serializableData) -> serializableData.instance().set("entity_action", power.getEntityAction())
            )
    );

    public static final PowerConfiguration<OnTeleportPower> ON_TELEPORT = PowerConfiguration.of(
            Identifier.of(ThePlaneswalker.MODID, "on_teleport"),
            PowerType.createConditionedDataFactory(
                    new SerializableData().add("entity_action", EntityAction.DATA_TYPE),
                    (data, condition) -> new OnTeleportPower(data.get("entity_action"), condition),
                    (power, serializableData) -> serializableData.instance().set("entity_action", power.getEntityAction())
            )
    );

    public static final PowerConfiguration<AttackBlockPower> ATTACK_BLOCK = PowerConfiguration.of(
            Identifier.of(ThePlaneswalker.MODID, "attack_block"),
            PowerType.createConditionedDataFactory(
                    new SerializableData().add("entity_action", EntityAction.DATA_TYPE),
                    (data, condition) -> new AttackBlockPower(data.get("entity_action"), condition),
                    (power, serializableData) -> serializableData.instance().set("entity_action", power.getEntityAction())
            )
    );

    private static <T extends PowerType> void register(PowerConfiguration<T> configuration) {
        Registry.register((Registry) ApoliRegistries.POWER_TYPE, configuration.id(), configuration);
    }

    public static <T extends PowerType> T getPowerType(Entity entity, PowerReference reference, Class<T> clazz) {
        PowerType powerType = reference.getNullablePowerType(entity);
        if (clazz.isInstance(powerType)) {
            return clazz.cast(powerType);
        }
        return null;
    }

    public static void initialization() {
        register(DIMENSION);
        register(POSITION);
        register(ACTIVATED_POSITION);
        register(DIMENSION_CHANGED);
        register(ON_TELEPORT);
        register(ATTACK_BLOCK);
    }

}
