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

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ExperienceOrbEntity;
import net.minecraft.entity.player.HungerManager;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.world.World;
import net.scirave.theplaneswalker.origins.TCPowers;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = ExperienceOrbEntity.class, priority = 100)
public abstract class ExperienceOrbEntityMixin extends Entity {

    public ExperienceOrbEntityMixin(EntityType<?> type, World world) {
        super(type, world);
    }

    @Shadow
    public abstract int getExperienceAmount();

    @Inject(method = "onPlayerCollision", at = @At("HEAD"), cancellable = true)
    public void repairCheck(PlayerEntity player, CallbackInfo ci) {
        if (!player.getWorld().isClient()) {
            if (TCPowers.SOULFOOD.isActive(player)) {
                HungerManager hungerManager = player.getHungerManager();
                if (hungerManager.getFoodLevel() < 20) {
                    hungerManager.add(getExperienceAmount(), 0.6F);
                    ci.cancel();
                    this.discard();
                    return;
                }
            }
        }
    }

}
