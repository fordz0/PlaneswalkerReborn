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

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.LightmapTextureManager;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;
import org.joml.Matrix4f;
import net.scirave.theplaneswalker.origins.ActivatedPositionPower;
import net.scirave.theplaneswalker.origins.TCPowers;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.HashMap;
import java.util.HashSet;

@Mixin(value = WorldRenderer.class, priority = 999)
public abstract class WorldRendererMixin {

    private static final BlockState AIR_STATE = Blocks.AIR.getDefaultState();

    private final HashMap<BlockPos, BlockState> savedStates = new HashMap<>();
    private final HashSet<PlayerEntity> listOfUsers = new HashSet<>();

    @Shadow
    private ClientWorld world;

    @Shadow
    public abstract void scheduleBlockRenders(int minX, int minY, int minZ, int maxX, int maxY, int maxZ);

    @Inject(at = @At(value = "HEAD"), method = "render")
    private void beforeRender(MatrixStack matrices, float tickDelta, long limitTime, boolean renderBlockOutline, Camera camera, GameRenderer gameRenderer, LightmapTextureManager lightmapTextureManager, Matrix4f matrix4f, CallbackInfo ci) {

        for (PlayerEntity plr : world.getPlayers()) {

            ActivatedPositionPower power = (ActivatedPositionPower) TCPowers.DIMENSIONAL_RIFT.get(plr);

            if (power != null) {
                BlockPos powerPos = power.pos;
                int range = power.range;

                if (power.isActive()) {

                    if (listOfUsers.add(plr)) {
                        scheduleBlockRenders(powerPos.getX() - range, powerPos.getY() - range, powerPos.getZ() - range, powerPos.getX() + range, powerPos.getY() + range, powerPos.getZ() + range);
                    }

                    for (int a = -range; a <= range; a++) {
                        int range2 = Math.abs(a);
                        for (int b = -range + range2; b <= (range - range2); b++) {
                            int range3 = Math.abs(b);
                            for (int c = -range + range2 + range3; c <= (range - range2 - range3); c++) {
                                BlockPos pos = new BlockPos(power.pos.getX() + a, power.pos.getY() + b, power.pos.getZ() + c);
                                if (!savedStates.containsKey(pos)) {
                                    BlockState state = world.getBlockState(pos);
                                    if (!state.isAir()) {
                                        savedStates.put(pos, state);
                                        world.getWorldChunk(pos).setBlockState(pos, AIR_STATE, false);
                                    }
                                }
                            }
                        }
                    }
                } else if (listOfUsers.remove(plr)) {
                    scheduleBlockRenders(powerPos.getX() - range, powerPos.getY() - range, powerPos.getZ() - range, powerPos.getX() + range, powerPos.getY() + range, powerPos.getZ() + range);
                }
            }

        }

    }

    @Inject(at = @At("TAIL"), method = "render")
    public void afterRender(MatrixStack matrices, float tickDelta, long limitTime, boolean renderBlockOutline, Camera camera, GameRenderer gameRenderer, LightmapTextureManager lightmapTextureManager, Matrix4f matrix4f, CallbackInfo ci) {
        savedStates.forEach((pos, state) -> world.getWorldChunk(pos).setBlockState(pos, state, false));
        savedStates.clear();
    }
}
