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

import io.github.apace100.apoli.data.ApoliDataTypes;
import io.github.apace100.apoli.power.PowerType;
import io.github.apace100.apoli.power.PowerTypeReference;
import io.github.apace100.apoli.power.factory.PowerFactory;
import io.github.apace100.apoli.registry.ApoliRegistries;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataTypes;
import net.minecraft.entity.Entity;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.world.World;
import net.scirave.theplaneswalker.ThePlaneswalker;

import java.util.function.Consumer;


public class TCPowers {

    public static final PowerType<?> FLYING = new PowerTypeReference<>(new Identifier(ThePlaneswalker.MODID, "spatial_stride"));

    public static final PowerType<?> PHASESHIFT = new PowerTypeReference<>(new Identifier(ThePlaneswalker.MODID, "phaseshift_drain"));

    public static final PowerType<?> DIMENSIONAL_RIFT = new PowerTypeReference<>(new Identifier(ThePlaneswalker.MODID, "dimensional_rift_tether"));

    public static final PowerType<?> SOULFOOD = new PowerTypeReference<>(new Identifier(ThePlaneswalker.MODID, "soulfood"));

    public static final PowerType<?> OVERSPECIALIZATION = new PowerTypeReference<>(new Identifier(ThePlaneswalker.MODID, "overspecialization"));

    public static final PowerType<?> INSOMNIAC = new PowerTypeReference<>(new Identifier(ThePlaneswalker.MODID, "insomniac"));

    public static final PowerType<?> VOID_VEINS = new PowerTypeReference<>(new Identifier(ThePlaneswalker.MODID, "void_veins"));

    public static final PowerType<?> EMPTY_RESERVES = new PowerTypeReference<>(new Identifier(ThePlaneswalker.MODID, "empty_reserves"));

    private static void register(PowerFactory<?> serializer) {
        Registry.register(ApoliRegistries.POWER_FACTORY, serializer.getSerializerId(), serializer);
    }

    public static void initialization() {

        register(new PowerFactory<>(new Identifier(ThePlaneswalker.MODID, "dimension"),

                        new SerializableData().add("dimension", SerializableDataTypes.DIMENSION),

                        data ->
                                (type, player) -> new DimensionPower(type, player, (RegistryKey<World>) data.get("dimension"))
                )
        );

        register(new PowerFactory<>(new Identifier(ThePlaneswalker.MODID, "position"), new SerializableData(),

                        data ->
                                (type, player) -> new PositionPower(type, player, BlockPos.ORIGIN)
                )
        );

        register(new PowerFactory<>(new Identifier(ThePlaneswalker.MODID, "activated_position"), new SerializableData().add("range", SerializableDataTypes.INT),

                        data ->
                                (type, player) -> new ActivatedPositionPower(type, player, BlockPos.ORIGIN, data.getInt("range"))
                ).allowCondition()
        );

        register(new PowerFactory<>(new Identifier(ThePlaneswalker.MODID, "dimension_changed"),

                        new SerializableData().add("entity_action", ApoliDataTypes.ENTITY_ACTION),

                        data ->
                                (type, player) -> new DimensionChangedPower(type, player, (Consumer<Entity>) data.get("entity_action"))
                )
        );

        register(new PowerFactory<>(new Identifier(ThePlaneswalker.MODID, "on_teleport"),

                        new SerializableData().add("entity_action", ApoliDataTypes.ENTITY_ACTION),

                        data ->
                                (type, player) -> new OnTeleportPower(type, player, (Consumer<Entity>) data.get("entity_action"))
                )
        );

        register(new PowerFactory<>(new Identifier(ThePlaneswalker.MODID, "attack_block"),

                        new SerializableData().add("entity_action", ApoliDataTypes.ENTITY_ACTION),

                        data ->
                                (type, player) -> new AttackBlockPower(type, player, (Consumer<Entity>) data.get("entity_action"))
                ).allowCondition()
        );


    }

}
