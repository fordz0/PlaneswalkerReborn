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

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.screen.AnvilScreenHandler;
import net.scirave.theplaneswalker.origins.TCPowers;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(AnvilScreenHandler.class)
public class AnvilScreenHandlerMixin {

    @Inject(
            method = "canTakeOutput",
            at = @At("HEAD"),
            cancellable = true
    )
    public void canAlwaysUse(PlayerEntity player, boolean present, CallbackInfoReturnable<Boolean> cir) {
        if (TCPowers.SOULFOOD.isActive(player)) {
            cir.setReturnValue(true);
        }
    }

    @Redirect(method = "onTakeOutput", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/player/PlayerEntity;addExperienceLevels(I)V"))
    private void noSubtractLevels(PlayerEntity playerEntity, int levels) {
        if (!TCPowers.SOULFOOD.isActive(playerEntity)) {
            playerEntity.addExperienceLevels(levels);
        }
    }

    @Environment(EnvType.CLIENT)
    @Inject(
            method = "getLevelCost",
            at = @At("HEAD"),
            cancellable = true
    )
    public void noCost(CallbackInfoReturnable<Integer> cir) {
        if (TCPowers.SOULFOOD.isActive(MinecraftClient.getInstance().player)) {
            cir.setReturnValue(0);
        }
    }
}
