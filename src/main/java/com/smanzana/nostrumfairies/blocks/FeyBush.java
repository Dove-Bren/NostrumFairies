package com.smanzana.nostrumfairies.blocks;

import java.util.List;
import java.util.Random;

import com.google.common.collect.Lists;
import com.smanzana.nostrumfairies.NostrumFairies;
import com.smanzana.nostrumfairies.entity.fey.EntityFeyBase;
import com.smanzana.nostrumfairies.entity.fey.EntityPersonalFairy;
import com.smanzana.nostrumfairies.entity.fey.IFeyWorker.FairyGeneralStatus;
import com.smanzana.nostrumfairies.items.FeyResource;
import com.smanzana.nostrumfairies.items.FeyResource.FeyResourceType;
import com.smanzana.nostrummagica.sound.NostrumMagicaSounds;

import net.minecraft.block.BlockBush;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;
import net.minecraft.util.EnumParticleTypes;
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
	
	public static void init() {
//    	GameRegistry.register(
//    			(new ItemBlock(instance)).setRegistryName(instance.getID())
//    		.setCreativeTab(NostrumMagica.creativeTab).setUnlocalizedName(instance.getID()));
	}
	
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
	public boolean isVisuallyOpaque() {
		return false;
	}
	
	@Override
	public boolean isOpaqueCube(IBlockState state) {
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
//	public IBlockState getState(Type type) {
//		return getDefaultState().withProperty(TYPE, type);
//	}
	
//	@Override
//	public IBlockState getStateFromMeta(int meta) {
//		
//		if (meta == 0)
//			return getDefaultState().withProperty(TYPE, Type.MIDNIGHT_IRIS);
//		if (meta == 1)
//			return getDefaultState().withProperty(TYPE, Type.CRYSTABLOOM);
//		
//		return getDefaultState();
//	}
	
	@Override
	public Item getItemDropped(IBlockState state, Random rand, int fortune) {
		return null;
       //return super.getItemDropped(state, rand, fortune);
    }
	
	@Override
	public int quantityDropped(IBlockState state, int fortune, Random random) {
		return super.quantityDropped(state, fortune, random);
	}
	
	@SideOnly(Side.CLIENT)
    public void getSubBlocks(Item itemIn, CreativeTabs tab, List<ItemStack> list) {
		super.getSubBlocks(itemIn, tab, list);
	}
	
	@Override
	public int damageDropped(IBlockState state) {
		return super.damageDropped(state);
	}
	
//	@Override
//	public ItemStack getPickBlock(IBlockState state, RayTraceResult target, World world, BlockPos pos, EntityPlayer player) {
//		return new ItemStack(Item.getItemFromBlock(this), 1, getMetaFromState(state));
//	}
	
	@Override
	public boolean canSilkHarvest(World world, BlockPos pos, IBlockState state, EntityPlayer player) {
		return true;
	}
	
	@Override
	public void randomTick(World worldIn, BlockPos pos, IBlockState state, Random random) {
		super.randomTick(worldIn, pos, state, random);
	}
	
	@Override
	protected boolean canSustainBush(IBlockState state) {
		boolean ret = super.canSustainBush(state);
		
//		if (!ret && state.getBlock() instanceof MagicDirt) {
//			ret = true;
//		}
		
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

	public boolean getEntityInteraction(ItemStack stack, EntityPlayer playerIn, EntityLivingBase target, EnumHand hand) {
		if (target instanceof EntityFeyBase && !(target instanceof EntityPersonalFairy)) {
			EntityFeyBase fey = (EntityFeyBase) target;
			if (fey.getStatus() == FairyGeneralStatus.WANDERING) {
				if (!target.worldObj.isRemote) {
					target.entityDropItem(FeyResource.create(FeyResourceType.TABLET, 1), .1f);
					((WorldServer) target.worldObj).spawnParticle(EnumParticleTypes.HEART,
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
				}
				
				return true;
			}
		}
		return false;
	}
}
