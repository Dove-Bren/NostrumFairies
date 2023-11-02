package com.smanzana.nostrumfairies.items;

import java.util.List;

import javax.annotation.Nullable;

import com.smanzana.nostrumfairies.NostrumFairies;
import com.smanzana.nostrumfairies.templates.TemplateBlueprint;
import com.smanzana.nostrummagica.client.gui.infoscreen.InfoScreenTabs;
import com.smanzana.nostrummagica.loretag.ILoreTagged;
import com.smanzana.nostrummagica.loretag.Lore;
import com.smanzana.nostrummagica.world.blueprints.RoomBlueprint;

import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.Direction;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

/**
 * Stores a template
 * @author Skyler
 *
 */
public class TemplateScroll extends Item implements ILoreTagged {

	public static final String ID = "template_scroll";
	
	private static TemplateScroll instance = null;
	public static TemplateScroll instance() {
		if (instance == null)
			instance = new TemplateScroll();
		
		return instance;
	}
	
	public TemplateScroll() {
		super();
		this.setUnlocalizedName(ID);
		this.setRegistryName(ID);
		this.setMaxDamage(0);
		this.setMaxStackSize(1);
		this.setHasSubtypes(false);
		this.setCreativeTab(NostrumFairies.creativeTab);
	}
	
	@Override
	public String getHighlightTip(ItemStack item, String displayName ) {
		return displayName;
	}
	
	private static final String NBT_TEMPLATE = "template";
	
	public static TemplateBlueprint GetTemplate(ItemStack stack) {
		if (stack.isEmpty() || !(stack.getItem() instanceof TemplateScroll) || !stack.hasTagCompound()) {
			return null;
		}
		
		CompoundNBT nbt = stack.getTagCompound();
		CompoundNBT blueprintTag = nbt.getCompoundTag(NBT_TEMPLATE);
		return TemplateBlueprint.fromNBT(blueprintTag);
	}
	
	public static ItemStack Capture(World world, BlockPos pos1, BlockPos pos2) {
		RoomBlueprint blueprint = new RoomBlueprint(world, pos1, pos2, false);
		return Create(blueprint);
	}
	
	public static ItemStack Capture(World world, BlockPos pos1, BlockPos pos2, BlockPos origin, Direction facing) {
		RoomBlueprint blueprint = new RoomBlueprint(world, pos1, pos2, false, origin, facing);
		return Create(blueprint);
	}
	
	public static ItemStack Create(RoomBlueprint blueprint) {
		return Create(new TemplateBlueprint(blueprint));
	}
	
	public static ItemStack Create(TemplateBlueprint blueprint) {
		ItemStack stack = new ItemStack(instance());
		SetTemplate(stack, blueprint);
		stack.setStackDisplayName("New Template");
		return stack;
	}
	
	protected static void SetTemplate(ItemStack stack, TemplateBlueprint blueprint) {
		SetTemplate(stack, blueprint.toNBT());
	}
	
	protected static void SetTemplate(ItemStack stack, CompoundNBT blueprintTag) {
		if (stack.isEmpty() || !(stack.getItem() instanceof TemplateScroll)) {
			return;
		}
		
		CompoundNBT nbt = stack.getTagCompound();
		if (nbt == null) {
			nbt = new CompoundNBT();
		}
		
		nbt.setTag(NBT_TEMPLATE, blueprintTag);
		stack.setTagCompound(nbt);
	}
	
	@Override
	public String getLoreKey() {
		return "template_scroll";
	}

	@Override
	public String getLoreDisplayName() {
		return "Template Slips";
	}
	
	@Override
	public Lore getBasicLore() {
		return new Lore().add("Template slips are created using the template wand." , "They store templates of a structure.");
				
	}
	
	@Override
	public Lore getDeepLore() {
		return new Lore().add("Template slips are created using the template wand." , "They store templates of a structure.", "These slips can be inserted into the template wand to be placed.", "They can also be put into building blocks to have workers attempt to build and maintain a structure.");
	}

	@Override
	public InfoScreenTabs getTab() {
		return InfoScreenTabs.INFO_ITEMS;
	}
	
	@Override
	public EnumActionResult onItemUse(PlayerEntity playerIn, World worldIn, BlockPos pos, EnumHand hand, Direction facing, float hitX, float hitY, float hitZ) {
		return EnumActionResult.PASS;
	}
	
	@Override
	public boolean itemInteractionForEntity(ItemStack stack, PlayerEntity playerIn, LivingEntity target, EnumHand hand) {
		return super.itemInteractionForEntity(stack, playerIn, target, hand);
	}
	
	@Override
	public void addInformation(ItemStack stack, @Nullable World worldIn, List<String> tooltip, ITooltipFlag flagIn) {
		super.addInformation(stack, worldIn, tooltip, flagIn);
	}
	
}
