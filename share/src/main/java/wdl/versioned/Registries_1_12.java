/*
 * This file is part of World Downloader: A mod to make backups of your
 * multiplayer worlds.
 * http://www.minecraftforum.net/forums/mapping-and-modding/minecraft-mods/2520465
 *
 * Copyright (c) 2014 nairol, cubic72
 * Copyright (c) 2018 Pokechu22, julialy
 *
 * This project is licensed under the MMPLv2.  The full text of the MMPL can be
 * found in LICENSE.md, or online at https://github.com/iopleke/MMPLv2/blob/master/LICENSE.md
 * For information about this the MMPLv2, see http://stopmodreposts.org/
 *
 * Do not redistribute (in modified or unmodified form) without prior permission.
 */
package wdl.versioned;

import net.minecraft.block.Block;
import net.minecraft.world.biome.Biome;

/**
 * Contains functions that interact with registries.
 *
 * This version is used in versions before 1.13.1, including 1.13.0.
 */
final class RegistryFunctions {
	private RegistryFunctions() { throw new AssertionError(); }

	/* (non-javadoc)
	 * @see VersionedFunctions#getBlockId
	 */
	static final int getBlockId(Block block) {
		return Block.REGISTRY.getId(block);
	}
	/* (non-javadoc)
	 * @see VersionedFunctions#getBiomeId
	 */
	static final int getBiomeId(Biome biome) {
		return Biome.REGISTRY.getId(biome);
	}
}
