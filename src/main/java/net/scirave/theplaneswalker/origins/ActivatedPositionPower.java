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

package net.scirave.theplaneswalker.origins;

import io.github.apace100.apoli.condition.EntityCondition;
import io.github.apace100.apoli.power.PowerConfiguration;
import net.minecraft.util.math.BlockPos;
import java.util.Optional;
import org.jetbrains.annotations.NotNull;

public class ActivatedPositionPower extends PositionPower {

    public int range;

    public ActivatedPositionPower(BlockPos pos, int range, Optional<EntityCondition> condition) {
        super(pos, condition);
        this.range = range;
    }

    @Override
    public @NotNull PowerConfiguration<?> getConfig() {
        return TCPowers.ACTIVATED_POSITION;
    }

}
