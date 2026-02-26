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

package net.scirave.theplaneswalker.mixin;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.scirave.theplaneswalker.origins.ActivatedPositionPower;
import net.scirave.theplaneswalker.origins.TCPowers;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Entity.class)
public abstract class EntityMixin {

    @Shadow
    public World world;

    private static final FluidState EMPTY = Fluids.EMPTY.getDefaultState();

    public FluidState method(World world, BlockPos pos) {
        for (PlayerEntity plr : world.getPlayers()) {
            ActivatedPositionPower power = (ActivatedPositionPower) TCPowers.DIMENSIONAL_RIFT.get(plr);
            if (power != null && power.isActive() && pos.getManhattanDistance(power.pos) <= power.range) {
                return EMPTY;
            }
        }
        return world.getFluidState(pos);
    }

    @Redirect(method = "updateSwimming", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/World;getFluidState(Lnet/minecraft/util/math/BlockPos;)Lnet/minecraft/fluid/FluidState;"))
    public FluidState redirectedMethod1(World world, BlockPos pos) {
        return method(world, pos);
    }

    @Redirect(method = "updateSubmergedInWaterState", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/World;getFluidState(Lnet/minecraft/util/math/BlockPos;)Lnet/minecraft/fluid/FluidState;"))
    public FluidState redirectedMethod2(World world, BlockPos pos) {
        return method(world, pos);
    }

    @Redirect(method = "updateMovementInFluid", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/World;getFluidState(Lnet/minecraft/util/math/BlockPos;)Lnet/minecraft/fluid/FluidState;"))
    public FluidState redirectedMethod3(World world, BlockPos pos) {
        return method(world, pos);
    }

    @Inject(method = "isInvisible", at = @At("HEAD"), cancellable = true)
    public void invisible(CallbackInfoReturnable<Boolean> cir) {
    }

    @Inject(method = "isInvisibleTo", at = @At("HEAD"), cancellable = true)
    public void invisibleTo(PlayerEntity player, CallbackInfoReturnable<Boolean> cir) {
    }

}
