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

import io.github.apace100.apoli.component.PowerHolderComponent;
import net.minecraft.network.packet.s2c.play.PositionFlag;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.scirave.theplaneswalker.origins.OnTeleportPower;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Set;

@Mixin(ServerPlayNetworkHandler.class)
public class ServerPlayerNetworkHandlerMixin {

    @Shadow
    public ServerPlayerEntity player;

    @Inject(method = "requestTeleport(DDDFFLjava/util/Set;)V", at = @At("HEAD"))
    public void teleporting(double x, double y, double z, float yaw, float pitch, Set<PositionFlag> flags, CallbackInfo ci) {
        PowerHolderComponent.getPowers(this.player, OnTeleportPower.class).forEach(OnTeleportPower::onTeleport);
    }

}
