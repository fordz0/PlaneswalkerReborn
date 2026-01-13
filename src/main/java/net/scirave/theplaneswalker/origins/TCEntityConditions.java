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

import io.github.apace100.apoli.power.factory.condition.ConditionFactory;
import io.github.apace100.apoli.registry.ApoliRegistries;
import io.github.apace100.calio.data.SerializableData;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Identifier;
import net.minecraft.registry.Registry;
import net.scirave.theplaneswalker.ThePlaneswalker;

public class TCEntityConditions {

    private static void register(ConditionFactory<Entity> conditionFactory) {
        Registry.register(ApoliRegistries.ENTITY_CONDITION, conditionFactory.getSerializerId(), conditionFactory);
    }

    public static void initialization() {
        register(new ConditionFactory<>(new Identifier(ThePlaneswalker.MODID, "is_flying"), new SerializableData(),
                (data, entity) -> {
                    if (entity instanceof PlayerEntity) {
                        return ((PlayerEntity) entity).getAbilities().flying;
                    }
                    return false;
                }));
        register(new ConditionFactory<>(new Identifier(ThePlaneswalker.MODID, "exposed_to_sun"), new SerializableData(),
                (data, entity) -> {
                    if (entity == null) {
                        return false;
                    }
                    var world = entity.getWorld();
                    if (!world.getDimension().hasSkyLight() || !world.isDay()) {
                        return false;
                    }
                    var pos = net.minecraft.util.math.BlockPos.ofFloored(entity.getX(), entity.getEyeY(), entity.getZ());
                    return world.isSkyVisible(pos);
                }));
        register(new ConditionFactory<>(new Identifier(ThePlaneswalker.MODID, "has_xp"), new SerializableData(),
                (data, entity) -> {
                    if (!(entity instanceof PlayerEntity player)) {
                        return false;
                    }
                    int level = player.experienceLevel;
                    int progress = Math.round(player.experienceProgress * player.getNextLevelExperience());
                    int points = progress;
                    if (level >= 32) {
                        points += (9 * level * level - 325 * level + 4440) / 2;
                    } else if (level >= 17) {
                        points += (5 * level * level - 81 * level + 720) / 2;
                    } else {
                        points += level * level + 6 * level;
                    }
                    return points > 0;
                }));
    }


}
