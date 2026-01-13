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

package net.scirave.theplaneswalker;

import net.fabricmc.api.ModInitializer;
import net.minecraft.item.Item;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.sound.MusicSound;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Identifier;
import net.minecraft.util.Rarity;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.scirave.theplaneswalker.origins.TCEntityActions;
import net.scirave.theplaneswalker.origins.TCEntityConditions;
import net.scirave.theplaneswalker.origins.TCPowers;
import net.scirave.theplaneswalker.helpers.VoidDuelTracker;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;

public class ThePlaneswalker implements ModInitializer {

    public static String MODID = "theplaneswalker";

    public static Identifier ONEIRI_ID = new Identifier(MODID, "oneiri");

    public static SoundEvent ONEIRI = SoundEvent.of(ONEIRI_ID);

    public static RegistryEntry<SoundEvent> ONEIRI_ENTRY = RegistryEntry.of(ONEIRI);

    public static MusicSound MUSIC = new MusicSound(ONEIRI_ENTRY, 300, 600, false);

    public static Identifier DIMENSION = new Identifier(MODID, "void");

    public static Item PLANESWALKER_SIGIL = new Item(new Item.Settings().rarity(Rarity.EPIC));

    @Override
    public void onInitialize() {
        Registry.register(Registries.SOUND_EVENT, ONEIRI_ID, ONEIRI);
        Registry.register(Registries.ITEM, new Identifier(MODID, "planeswalker_sigil"), PLANESWALKER_SIGIL);
        TCPowers.initialization();
        TCEntityActions.initialization();
        TCEntityConditions.initialization();
        ServerTickEvents.END_SERVER_TICK.register(VoidDuelTracker::tick);
    }
}
