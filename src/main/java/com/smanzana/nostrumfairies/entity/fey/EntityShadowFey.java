package com.smanzana.nostrumfairies.entity.fey;

import java.io.IOException;

import javax.annotation.Nullable;

import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import com.smanzana.nostrumfairies.entity.EntityTippedArrowEx;
import com.smanzana.nostrumfairies.items.FeyResource;
import com.smanzana.nostrumfairies.items.FeyResource.FeyResourceType;
import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.attributes.AttributeMagicResist;
import com.smanzana.nostrummagica.capabilities.INostrumMagic;
import com.smanzana.nostrummagica.client.gui.infoscreen.InfoScreenTabs;
import com.smanzana.nostrummagica.entity.tasks.EntityAIAttackRanged;
import com.smanzana.nostrummagica.entity.tasks.EntitySpellAttackTask;
import com.smanzana.nostrummagica.loretag.ILoreTagged;
import com.smanzana.nostrummagica.loretag.Lore;
import com.smanzana.nostrummagica.spells.EAlteration;
import com.smanzana.nostrummagica.spells.EMagicElement;
import com.smanzana.nostrummagica.spells.Spell;
import com.smanzana.nostrummagica.spells.Spell.SpellPart;
import com.smanzana.nostrummagica.spells.components.shapes.SingleShape;
import com.smanzana.nostrummagica.spells.components.triggers.AITargetTrigger;

import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.IEntityLivingData;
import net.minecraft.entity.IRangedAttackMob;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.EntityAIAvoidEntity;
import net.minecraft.entity.ai.EntityAIHurtByTarget;
import net.minecraft.entity.ai.EntityAILookIdle;
import net.minecraft.entity.ai.EntityAINearestAttackableTarget;
import net.minecraft.entity.ai.EntityAISwimming;
import net.minecraft.entity.ai.EntityAIWander;
import net.minecraft.entity.ai.EntityAIWatchClosest;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.monster.IMob;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Enchantments;
import net.minecraft.init.Items;
import net.minecraft.init.MobEffects;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializer;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.network.play.server.SPacketAnimation;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.EntitySelectors;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;

public class EntityShadowFey extends EntityMob implements IRangedAttackMob {
	
	public static enum BattleStance {
		RANGED,
		MELEE,
		IDLE;
		
		public final static class BattleStanceSerializer implements DataSerializer<BattleStance> {
			
			private BattleStanceSerializer() {
				DataSerializers.registerSerializer(this);
			}
			
			@Override
			public void write(PacketBuffer buf, BattleStance value) {
				buf.writeEnumValue(value);
			}

			@Override
			public BattleStance read(PacketBuffer buf) throws IOException {
				return buf.readEnumValue(BattleStance.class);
			}

			@Override
			public DataParameter<BattleStance> createKey(int id) {
				return new DataParameter<>(id, this);
			}
		}
		
		public static final BattleStanceSerializer Serializer = new BattleStanceSerializer();
	}
	
	protected static final DataParameter<BattleStance> STANCE  = EntityDataManager.<BattleStance>createKey(EntityShadowFey.class, BattleStance.Serializer);
	protected static final DataParameter<Boolean> MORPHING = EntityDataManager.<Boolean>createKey(EntityShadowFey.class, DataSerializers.BOOLEAN);

	private static Spell SPELL_SLOW = null;
	
	private static void initSpells() {
		if (SPELL_SLOW == null) {
			SPELL_SLOW = new Spell("Shadow Binds");
			SPELL_SLOW.addPart(new SpellPart(AITargetTrigger.instance()));
			SPELL_SLOW.addPart(new SpellPart(SingleShape.instance(), EMagicElement.LIGHTNING, 2, EAlteration.INFLICT));
		}
	}
	
	protected static boolean isDangerItem(@Nullable ItemStack stack) {
		if (stack == null) {
			return false;
		}
		
		if (stack.getItem() instanceof FeyResource && FeyResource.instance().getType(stack) == FeyResourceType.BELL) {
			return true;
		}
		
		return false;
	}

	protected int idleTicks;
	protected int morphTicks;
	
	public EntityShadowFey(World world) {
		super(world);
		this.experienceValue = 9;
		this.height = 0.75f;
		
		initSpells();
	}

	// TODO would be cool to make them take arrows, or use arrows to do more damage or something.

	// only available on server
	protected boolean shouldUseBow() {
		EntityLivingBase target = this.getAttackTarget();
		if (target != null && this.getDistanceSqToEntity(target) < 9) {
			return false;
		}
		
		return true;
	}
	
	public void setBattleStance(BattleStance stance) {
		this.dataManager.set(STANCE, stance);
	}
	
	public BattleStance getStance() {
		return this.dataManager.get(STANCE);
	}
	
