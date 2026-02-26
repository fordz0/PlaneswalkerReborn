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

import com.mojang.authlib.GameProfile;
import io.github.apace100.apoli.component.PowerHolderComponent;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.scirave.theplaneswalker.helpers.ServerPlayerEntityInterface;
import net.scirave.theplaneswalker.origins.DimensionChangedPower;
import net.scirave.theplaneswalker.origins.TCPowers;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerPlayerEntity.class)
public abstract class ServerPlayerEntityMixin extends PlayerEntity implements ServerPlayerEntityInterface {

    private LivingEntity lastAttacked;

    private BlockPos lastInteracted;

    public ServerPlayerEntityMixin(World world, BlockPos pos, float yaw, GameProfile profile) {
        super(world, pos, yaw, profile);
    }

    @Inject(method = "worldChanged", at = @At("HEAD"))
    public void worldChanged(ServerWorld world, CallbackInfo ci) {
        PowerHolderComponent.getPowers((ServerPlayerEntity) (Object) this, DimensionChangedPower.class).forEach(DimensionChangedPower::onChange);
    }

    @Inject(method = "sleep", at = @At("HEAD"), cancellable = true)
    public void noTrueRest(BlockPos pos, CallbackInfo ci) {
        if (TCPowers.INSOMNIAC.isActive((ServerPlayerEntity) (Object) this)) {
            super.sleep(pos);
            ci.cancel();
        }
    }

    @Override
    public LivingEntity getLastAttacked() {
        return lastAttacked;
    }

    @Override
    public void setLastAttacked(LivingEntity entity) {
        lastAttacked = entity;
    }

    @Override
    public BlockPos getLastInteracted() {
        return lastInteracted;
    }

    @Override
    public void setLastInteracted(BlockPos pos) {
        lastInteracted = pos;
    }

}
