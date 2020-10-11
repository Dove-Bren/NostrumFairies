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

import net.minecraft.block.state.IBlockState;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.EnumFacing;
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
	public synchronized void spawnBlock(World world, BlockPos pos, EnumFacing direction, BlueprintBlock block) {
		// Templating doesn't mess with clearing blocks
		IBlockState existingState = world.getBlockState(pos);
		if (existingState != null && !existingState.getBlock().isReplaceable(world, pos) && !world.isAirBlock(pos)) {
			return; // Skip!
		}
		
		if (block.getTileEntityData() != null) {
			// Templating doesn't do tile entities, either
			return;
		}
		
		IBlockState placeState = block.getSpawnState(direction);
		if (placeState != null) {
			TemplateBlock.SetTemplate(world, pos, placeState);
			spawnedBlocks.add(pos.toImmutable());
		} else {
			; // Templating doesn't mess with air
		}
	}
	
	private List<BlockPos> spawnedBlocks = null;

	public synchronized List<BlockPos> spawn(World world, BlockPos origin, EnumFacing direction) {
		spawnedBlocks = new ArrayList<>();
		blueprint.spawn(world, origin, direction);
		return spawnedBlocks;
	}
	
	public NBTTagCompound toNBT() {
		NBTTagCompound nbt = new NBTTagCompound();
		
		nbt.setUniqueId(NBT_ID, id);
		
		NBTTagList list = new NBTTagList();
		INBTGenerator gen = blueprint.toNBTWithBreakdown();
		while (gen.hasNext()) {
			list.appendTag(gen.next());
		}
		
		nbt.setTag(NBT_BLUEPRINT, list);
		
		return nbt;
	}
	
	public static final TemplateBlueprint fromNBT(NBTTagCompound nbt) {
		UUID id = nbt.getUniqueId(NBT_ID);
		TemplateBlueprint blueprint = GetRegisteredBlueprint(id);
		if (blueprint != null) {
			return blueprint; // Whoo! Cached!
		}
		
		RoomBlueprint roomPrint = null;
		NBTTagList list = nbt.getTagList(NBT_BLUEPRINT, NBT.TAG_COMPOUND);
		for (int i = 0; i < list.tagCount(); i++) {
			NBTTagCompound blueprintTag = list.getCompoundTagAt(i);
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
