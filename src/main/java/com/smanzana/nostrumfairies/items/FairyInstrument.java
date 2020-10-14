package com.smanzana.nostrumfairies.items;

import java.util.List;

import com.smanzana.nostrumfairies.NostrumFairies;
import com.smanzana.nostrumfairies.capabilities.fey.INostrumFeyCapability;
import com.smanzana.nostrumfairies.client.gui.NostrumFairyGui;
import com.smanzana.nostrumfairies.sound.NostrumFairiesSounds;
import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.client.gui.infoscreen.InfoScreenTabs;
import com.smanzana.nostrummagica.loretag.ILoreTagged;
import com.smanzana.nostrummagica.loretag.Lore;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * Magical instrument that allows you to open up the fairy GUI and interact with personal fairies
 * @author Skyler
 *
 */
public class FairyInstrument extends Item implements ILoreTagged {

	public static enum InstrumentType {
		FLUTE("flute", "fairy_instrument_flute"),
		HARP("harp", "fairy_instrument_harp"),
		OCARINA("ocarina", "fairy_instrument_ocarina");
		
		private final String suffix;
		private final String model;
		
		private InstrumentType(String suffix, String model) {
			this.suffix = suffix;;
			this.model = model;
		}
		
		public String getSuffix() {
			return suffix;
		}
		
		public String getModelName() {
			return model;
		}
	}
	
	public static final String ID = "fairy_instrument";
	
	private static FairyInstrument instance = null;
	public static FairyInstrument instance() {
		if (instance == null)
			instance = new FairyInstrument();
		
		return instance;
	}
	
	public static void init() {
		;
	}
	
	public FairyInstrument() {
		super();
		this.setUnlocalizedName(ID);
		this.setMaxDamage(0);
		this.setMaxStackSize(1);
		this.setCreativeTab(NostrumFairies.creativeTab);
		this.setHasSubtypes(true);
	}
	
	@Override
	public String getUnlocalizedName(ItemStack stack) {
		int i = stack.getMetadata();
		
		String suffix = typeFromMeta(i).getSuffix();
		
		return this.getUnlocalizedName() + "." + suffix;
	}
	
	@SideOnly(Side.CLIENT)
	public String getModelName(InstrumentType type) {
		return type.getModelName();
	}
	
	/**
     * returns a list of items with the same ID, but different meta (eg: dye returns 16 items)
     */
    @SideOnly(Side.CLIENT)
    @Override
	public void getSubItems(Item itemIn, CreativeTabs tab, List<ItemStack> subItems) {
    	for (InstrumentType type: InstrumentType.values()) {
    		subItems.add(create(type));
    	}
	}
    
    public static ItemStack create(InstrumentType type) {
    	return new ItemStack(instance(), 1, metaFromType(type));
    }
	
	protected static int metaFromType(InstrumentType type) {
		return type.ordinal();
	}
	
	protected InstrumentType typeFromMeta(int meta) {
		return InstrumentType.values()[meta % InstrumentType.values().length];
	}
	
    @Override
	public String getLoreKey() {
		return "fairy_instrument";
	}

	@Override
	public String getLoreDisplayName() {
		return "Fairy Instruments";
	}
	
	@Override
	public Lore getBasicLore() {
		return new Lore().add("");
				
	}
	
	@Override
	public Lore getDeepLore() {
		return new Lore().add("");
	}

	@Override
	public InfoScreenTabs getTab() {
		return InfoScreenTabs.INFO_ITEMS;
	}

	public InstrumentType getType(ItemStack stack) {
		return typeFromMeta(stack.getMetadata());
	}
	
	@Override
	public EnumActionResult onItemUse(ItemStack stack, EntityPlayer playerIn, World worldIn, BlockPos pos, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
		return EnumActionResult.PASS;
	}
	
	@Override
	public ActionResult<ItemStack> onItemRightClick(ItemStack stack, World worldIn, EntityPlayer playerIn, EnumHand hand) {
		InstrumentType type = getType(stack);
		if (!worldIn.isRemote) {
			final NostrumFairiesSounds sound;
			switch (type) {
			case FLUTE:
			default:
				sound = NostrumFairiesSounds.FLUTE;
				break;
			case HARP:
				sound = NostrumFairiesSounds.LYRE;
				break;
			case OCARINA:
				sound = NostrumFairiesSounds.OCARINA;
				break;
			}
			
			sound.play(worldIn, playerIn.posX, playerIn.posY, playerIn.posZ);
			
			INostrumFeyCapability attr = NostrumFairies.getFeyWrapper(playerIn);
			if (attr != null && attr.isUnlocked()) {

				// Would be cooler if the fairies flew in towards you after you called
				attr.disableFairies(40);
				
				NostrumMagica.playerListener.registerTimer((t, entity, data) -> {
					if (!playerIn.isDead && playerIn.getHeldItem(hand) == stack) {
						playerIn.openGui(NostrumFairies.instance, NostrumFairyGui.fairyGuiID, worldIn, (int) playerIn.posX, (int) playerIn.posY, (int) playerIn.posZ);
					}
					return true;
				}, 30, 0);
			} else {
				playerIn.addChatComponentMessage(new TextComponentTranslation("info.instrument.locked"));
			}
		}
		
		return ActionResult.newResult(EnumActionResult.PASS, stack);
	}
	
}