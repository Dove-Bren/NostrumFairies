package com.smanzana.nostrumfairies.templates;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.annotation.Nullable;

import com.smanzana.nostrumfairies.blocks.TemplateBlock;
import com.smanzana.nostrummagica.world.blueprints.Blueprint;
import com.smanzana.nostrummagica.world.blueprints.Blueprint.LoadContext;
import com.smanzana.nostrummagica.world.blueprints.BlueprintBlock;
import com.smanzana.nostrummagica.world.blueprints.BlueprintLocation;
import com.smanzana.nostrummagica.world.blueprints.BlueprintSpawnContext;
import com.smanzana.nostrummagica.world.blueprints.IBlueprint;
import com.smanzana.nostrummagica.world.blueprints.IBlueprintBlockPlacer;
import com.smanzana.nostrummagica.world.blueprints.IBlueprintScanner;

import net.minecraft.block.BlockState;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MutableBoundingBox;
import net.minecraft.world.IServerWorld;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;

/**
 * Wraps up a base blueprint, adds an ID and some basic caching, and replaces spawning
 * with template block spawning
 * @author Skyler
 *
 */
public class TemplateBlueprint implements IBlueprintBlockPlacer, IBlueprint {
	
	// TODO set of acceptable blockstates that may have tile entities that we're okay to just put down anyways

	private static final String NBT_ID = "id";
	private static final String NBT_BLUEPRINT = "blueprint";
	
	protected final Blueprint blueprint;
	protected final UUID id;
	
	public TemplateBlueprint(Blueprint blueprint) {
		this(UUID.randomUUID(), blueprint);
	}
	
	protected TemplateBlueprint(UUID id, Blueprint blueprint) {
		this.blueprint = blueprint;
		this.id = id;
		RegisterBlueprint(id, this);
	}

	@Override
	public synchronized boolean spawnBlock(BlueprintSpawnContext context, BlockPos pos, Direction direction, BlueprintBlock block) {
		// Templating doesn't mess with clearing blocks
		BlockState existingState = context.world.getBlockState(pos);
		if (existingState != null && !existingState.getMaterial().isReplaceable() && !context.world.isAirBlock(pos)) {
			return true; // Skip!
		}
		
		if (block.getTileEntityData() != null) {
			// Templating doesn't do tile entities, either
			return true;
		}
		
		BlockState placeState = block.getSpawnState(direction);
		if (placeState != null && !(placeState.getBlock() instanceof TemplateBlock)) {
			TemplateBlock.SetTemplate(((IServerWorld) context.world).getWorld(), pos, placeState);
			spawnedBlocks.add(pos.toImmutable());
		} else {
			; // Templating doesn't mess with air or template blocks
		}
		
		return true;
	}
	
	@Override
	public void finalizeBlock(BlueprintSpawnContext context, BlockPos pos, BlockState placedState, @Nullable TileEntity te, Direction direction, BlueprintBlock block) {
		;
	}
	
	private List<BlockPos> spawnedBlocks = null;

	public synchronized List<BlockPos> spawn(World world, BlockPos origin, Direction direction) {
		spawnedBlocks = new ArrayList<>();
		blueprint.spawn(world, origin, direction, null, this);
		return spawnedBlocks;
	}
	
	public CompoundNBT toNBT() {
		CompoundNBT nbt = new CompoundNBT();
		
		nbt.putUniqueId(NBT_ID, id);
		nbt.put(NBT_BLUEPRINT, blueprint.toNBT());
		
		return nbt;
	}
	
	public static final TemplateBlueprint fromNBT(CompoundNBT nbt) {
		UUID id = nbt.getUniqueId(NBT_ID);
		TemplateBlueprint blueprint = GetRegisteredBlueprint(id);
		if (blueprint != null) {
			return blueprint; // Whoo! Cached!
		}
		
		final LoadContext context = new LoadContext("TemplateBlueprint_NBT_load");
		Blueprint actualPrint = Blueprint.FromNBT(context, nbt);
		
		return new TemplateBlueprint(id, actualPrint);
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

	@Override
	public BlockPos getDimensions() {
		return blueprint.getDimensions();
	}

	@Override
	public BlueprintLocation getEntry() {
		return blueprint.getEntry();
	}

	@Override
	public void scanBlocks(IBlueprintScanner scanner) {
		blueprint.scanBlocks(scanner);
	}

	@Override
	public void spawn(IWorld world, BlockPos pos, Direction facing, MutableBoundingBox bounds, IBlueprintBlockPlacer placer) {
		blueprint.spawn(world, pos, facing, bounds, placer);
	}
}