	public void setMorphing(boolean morphing) {
		dataManager.set(MORPHING, morphing);
	}
	
	public boolean getMorphing() {
		return dataManager.get(MORPHING);
	}

	@Override
	protected void initEntityAI() {
		int priority = 1;

		this.tasks.addTask(priority++, new EntityAISwimming(this));
		//EntityCreature theEntityIn, Class<T> classToAvoidIn, Predicate <? super T > avoidTargetSelectorIn, float avoidDistanceIn, double farSpeedIn, double nearSpeedIn
		this.tasks.addTask(priority++, new EntityAIAvoidEntity<EntityPlayer>(this, EntityPlayer.class, (player) -> {
				return isDangerItem(player.getHeldItemMainhand()) || isDangerItem(player.getHeldItemOffhand());
			}, 5, 1, 1.2));
		this.tasks.addTask(priority++, new EntityAIAttackRanged<EntityShadowFey>(this, 1.0, 0, 25) { // All delay in animation
			@Override
			public boolean hasWeaponEquipped(EntityShadowFey elf) {
				return shouldUseBow() && !elf.getMorphing();
			}
			
			@Override
			protected boolean isAttackAnimationComplete(EntityShadowFey elf) {
				return !elf.isSwingInProgress;
			}
			
			@Override
			protected void startAttackAnimation(EntityShadowFey elf) {
				elf.swingArm(EnumHand.OFF_HAND);
			}
		});
		
		this.tasks.addTask(priority++, new EntityAIAttackRanged<EntityShadowFey>(this, 0.75, 10, 3) {
			@Override
			public boolean hasWeaponEquipped(EntityShadowFey elf) {
				return !shouldUseBow() && !elf.getMorphing();
			}
			
			@Override
			protected boolean isAttackAnimationComplete(EntityShadowFey elf) {
				return elf.swingProgress >= .7;// slash is .7 through animation
			}
			
			@Override
			protected void startAttackAnimation(EntityShadowFey elf) {
				elf.swingArm(EnumHand.MAIN_HAND);
			}
		});
		
		this.tasks.addTask(priority++, new EntitySpellAttackTask<EntityShadowFey>(this, 60, 10, true, (elf) -> {
			return elf.getAttackTarget() != null && !elf.getMorphing();
		}, new Spell[]{SPELL_SLOW}));
		this.tasks.addTask(priority++, new EntityAIWander(this, 1.0D));
		this.tasks.addTask(priority++, new EntityAIWatchClosest(this, EntityPlayer.class, 8.0F));
		this.tasks.addTask(priority++, new EntityAILookIdle(this));
		
		priority = 1;
		this.targetTasks.addTask(priority++, new EntityAIHurtByTarget(this, true, new Class[0]));
		this.targetTasks.addTask(priority++, new EntityAINearestAttackableTarget<EntityFeyBase>(this, EntityFeyBase.class, 5, true, false, (Predicate <EntityFeyBase >)null));
		this.targetTasks.addTask(priority++, new EntityAINearestAttackableTarget<EntityPlayer>(this, EntityPlayer.class, 5, true, false, (Predicate <EntityPlayer >)null));
		this.targetTasks.addTask(priority++, new EntityAINearestAttackableTarget<EntityLiving>(this, EntityLiving.class, 5, true, false, (living) -> {
			return living != null && !living.isDead && !(living instanceof IMob);
		}));
	}

