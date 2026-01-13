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

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.GlassBottleItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.stat.Stats;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;
import net.scirave.theplaneswalker.origins.TCPowers;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = GlassBottleItem.class, priority = 999)
public abstract class GlassBottleItemMixin {

    @Inject(method = "use", at = @At("RETURN"), cancellable = true)
    public void useBottle(World world, PlayerEntity user, Hand hand, CallbackInfoReturnable<TypedActionResult<ItemStack>> cir) {
        if (cir.getReturnValue().getResult() == ActionResult.PASS) {
            if (TCPowers.SOULFOOD.isActive(user)) {
                int xpCost = 3 + world.random.nextInt(9);
                boolean canUseXp = getTotalExperiencePoints(user) >= xpCost;
                if (canUseXp || user.getHungerManager().getFoodLevel() >= 6) {
                    ItemStack stack = user.getStackInHand(hand);
                    if (canUseXp) {
                        user.addExperience(-xpCost);
                    } else {
                        reduceHungerOnly(user.getHungerManager(), 6);
                    }
                    user.giveItemStack(Items.EXPERIENCE_BOTTLE.getDefaultStack());
                    stack.decrement(1);
                    user.incrementStat(Stats.USED.getOrCreateStat((GlassBottleItem) (Object) this));
                    cir.setReturnValue(TypedActionResult.success(stack, true));
                }
            }
        }
    }

    @Unique
    private static void reduceHungerOnly(net.minecraft.entity.player.HungerManager hungerManager, int amount) {
        int currentFood = hungerManager.getFoodLevel();
        float currentSaturation = hungerManager.getSaturationLevel();
        hungerManager.setFoodLevel(Math.max(0, currentFood - amount));
        hungerManager.setSaturationLevel(currentSaturation);
    }

    @Unique
    private static int getTotalExperiencePoints(PlayerEntity player) {
        int level = player.experienceLevel;
        int progress = Math.round(player.experienceProgress * player.getNextLevelExperience());
        return getXpForLevel(level) + progress;
    }

    @Unique
    private static int getXpForLevel(int level) {
        if (level <= 16) {
            return level * level + 6 * level;
        }
        if (level <= 31) {
            return (int) (2.5 * level * level - 40.5 * level + 360);
        }
        return (int) (4.5 * level * level - 162.5 * level + 2220);
    }

}
