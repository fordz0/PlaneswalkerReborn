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

package net.scirave.theplaneswalker.mixin;

import net.minecraft.block.AbstractBlock;
import net.minecraft.block.BlockState;
import net.minecraft.block.EntityShapeContext;
import net.minecraft.block.ShapeContext;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ai.pathing.NavigationType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.scirave.theplaneswalker.origins.ActivatedPositionPower;
import net.scirave.theplaneswalker.origins.TCPowers;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = AbstractBlock.class, priority = 999)
public class AbstractBlockMixin {

    @Inject(at = @At("RETURN"), method = "getOutlineShape", cancellable = true)
    private void modifyBlockOutline(BlockState state, BlockView world, BlockPos pos, ShapeContext context, CallbackInfoReturnable<VoxelShape> cir) {
        if (context instanceof EntityShapeContext entityContext) {
            Entity entity = entityContext.getEntity();
            if (entity != null) {
                for (PlayerEntity plr : entity.getWorld().getPlayers()) {
                    ActivatedPositionPower power = (ActivatedPositionPower) TCPowers.DIMENSIONAL_RIFT.get(plr);
                    if (power != null && power.isActive() && pos.getManhattanDistance(power.pos) <= power.range) {
                        cir.setReturnValue(VoxelShapes.empty());
                        return;
                    }
                }
            }
        }
    }

    @Inject(method = "canPathfindThrough", at = @At("HEAD"), cancellable = true)
    private void allowPathfinding(BlockState state, BlockView world, BlockPos pos, NavigationType type, CallbackInfoReturnable<Boolean> cir) {
        if (world instanceof World actualWorld) {
            for (PlayerEntity plr : actualWorld.getPlayers()) {
                ActivatedPositionPower power = (ActivatedPositionPower) TCPowers.DIMENSIONAL_RIFT.get(plr);
                if (power != null && power.isActive() && pos.getManhattanDistance(power.pos) <= power.range) {
                    cir.setReturnValue(true);
                    return;
                }
            }
        }
    }

}
