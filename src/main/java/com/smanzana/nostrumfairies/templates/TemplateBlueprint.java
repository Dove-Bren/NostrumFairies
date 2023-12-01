package com.smanzana.nostrumfairies.templates;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.annotation.Nullable;

import com.smanzana.nostrumfairies.blocks.TemplateBlock;
import com.smanzana.nostrummagica.world.blueprints.RoomBlueprint;
import com.smanzana.nostrummagica.world.blueprints.RoomBlueprint.BlueprintBlock;
import com.smanzana.nostrummagica.world.blueprints.RoomBlueprint.IBlueprintSpawner;
import com.smanzana.nostrummagica.world.blueprints.RoomBlueprint.INBTGenerator;
import com.smanzana.nostrummagica.world.blueprints.RoomBlueprint.SpawnContext;

import net.minecraft.block.BlockState;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants.NBT;

/**
 * Wraps up a base blueprint, adds an ID and some basic caching, and replaces spawning
 * with template block spawning
 * @author Skyler
 *
 */
public class TemplateBlueprint implements IBlueprintSpawner {
	
	// TODO set of acceptable blockstates that may have tile entities that we're okay to just put down anyways

	private static final String NBT_ID = "id";
	private static final String NBT_BLUEPRINT = "blueprint";
	
	protected final RoomBlueprint blueprint;
	protected final UUID id;
	
	public TemplateBlueprint(RoomBlueprint blueprint) {
		this(UUID.randomUUID(), blueprint);
	}
	
	protected TemplateBlueprint(UUID id, RoomBlueprint blueprint) {
		this.blueprint = blueprint;
		this.id = id;
		blueprint.setSpawningFunc(this);
		RegisterBlueprint(id, this);
	}

	@Override
	public synchronized void spawnBlock(SpawnContext context, BlockPos pos, Direction direction, BlueprintBlock block) {
		// Templating doesn't mess with clearing blocks
		BlockState existingState = context.world.getBlockState(pos);
		if (existingState != null && !existingState.getMaterial().isReplaceable() && !context.world.isAirBlock(pos)) {
			return; // Skip!
		}
		
		if (block.getTileEntityData() != null) {
			// Templating doesn't do tile entities, either
			return;
		}
		
		BlockState placeState = block.getSpawnState(direction);
		if (placeState != null && !(placeState.getBlock() instanceof TemplateBlock)) {
			TemplateBlock.SetTemplate(context.world.getWorld(), pos, placeState);
			spawnedBlocks.add(pos.toImmutable());
		} else {
			; // Templating doesn't mess with air or template blocks
		}
	}
	
	private List<BlockPos> spawnedBlocks = null;

	public synchronized List<BlockPos> spawn(World world, BlockPos origin, Direction direction) {
		spawnedBlocks = new ArrayList<>();
		blueprint.spawn(world, origin, direction, UUID.randomUUID());
		return spawnedBlocks;
	}
	
	public CompoundNBT toNBT() {
		CompoundNBT nbt = new CompoundNBT();
		
		nbt.putUniqueId(NBT_ID, id);
		
		ListNBT list = new ListNBT();
		INBTGenerator gen = blueprint.toNBTWithBreakdown();
		while (gen.hasNext()) {
			list.add(gen.next());
		}
		
		nbt.put(NBT_BLUEPRINT, list);
		
		return nbt;
	}
	
	public static final TemplateBlueprint fromNBT(CompoundNBT nbt) {
		UUID id = nbt.getUniqueId(NBT_ID);
		TemplateBlueprint blueprint = GetRegisteredBlueprint(id);
		if (blueprint != null) {
			return blueprint; // Whoo! Cached!
		}
		
		RoomBlueprint roomPrint = null;
		ListNBT list = nbt.getList(NBT_BLUEPRINT, NBT.TAG_COMPOUND);
		for (int i = 0; i < list.size(); i++) {
			CompoundNBT blueprintTag = list.getCompound(i);
			RoomBlueprint room = RoomBlueprint.fromNBT(blueprintTag);
			
			if (roomPrint == null) {
				roomPrint = room;
			} else {
				roomPrint = roomPrint.join(room);
			}
		}
		
		return new TemplateBlueprint(id, roomPrint);
	}
	
	private static final Map<UUID, TemplateBlueprint> registry = new HashMap<>();
	
	protected static final void RegisterBlueprint(UUID id, TemplateBlueprint blueprint) {
		registry.put(id, blueprint);
	}
	
	protected static final @Nullable TemplateBlueprint GetRegisteredBlueprint(UUID id) {
		return registry.get(id);
	}

	public BlueprintBlock[][][] getPreview() {
		return blueprint.getPreview();
	}
}
