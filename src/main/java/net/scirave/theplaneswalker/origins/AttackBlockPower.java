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

import io.github.apace100.apoli.action.EntityAction;
import io.github.apace100.apoli.condition.EntityCondition;
import io.github.apace100.apoli.power.PowerConfiguration;
import io.github.apace100.apoli.power.type.PowerType;
import java.util.Optional;
import org.jetbrains.annotations.NotNull;

public class AttackBlockPower extends PowerType {

    private final EntityAction entityAction;

    public AttackBlockPower(EntityAction entityAction, Optional<EntityCondition> condition) {
        super(condition);
        this.entityAction = entityAction;
    }

    public void onAttack() {
        entityAction.execute(getHolder());
    }

    public EntityAction getEntityAction() {
        return entityAction;
    }

    @Override
    public @NotNull PowerConfiguration<?> getConfig() {
        return TCPowers.ATTACK_BLOCK;
    }
}
