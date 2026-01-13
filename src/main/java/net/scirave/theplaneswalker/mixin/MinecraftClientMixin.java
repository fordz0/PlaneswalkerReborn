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

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.sound.MusicSound;
import net.scirave.theplaneswalker.ThePlaneswalker;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = MinecraftClient.class, priority = 999)
public class MinecraftClientMixin {


    @Shadow
    @Nullable
    public ClientWorld world;

    @Inject(method = "getMusicType", at = @At("HEAD"), cancellable = true)
    public void replaceMusic(CallbackInfoReturnable<MusicSound> cir) {
        if (this.world != null) {
            if (this.world.getRegistryKey().getValue().equals(ThePlaneswalker.DIMENSION)) {
                cir.setReturnValue(ThePlaneswalker.MUSIC);
            }
        }
    }

}
