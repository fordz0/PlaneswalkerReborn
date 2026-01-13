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

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.registry.tag.FluidTags;
import net.minecraft.util.math.BlockPos;

public class TeleportHelper {

    public static boolean viableNonCollide(BlockState state) {
        return state.getFluidState().isIn(FluidTags.WATER) || (state.getFluidState().isEmpty() && !state.blocksMovement());
    }

    public static boolean viableNonCollideNoWater(BlockState state) {
        return state.getFluidState().isEmpty() && !state.blocksMovement();
    }

    public static boolean viableCollide(BlockState state) {
        return state.getFluidState().isIn(FluidTags.WATER) || state.isIn(BlockTags.VALID_SPAWN) || state.blocksMovement();
    }

    public static Integer platformUp(ServerWorld world, int x, int y, int z) {
        int level = y;

        BlockState one;
        BlockState two;
        BlockState three;

        do {
            one = world.getBlockState(new BlockPos(x, level, z));
            two = world.getBlockState(new BlockPos(x, level + 1, z));
            three = world.getBlockState(new BlockPos(x, level + 2, z));
            level++;
            if (world.isOutOfHeightLimit(level + 2) || ((level + 2) > world.getLogicalHeight())) {
                return null;
            }
        } while (!viableNonCollide(one) || !viableNonCollide(two) || !viableNonCollideNoWater(three));

        world.setBlockState(new BlockPos(x, level, z), Blocks.COBBLESTONE.getDefaultState());

        return level + 2;
    }

    public static Integer up(ServerWorld world, int x, int y, int z) {
        int level = y;

        BlockState one;
        BlockState two;
        BlockState three;

        do {
            if (world.isOutOfHeightLimit(level + 2) || ((level + 2) > world.getLogicalHeight())) {
                return null;
            }
            one = world.getBlockState(new BlockPos(x, level, z));
            two = world.getBlockState(new BlockPos(x, level + 1, z));
            three = world.getBlockState(new BlockPos(x, level + 2, z));
            level++;
        } while (!viableCollide(one) || !viableNonCollide(two) || !viableNonCollideNoWater(three));

        return level + 2;
    }

    public static Integer down(ServerWorld world, int x, int y, int z) {
        int level = y;


        BlockState one;
        BlockState two;
        BlockState three;

        do {
            if (world.isOutOfHeightLimit(level)) {
                return null;
            }
            one = world.getBlockState(new BlockPos(x, level, z));
            two = world.getBlockState(new BlockPos(x, level + 1, z));
            three = world.getBlockState(new BlockPos(x, level + 2, z));
            level--;
        } while (!viableCollide(one) || !viableNonCollide(two) || !viableNonCollideNoWater(three));


        return level + 2;
    }

    public static Integer safeSpawn(ServerWorld world, int x, int z) {

        int minimum = world.getBottomY();
        int maximum = world.getLogicalHeight();
        int middle = Math.floorDiv(Math.abs(minimum) + maximum, 2) + minimum;

        BlockState one = world.getBlockState(new BlockPos(x, middle, z));
        BlockState two = world.getBlockState(new BlockPos(x, middle + 1, z));
        BlockState three = world.getBlockState(new BlockPos(x, middle + 2, z));

        if (viableNonCollide(one) && viableNonCollide(two) && viableNonCollideNoWater(three)) {
            Integer level = down(world, x, middle, z);
            if (level == null) {
                level = up(world, x, middle, z);
                if (level != null) {
                    return level;
                }
            } else {
                return level;
            }
        } else {
            Integer level = up(world, x, middle, z);
            if (level == null) {
                level = down(world, x, middle, z);
                if (level != null) {
                    return level;
                }
            } else {
                return level;
            }
        }

        return platformUp(world, x, middle, z);

    }


}
