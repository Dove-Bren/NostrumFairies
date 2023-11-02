package com.smanzana.nostrumfairies.blocks;

import java.util.List;
import java.util.Random;

import com.google.common.collect.Lists;
import com.smanzana.nostrumfairies.NostrumFairies;
import com.smanzana.nostrumfairies.entity.fey.EntityFeyBase;
import com.smanzana.nostrumfairies.entity.fey.EntityPersonalFairy;
import com.smanzana.nostrumfairies.items.FeyResource;
import com.smanzana.nostrumfairies.items.FeyResource.FeyResourceType;
import com.smanzana.nostrumfairies.serializers.FairyGeneralStatus;
import com.smanzana.nostrummagica.sound.NostrumMagicaSounds;

import net.minecraft.block.BlockBush;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.BlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.IShearable;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * @author Skyler
 *
 */
public class FeyBush extends BlockBush implements IShearable {
	
	private static FeyBush instance = null;
	public static FeyBush instance() {
		if (instance == null)
			instance = new FeyBush();
		
		return instance;
	};
	
	public static final String ID = "fey_bush";
	
	public FeyBush() {
		super(Material.PLANTS);
		this.blockSoundType = SoundType.PLANT;
		
		this.setUnlocalizedName(ID);
		this.setCreativeTab(NostrumFairies.creativeTab);
	}
	
	@Override
	public boolean isPassable(IBlockAccess worldIn, BlockPos pos) {
        return true;
    }
	
	@Override
	public boolean isOpaqueCube(BlockState state) {
		return false;
	}
	
	@Override
	public boolean isReplaceable(IBlockAccess worldIn, BlockPos pos) {
        return false;
    }
	
//	@Override
//	protected BlockStateContainer createBlockState() {
//		return new BlockStateContainer(this, TYPE);
//	}
//	
//	public BlockState getState(Type type) {
//		return getDefaultState().withProperty(TYPE, type);
//	}
	
//	@Override
//	public BlockState getStateFromMeta(int meta) {
//		
//		if (meta == 0)
//			return getDefaultState().withProperty(TYPE, Type.MIDNIGHT_IRIS);
//		if (meta == 1)
//			return getDefaultState().withProperty(TYPE, Type.CRYSTABLOOM);
//		
//		return getDefaultState();
//	}
	
	@Override
	public Item getItemDropped(BlockState state, Random rand, int fortune) {
		return null;
       //return super.getItemDropped(state, rand, fortune);
    }
	
	@Override
	public int quantityDropped(BlockState state, int fortune, Random random) {
		return super.quantityDropped(state, fortune, random);
	}
	
	@OnlyIn(Dist.CLIENT)
    public void getSubBlocks(CreativeTabs tab, NonNullList<ItemStack> list) {
		super.getSubBlocks(tab, list);
	}
	
	@Override
	public int damageDropped(BlockState state) {
		return super.damageDropped(state);
	}
	
//	@Override
//	public ItemStack getPickBlock(BlockState state, RayTraceResult target, World world, BlockPos pos, PlayerEntity player) {
//		return new ItemStack(Item.getItemFromBlock(this), 1, getMetaFromState(state));
//	}
	
	@Override
	public boolean canSilkHarvest(World world, BlockPos pos, BlockState state, PlayerEntity player) {
		return true;
	}
	
	@Override
	public void randomTick(World worldIn, BlockPos pos, BlockState state, Random random) {
		super.randomTick(worldIn, pos, state, random);
	}
	
	@Override
	protected boolean canSustainBush(BlockState state) {
		boolean ret = super.canSustainBush(state);
		
		return ret;
	}

	@Override
	public boolean isShearable(ItemStack item, IBlockAccess world, BlockPos pos) {
		return true;
	}

	@Override
	public List<ItemStack> onSheared(ItemStack item, IBlockAccess world, BlockPos pos, int fortune) {
		return Lists.newArrayList(new ItemStack(this));
	}

	public boolean getEntityInteraction(ItemStack stack, PlayerEntity playerIn, LivingEntity target, Hand hand) {
		if (target instanceof EntityFeyBase && !(target instanceof EntityPersonalFairy)) {
			EntityFeyBase fey = (EntityFeyBase) target;
			if (fey.getStatus() == FairyGeneralStatus.WANDERING) {
				if (!target.world.isRemote) {
					target.entityDropItem(FeyResource.create(FeyResourceType.TABLET, 1), .1f);
					((WorldServer) target.world).spawnParticle(EnumParticleTypes.HEART,
							target.posX,
							target.posY,	
							target.posZ,
							10,
							.2,
							.25,
							.2,
							.1,
							new int[0]);
					NostrumMagicaSounds.AMBIENT_WOOSH2.play(target);
					stack.shrink(1);
				}
				
				return true;
			}
		}
		
		return false;
	}
}