	@Override
	protected void applyEntityAttributes() {
		super.applyEntityAttributes();
		this.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(0.24D);
		this.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(16.0D);
		this.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).setBaseValue(4.0D);
		this.getEntityAttribute(SharedMonsterAttributes.ARMOR).setBaseValue(4.0D);
		this.getEntityAttribute(SharedMonsterAttributes.FOLLOW_RANGE).setBaseValue(20.0);
		this.getEntityAttribute(AttributeMagicResist.instance()).setBaseValue(40.0D);
	}
	
	@Override
	protected void entityInit() {
		super.entityInit();
		dataManager.register(STANCE, BattleStance.IDLE);
		dataManager.register(MORPHING, false);
	}
	
	@Override
	public void onEntityUpdate() {
		super.onEntityUpdate();
		
		if (this.getAttackTarget() != null && this.getAttackTarget().isDead) {
			this.setAttackTarget(null);
		}
		
		if (!worldObj.isRemote) {
			if (this.getAttackTarget() == null || this.getMorphing()) {
				setBattleStance(BattleStance.IDLE);
			} else {
				if (this.shouldUseBow()) {
					setBattleStance(BattleStance.RANGED);
				} else {
					setBattleStance(BattleStance.MELEE);
				}
			}
		}
	}
	
	protected int getDefaultSwingAnimationDuration() {
		if (this.shouldUseBow()) {
			return 40;
		} else {
			return 10;
		}
		
	}
	
	@SuppressWarnings("unchecked")
	protected static final Predicate<Entity> SHADOW_FEY_ARROW_FILTER = Predicates.and(EntitySelectors.NOT_SPECTATING, EntitySelectors.IS_ALIVE, new Predicate<Entity>() {
		public boolean apply(@Nullable Entity ent) {
			return ent.canBeCollidedWith() && !(ent instanceof EntityShadowFey);
		}
	});
	
	protected void shootArrowAt(EntityLivingBase target, float distanceFactor) {
		EntityTippedArrowEx entitytippedarrow = new EntityTippedArrowEx(this.worldObj, this);
		entitytippedarrow.setFilter(SHADOW_FEY_ARROW_FILTER);
		double d0 = target.posX - this.posX;
		double d1 = target.getEntityBoundingBox().minY + (double)(target.height / 3.0F) - entitytippedarrow.posY;
		double d2 = target.posZ - this.posZ;
		double d3 = (double)MathHelper.sqrt_double(d0 * d0 + d2 * d2);
		entitytippedarrow.setThrowableHeading(d0, d1 + d3 * 0.20000000298023224D, d2, 1.6F, .5f);
		int i = EnchantmentHelper.getMaxEnchantmentLevel(Enchantments.POWER, this);
		int j = EnchantmentHelper.getMaxEnchantmentLevel(Enchantments.PUNCH, this);
		entitytippedarrow.setDamage((double)(distanceFactor * 2.0F) + this.rand.nextGaussian() * 0.25D + 3);

		if (i > 0)
		{
			entitytippedarrow.setDamage(entitytippedarrow.getDamage() + (double)i * 0.5D + 0.5D);
		}

		if (j > 0)
		{
			entitytippedarrow.setKnockbackStrength(j);
		}

		boolean flag = this.isBurning() && this.rand.nextBoolean();
		flag = flag || EnchantmentHelper.getMaxEnchantmentLevel(Enchantments.FLAME, this) > 0;

		if (flag)
		{
			entitytippedarrow.setFire(100);
		}

		ItemStack itemstack = this.getHeldItem(EnumHand.OFF_HAND);

		if (itemstack != null && itemstack.getItem() == Items.TIPPED_ARROW)
		{
			entitytippedarrow.setPotionEffect(itemstack);
		}
		else if (rand.nextBoolean() && rand.nextBoolean() && rand.nextBoolean())
		{
			entitytippedarrow.addEffect(new PotionEffect(MobEffects.POISON, 600));
		}

		//this.playSound(SoundEvents.ENTITY_SKELETON_SHOOT, 1.0F, 1.0F / (this.getRNG().nextFloat() * 0.4F + 0.8F));
		this.worldObj.spawnEntityInWorld(entitytippedarrow);
	}
	
	protected void slashAt(EntityLivingBase target, float distanceFactor) {
		if (this.attackEntityAsMob(target)) {
			this.heal(2f);
		}
	}

	@Override
	public void attackEntityWithRangedAttack(EntityLivingBase target, float distanceFactor) {
		if (this.shouldUseBow()) {
			shootArrowAt(target, distanceFactor);
		} else {
			slashAt(target, distanceFactor);
		}
	}
	
	/**
	 * Returns an integer indicating the end point of the swing animation, used by {@link #swingProgress} to provide a
	 * progress indicator. Takes dig speed enchantments into account.
	 * Note: Copied from vanilla where you can't override it :(
	 */
	protected int getArmSwingAnimationEnd() {
		return this.isPotionActive(MobEffects.HASTE)
				? getDefaultSwingAnimationDuration() - (1 + this.getActivePotionEffect(MobEffects.HASTE).getAmplifier())
				: (this.isPotionActive(MobEffects.MINING_FATIGUE)
						? getDefaultSwingAnimationDuration() + (1 + this.getActivePotionEffect(MobEffects.MINING_FATIGUE).getAmplifier()) * 2
						: getDefaultSwingAnimationDuration());
	}

	@Override
	public void swingArm(EnumHand hand) {
		ItemStack stack = this.getHeldItem(hand);
		if (stack != null && stack.getItem() != null) {
			if (stack.getItem().onEntitySwing(this, stack)) {
				return;
			}
		}
		
		if (!this.isSwingInProgress || this.swingProgressInt >= this.getArmSwingAnimationEnd() / 2 || this.swingProgressInt < 0) {
			this.swingProgressInt = -1;
			this.isSwingInProgress = true;
			this.swingingHand = hand;

			if (this.worldObj instanceof WorldServer) {
				((WorldServer)this.worldObj).getEntityTracker().sendToAllTrackingEntity(this, new SPacketAnimation(this, hand == EnumHand.MAIN_HAND ? 0 : 3));
			}
		}
	}
	
	@Override
	protected void updateArmSwingProgress() {
		int i = this.getArmSwingAnimationEnd();

		if (this.isSwingInProgress) {
			++this.swingProgressInt;

			if (this.swingProgressInt >= i) {
				this.swingProgressInt = 0;
				this.isSwingInProgress = false;
			}
		}else {
			this.swingProgressInt = 0;
		}

		this.swingProgress = (float)this.swingProgressInt / (float)i;
	}
	
	protected void transform() {
		final EntityFeyBase fey;
		switch (rand.nextInt(5)) {
		case 0:
			fey = new EntityFairy(worldObj);
			break;
		case 1:
			fey = new EntityDwarf(worldObj);
			break;
		case 2:
			fey = new EntityGnome(worldObj);
			break;
		case 3:
			fey = new EntityElf(worldObj);
			break;
		case 4:
		default:
			fey = new EntityElfArcher(worldObj);
			break;
		}
		fey.copyLocationAndAnglesFrom(this);
		fey.onInitialSpawn(this.worldObj.getDifficultyForLocation(new BlockPos(fey)), (IEntityLivingData)null);
		
		worldObj.removeEntity(this);
		worldObj.spawnEntityInWorld(fey);
		fey.setCursed(true);
		
		this.worldObj.playEvent((EntityPlayer)null, 1027, new BlockPos((int)this.posX, (int)this.posY, (int)this.posZ), 0);
		
		for (EntityPlayer player : worldObj.playerEntities) {
			if (player.getDistanceSqToEntity(this) < 36) {
				INostrumMagic attr = NostrumMagica.getMagicWrapper(player);
				if (attr != null) {
					attr.giveFullLore(ShadowFeyConversionLore.instance());
				}
			}
		}
	}
	
	@Override
	public void onUpdate() {
		super.onUpdate();
		
		this.updateArmSwingProgress();
		
		// I'll just go ahead and loop this. Can't be that big.
		if (!worldObj.isRemote) {
			boolean morphing = false;
			for (EntityPlayer player : worldObj.playerEntities) {
				if ((isDangerItem(player.getHeldItemMainhand()) || isDangerItem(player.getHeldItemOffhand()))
						&& player.getDistanceSqToEntity(this) < 36) {
					morphing = true;
					break;
				}
			}
			
			if (morphing) {
				if (!this.getMorphing()) {
					this.setMorphing(true);
					// Effects? Maybe only if morphing ticks are 0?
				}
				
				this.morphTicks++;
				if (morphTicks > 20 * 15) {
					transform();
				}
			} else {
				if (this.getMorphing()) {
					this.setMorphing(false);
				}
				if (morphTicks > 0) {
					morphTicks--;
				}
			}
		}
	}
	
	@Override
	public IEntityLivingData onInitialSpawn(DifficultyInstance difficulty, @Nullable IEntityLivingData livingdata) {
		livingdata = super.onInitialSpawn(difficulty, livingdata);
		
		this.setLeftHanded(this.rand.nextBoolean());
		
		return livingdata;
	}
	
	@Override
	protected void dropFewItems(boolean wasRecentlyHit, int lootingModifier) {
		if (wasRecentlyHit && !worldObj.isRemote) {
			int chance = 1 + lootingModifier;
			if (rand.nextInt(100) < chance) {
				
				this.entityDropItem(FeyResource.create(FeyResourceType.ESSENCE_CORRUPTED, rand.nextInt(1 + lootingModifier/2)), 0);
				
			}
		}
	}
	
	public static final class ShadowFeyConversionLore implements ILoreTagged {
		
		private static ShadowFeyConversionLore instance = null;
		public static ShadowFeyConversionLore instance() {
			if (instance == null) {
				instance = new ShadowFeyConversionLore();
			}
			return instance;
		}

		@Override
		public String getLoreKey() {
			return "lore_shadow_fey_conversion";
		}

		@Override
		public String getLoreDisplayName() {
			return "Shadow Fey Conversion";
		}

		@Override
		public Lore getBasicLore() {
			return new Lore().add("The Shadow Fey didn't like the charm you held. That was evident. You never expected them to transform in front of your eyes after extended exposure!", "As you've seen, the shadow fey transform after extended exposure. There seem to be various kinds of fey that they are transformed into, but you haven't had much time to find out more, as the revitalized fey die shortly after the conversion.");
		}

		@Override
		public Lore getDeepLore() {
			return getBasicLore();
		}

		@Override
		public InfoScreenTabs getTab() {
			return InfoScreenTabs.INFO_ENTITY;
		}
	}
}
