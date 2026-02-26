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
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtIntArray;
import net.minecraft.util.math.BlockPos;
import java.util.Optional;
import org.jetbrains.annotations.NotNull;

public class PositionPower extends PowerType {

    public BlockPos pos;

    public PositionPower(BlockPos pos, Optional<EntityCondition> condition) {
        super(condition);
        this.pos = pos;
    }

    @Override
    public NbtElement toTag() {
        NbtCompound tag = new NbtCompound();
        tag.putInt("x", pos.getX());
        tag.putInt("y", pos.getY());
        tag.putInt("z", pos.getZ());
        return tag;
    }

    @Override
    public void fromTag(NbtElement tag) {
        if (tag instanceof NbtCompound compound
                && compound.contains("x")
                && compound.contains("y")
                && compound.contains("z")) {
            pos = new BlockPos(compound.getInt("x"), compound.getInt("y"), compound.getInt("z"));
            return;
        }
        if (tag instanceof NbtIntArray intArray) {
            int[] values = intArray.getIntArray();
            if (values.length >= 3) {
                pos = new BlockPos(values[0], values[1], values[2]);
            }
        }
    }

    @Override
    public @NotNull PowerConfiguration<?> getConfig() {
        return TCPowers.POSITION;
    }

}
