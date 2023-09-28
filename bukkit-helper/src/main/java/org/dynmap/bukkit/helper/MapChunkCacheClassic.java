/*
 * Copyright 2023 Andrew121410
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * [5](http://www.apache.org/licenses/LICENSE-2.0)
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

// Modified by Andrew121410 on 28 Sep 2023
// Added try and catch to getBlockSkyLight and getBlockEmittedLight as seen in GenericMapChunkCache and AbstractMapChunkCache
package org.dynmap.bukkit.helper;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

import org.bukkit.World;
import org.bukkit.Chunk;
import org.bukkit.block.Biome;
import org.bukkit.ChunkSnapshot;
import org.dynmap.DynmapChunk;
import org.dynmap.DynmapCore;
import org.dynmap.DynmapWorld;
import org.dynmap.Log;
import org.dynmap.bukkit.helper.AbstractMapChunkCache.Snapshot;
import org.dynmap.bukkit.helper.SnapshotCache.SnapshotRec;
import org.dynmap.common.BiomeMap;
import org.dynmap.hdmap.HDBlockModels;
import org.dynmap.renderer.DynmapBlockState;
import org.dynmap.renderer.RenderPatchFactory;
import org.dynmap.utils.DynIntHashMap;
import org.dynmap.utils.MapChunkCache;
import org.dynmap.utils.MapIterator;
import org.dynmap.utils.BlockStep;
import org.dynmap.utils.VisibilityLimit;

/**
 * Container for managing chunks - dependent upon using chunk snapshots, since rendering is off server thread
 */
public class MapChunkCacheClassic extends AbstractMapChunkCache {

    public static class WrappedSnapshot implements Snapshot {
    	private final ChunkSnapshot ss;
    	private final int sectionmask;
		public WrappedSnapshot(ChunkSnapshot ss) {
    		this.ss = ss;
    		int mask = 0;
    		for (int i = 0; i < 16; i++) {
    			if (ss.isSectionEmpty(i))
    				mask |= (1 << i);
    		}
    		sectionmask = mask;
    	}
		@Override
    	public final DynmapBlockState getBlockType(int x, int y, int z) {
    		if ((sectionmask & (1 << (y >> 4))) != 0)
    			return DynmapBlockState.AIR;
            return BukkitVersionHelper.stateByID[(ss.getBlockTypeId(x & 0xF, y, z & 0xF) << 4) | ss.getBlockData(x & 0xF, y, z & 0xF)];
    	}
		@Override
        public final int getBlockSkyLight(int x, int y, int z) {
			try {
				return ss.getBlockSkyLight(x & 0xF, y, z & 0xF);
			} catch (ArrayIndexOutOfBoundsException aioobx) {
				return 15;
			}
        }
		@Override
        public final int getBlockEmittedLight(int x, int y, int z) {
			try {
				return ss.getBlockEmittedLight(x & 0xF, y, z & 0xF);
			} catch (ArrayIndexOutOfBoundsException aioobx) {
				return 0;
			}
        }
		@Override
        public final int getHighestBlockYAt(int x, int z) {
        	return ss.getHighestBlockYAt(x, z);
        }
		@Override
        public final Biome getBiome(int x, int z) {
        	return ss.getBiome(x & 0xF, z & 0xF);
        }
		@Override
        public final boolean isSectionEmpty(int sy) {
        	return (sectionmask & (1 << sy)) != 0;
        }
		@Override
        public final Object[] getBiomeBaseFromSnapshot() {
        	return BukkitVersionHelper.helper.getBiomeBaseFromSnapshot(ss);
        }
    }

    /**
     * Construct empty cache
     */
    public MapChunkCacheClassic() {
    	
    }

	@Override
	public Snapshot wrapChunkSnapshot(ChunkSnapshot css) {
		return new WrappedSnapshot(css);
	}
    
}
