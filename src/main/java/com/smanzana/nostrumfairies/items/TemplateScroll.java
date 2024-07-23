package com.smanzana.nostrumfairies.items;

import java.util.List;

import javax.annotation.Nullable;

import com.smanzana.autodungeons.world.blueprints.Blueprint;
import com.smanzana.autodungeons.world.blueprints.BlueprintLocation;
import com.smanzana.nostrumfairies.templates.TemplateBlueprint;
import com.smanzana.nostrummagica.client.gui.infoscreen.InfoScreenTabs;
import com.smanzana.nostrummagica.loretag.ILoreTagged;
import com.smanzana.nostrummagica.loretag.Lore;

import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUseContext;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.world.World;

/**
 * Stores a template
 * @author Skyler
 *
 */
public class TemplateScroll extends Item implements ILoreTagged {

	public static final String ID = "template_scroll";
	private static final String NBT_TEMPLATE = "template";
	
	public TemplateScroll() {
		super(FairyItems.PropUnstackable());
	}
	
	@Override
	public ITextComponent getHighlightTip(ItemStack item, ITextComponent displayName) {
		return displayName;
	}
	
	public static TemplateBlueprint GetTemplate(ItemStack stack) {
		if (stack.isEmpty() || !(stack.getItem() instanceof TemplateScroll) || !stack.hasTag()) {
			return null;
		}
		
		CompoundNBT nbt = stack.getTag();
		CompoundNBT blueprintTag = nbt.getCompound(NBT_TEMPLATE);
		return TemplateBlueprint.fromNBT(blueprintTag);
	}
	
	public static ItemStack Capture(World world, BlockPos pos1, BlockPos pos2) {
		return Capture(world, pos1, pos2, null);
	}
	
	public static ItemStack Capture(World world, BlockPos pos1, BlockPos pos2, @Nullable BlueprintLocation origin) {
		Blueprint blueprint = Blueprint.Capture(world, pos1, pos2, origin);
		return Create(blueprint);
	}
	
	public static ItemStack Create(Blueprint blueprint) {
		return Create(new TemplateBlueprint(blueprint));
	}
	
	public static ItemStack Create(TemplateBlueprint blueprint) {
		ItemStack stack = new ItemStack(FairyItems.templateScroll);
		SetTemplate(stack, blueprint);
		stack.setDisplayName(new StringTextComponent("New Template"));
		return stack;
	}
	
	protected static void SetTemplate(ItemStack stack, TemplateBlueprint blueprint) {
		SetTemplate(stack, blueprint.toNBT());
	}
	
	protected static void SetTemplate(ItemStack stack, CompoundNBT blueprintTag) {
		if (stack.isEmpty() || !(stack.getItem() instanceof TemplateScroll)) {
			return;
		}
		
		CompoundNBT nbt = stack.getTag();
		if (nbt == null) {
			nbt = new CompoundNBT();
		}
		
		nbt.put(NBT_TEMPLATE, blueprintTag);
		stack.setTag(nbt);
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
	public ActionResultType onItemUse(ItemUseContext context) {
		return ActionResultType.PASS;
	}
	
	@Override
	public ActionResultType itemInteractionForEntity(ItemStack stack, PlayerEntity playerIn, LivingEntity target, Hand hand) {
		return super.itemInteractionForEntity(stack, playerIn, target, hand);
	}
	
	@Override
	public void addInformation(ItemStack stack, @Nullable World worldIn, List<ITextComponent> tooltip, ITooltipFlag flagIn) {
		super.addInformation(stack, worldIn, tooltip, flagIn);
	}
	
}
