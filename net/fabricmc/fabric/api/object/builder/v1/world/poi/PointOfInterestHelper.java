/*
 * Copyright (c) 2016, 2017, 2018, 2019 FabricMC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.fabricmc.fabric.api.object.builder.v1.world.poi;

import java.util.Set;
import net.minecraft.class_2248;
import net.minecraft.class_2680;
import net.minecraft.class_2960;
import net.minecraft.class_4156;
import net.minecraft.class_4158;
import net.minecraft.class_5321;
import net.minecraft.class_7477;
import net.minecraft.class_7923;
import net.minecraft.class_7924;
import com.google.common.collect.ImmutableSet;

/**
 * This class provides utilities to create a {@link class_4158}.
 *
 * <p>A point of interest is typically used by villagers to specify their workstation blocks, meeting zones and homes.
 * Points of interest are also used by bees to specify where their bee hive is and nether portals to find existing portals.
 */
public final class PointOfInterestHelper {
	private PointOfInterestHelper() {
	}

	/**
	 * Creates and registers a {@link class_4158}.
	 *
	 * @param id The id of this {@link class_4158}.
	 * @param ticketCount the amount of tickets.
	 * @param searchDistance the search distance.
	 * @param blocks all the blocks where a {@link class_4156} of this type will be present.
	 * @return a new {@link class_4158}.
	 */
	public static class_4158 register(class_2960 id, int ticketCount, int searchDistance, class_2248... blocks) {
		final ImmutableSet.Builder<class_2680> builder = ImmutableSet.builder();

		for (class_2248 block : blocks) {
			builder.addAll(block.method_9595().method_11662());
		}

		return register(id, ticketCount, searchDistance, builder.build());
	}

	/**
	 * Creates and registers a {@link class_4158}.
	 *
	 * @param id the id of this {@link class_4158}.
	 * @param ticketCount the amount of tickets.
	 * @param searchDistance the search distance.
	 * @param blocks all {@link class_2680 block states} where a {@link class_4156} of this type will be present
	 * @return a new {@link class_4158}.
	 */
	public static class_4158 register(class_2960 id, int ticketCount, int searchDistance, Iterable<class_2680> blocks) {
		final ImmutableSet.Builder<class_2680> builder = ImmutableSet.builder();

		return register(id, ticketCount, searchDistance, builder.addAll(blocks).build());
	}

	// INTERNAL METHODS

	private static class_4158 register(class_2960 id, int ticketCount, int searchDistance, Set<class_2680> states) {
		return class_7477.method_43993(class_7923.field_41128, class_5321.method_29179(class_7924.field_41212, id), states, ticketCount, searchDistance);
	}
}
