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
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.world.World;
import net.scirave.theplaneswalker.helpers.ServerPlayerEntityInterface;
import net.scirave.theplaneswalker.origins.TCPowers;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = LivingEntity.class, priority = 999)
public abstract class LivingEntityMixin extends EntityMixin {

    @Inject(method = "damage", at = @At("HEAD"))
    public void onDamaged(DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {
        Entity attacker = source.getAttacker();
        if (attacker instanceof ServerPlayerEntity && attacker != ((Object) this)) {
            ((ServerPlayerEntityInterface) attacker).setLastAttacked((LivingEntity) (Object) this);
        }
    }

    @Inject(method = "isFallFlying", at = @At("HEAD"), cancellable = true)
    public void noGliding(CallbackInfoReturnable<Boolean> cir) {
    }

    @ModifyVariable(method = "heal", at = @At("HEAD"), index = 1)
    private float reduceHealingInOverworld(float amount) {
        if (TCPowers.VOID_VEINS.isActive((LivingEntity) (Object) this)
                && ((LivingEntity) (Object) this).getWorld().getRegistryKey().equals(World.OVERWORLD)) {
            return amount * 0.5F;
        }
        return amount;
    }

    @Inject(method = "canSee", at = @At("HEAD"), cancellable = true)
    public void noSee(Entity entity, CallbackInfoReturnable<Boolean> cir) {
        if (TCPowers.PHASESHIFT.isActive(entity)) {
            cir.setReturnValue(false);
        }
    }

    @Inject(method = "pushAway", at = @At("HEAD"), cancellable = true)
    public void stopPushingAway(Entity entity, CallbackInfo ci) {
    }

    @Inject(method = "pushAwayFrom", at = @At("HEAD"), cancellable = true)
    public void stopPushingAwayFrom(Entity entity, CallbackInfo ci) {
    }


}
