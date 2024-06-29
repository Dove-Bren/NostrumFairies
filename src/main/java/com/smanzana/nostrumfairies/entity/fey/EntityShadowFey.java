package com.smanzana.nostrumfairies.entity.fey;

import java.util.function.Predicate;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.smanzana.nostrumfairies.NostrumFairies;
import com.smanzana.nostrumfairies.entity.EntityArrowEx;
import com.smanzana.nostrumfairies.entity.FairyEntities;
import com.smanzana.nostrumfairies.items.FairyItems;
import com.smanzana.nostrumfairies.serializers.BattleStanceShadowFey;
import com.smanzana.nostrumfairies.sound.NostrumFairiesSounds;
import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.attribute.NostrumAttributes;
import com.smanzana.nostrummagica.capabilities.INostrumMagic;
import com.smanzana.nostrummagica.client.gui.infoscreen.InfoScreenTabs;
import com.smanzana.nostrummagica.entity.tasks.AttackRangedGoal;
import com.smanzana.nostrummagica.entity.tasks.SpellAttackGoal;
import com.smanzana.nostrummagica.loretag.ILoreTagged;
import com.smanzana.nostrummagica.loretag.Lore;
import com.smanzana.nostrummagica.spell.EAlteration;
import com.smanzana.nostrummagica.spell.EMagicElement;
import com.smanzana.nostrummagica.spell.Spell;
import com.smanzana.nostrummagica.spell.component.SpellEffectPart;
import com.smanzana.nostrummagica.spell.component.SpellShapePart;
import com.smanzana.nostrummagica.spell.component.shapes.NostrumSpellShapes;

import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityClassification;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ILivingEntityData;
import net.minecraft.entity.IRangedAttackMob;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.ai.attributes.AttributeModifierMap;
import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraft.entity.ai.goal.AvoidEntityGoal;
import net.minecraft.entity.ai.goal.HurtByTargetGoal;
import net.minecraft.entity.ai.goal.LookAtGoal;
import net.minecraft.entity.ai.goal.LookRandomlyGoal;
import net.minecraft.entity.ai.goal.NearestAttackableTargetGoal;
import net.minecraft.entity.ai.goal.SwimGoal;
import net.minecraft.entity.ai.goal.WaterAvoidingRandomWalkingGoal;
import net.minecraft.entity.monster.IMob;
import net.minecraft.entity.monster.MonsterEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.network.play.server.SAnimateHandPacket;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.Effects;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EntityPredicates;
import net.minecraft.util.Hand;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.IServerWorld;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;

public class EntityShadowFey extends MonsterEntity implements IRangedAttackMob {
	
	public static final String ID = "shadow_fey";
	
	protected static final DataParameter<BattleStanceShadowFey> STANCE  = EntityDataManager.<BattleStanceShadowFey>createKey(EntityShadowFey.class, BattleStanceShadowFey.instance());
	protected static final DataParameter<Boolean> MORPHING = EntityDataManager.<Boolean>createKey(EntityShadowFey.class, DataSerializers.BOOLEAN);

	private static Spell SPELL_SLOW = null;
	
	private static void initSpells() {
		if (SPELL_SLOW == null) {
			SPELL_SLOW = Spell.CreateAISpell("Shadow Binds");
			SPELL_SLOW.addPart(new SpellShapePart(NostrumSpellShapes.SeekingBullet));
			SPELL_SLOW.addPart(new SpellEffectPart(EMagicElement.LIGHTNING, 1, EAlteration.INFLICT));
		}
	}
	
	protected static boolean isDangerItem(@Nonnull ItemStack stack) {
		if (stack.isEmpty()) {
			return false;
		}
		
		if (stack.getItem() == FairyItems.feyBell) {
			return true;
		}
		
		return false;
	}

	protected int idleTicks;
	protected int idleChatTicks;
	protected int morphTicks;
	
	public EntityShadowFey(EntityType<? extends EntityShadowFey> type, World world) {
		super(type, world);
		this.experienceValue = 9;
		
		idleChatTicks = -1;
		
		initSpells();
	}

	// TODO would be cool to make them take arrows, or use arrows to do more damage or something.

	// only available on server
	protected boolean shouldUseBow() {
		LivingEntity target = this.getAttackTarget();
		if (target != null && this.getDistanceSq(target) < 9) {
			return false;
		}
		
		return true;
	}
	
	public void setBattleStance(BattleStanceShadowFey stance) {
		this.dataManager.set(STANCE, stance);
	}
	
	public BattleStanceShadowFey getStance() {
		return this.dataManager.get(STANCE);
	}
	
	public void setMorphing(boolean morphing) {
		dataManager.set(MORPHING, morphing);
	}
	
	public boolean getMorphing() {
		return dataManager.get(MORPHING);
	}

	@Override
	protected void registerGoals() {
		int priority = 1;

		this.goalSelector.addGoal(priority++, new SwimGoal(this));
		//EntityCreature theEntityIn, Class<T> classToAvoidIn, Predicate <? super T > avoidTargetSelectorIn, float avoidDistanceIn, double farSpeedIn, double nearSpeedIn
		this.goalSelector.addGoal(priority++, new AvoidEntityGoal<PlayerEntity>(this, PlayerEntity.class, 5, 1, 1.2, (player) -> {
			return isDangerItem(player.getHeldItemMainhand()) || isDangerItem(player.getHeldItemOffhand());
		}));
		this.goalSelector.addGoal(priority++, new AttackRangedGoal<EntityShadowFey>(this, 1.0, 0, 25) { // All delay in animation
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
				elf.swingArm(Hand.OFF_HAND);
			}
		});
		
		this.goalSelector.addGoal(priority++, new AttackRangedGoal<EntityShadowFey>(this, 0.75, 10, 3) {
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
				elf.swingArm(Hand.MAIN_HAND);
			}
		});
		
		this.goalSelector.addGoal(priority++, new SpellAttackGoal<EntityShadowFey>(this, 60, 10, true, (elf) -> {
			return elf.getAttackTarget() != null && !elf.getMorphing();
		}, new Spell[]{SPELL_SLOW}));
		this.goalSelector.addGoal(priority++, new WaterAvoidingRandomWalkingGoal(this, 1.0D));
		this.goalSelector.addGoal(priority++, new LookAtGoal(this, PlayerEntity.class, 8.0F));
		this.goalSelector.addGoal(priority++, new LookRandomlyGoal(this));
		
		priority = 1;
		this.targetSelector.addGoal(priority++, new HurtByTargetGoal(this).setCallsForHelp(EntityShadowFey.class));
		this.targetSelector.addGoal(priority++, new NearestAttackableTargetGoal<EntityFeyBase>(this, EntityFeyBase.class, 5, true, false, null));
		this.targetSelector.addGoal(priority++, new NearestAttackableTargetGoal<PlayerEntity>(this, PlayerEntity.class, 10, true, false, (player) -> {
			return !player.isSneaking();
		}));
		this.targetSelector.addGoal(priority++, new NearestAttackableTargetGoal<MobEntity>(this, MobEntity.class, 5, true, false, (living) -> {
			return living != null && living.isAlive() && !(living instanceof IMob);
		}));
	}

	public static final AttributeModifierMap.MutableAttribute BuildAttributes() {
		return MonsterEntity.func_234295_eP_()
				.createMutableAttribute(Attributes.MOVEMENT_SPEED, .24)
				.createMutableAttribute(Attributes.MAX_HEALTH, 14.0)
				.createMutableAttribute(Attributes.ATTACK_DAMAGE, 4.0)
				.createMutableAttribute(Attributes.ARMOR, 2.0)
				.createMutableAttribute(Attributes.FOLLOW_RANGE, 20.0)
				.createMutableAttribute(NostrumAttributes.magicResist, 0)
			;
	}
	
	@Override
	protected void registerData() {
		super.registerData();
		dataManager.register(STANCE, BattleStanceShadowFey.IDLE);
		dataManager.register(MORPHING, false);
	}
	
	@Override
	public void baseTick() {
		super.baseTick();
		
		if (this.getAttackTarget() != null && !this.getAttackTarget().isAlive()) {
			this.setAttackTarget(null);
		}
		
		if (!world.isRemote) {
			if (this.getAttackTarget() == null || this.getMorphing()) {
				setBattleStance(BattleStanceShadowFey.IDLE);
			} else {
				if (this.shouldUseBow()) {
					setBattleStance(BattleStanceShadowFey.RANGED);
				} else {
					setBattleStance(BattleStanceShadowFey.MELEE);
				}
			}
		}
		
		if (this.getStance() == BattleStanceShadowFey.IDLE) {
			this.idleTicks++;
			if (world.isRemote) {
				if (idleChatTicks == 0) {
					NostrumFairiesSounds.SHADOW_FEY_IDLE.play(NostrumFairies.proxy.getPlayer(), world, getPosX(), getPosY(), getPosZ());
					idleChatTicks = -1;
				}
				
				if (idleChatTicks == -1) {
					idleChatTicks = (rand.nextInt(10) + 5) * 20; 
				}
				
				idleChatTicks--;
			}
		} else {
			this.idleTicks = 0;
			idleChatTicks = -1;
		}
	}
	
	protected int getDefaultSwingAnimationDuration() {
		if (this.shouldUseBow()) {
			return 60;
		} else {
			return 10;
		}
		
	}
	
	protected static final Predicate<Entity> SHADOW_FEY_ARROW_FILTER = EntityPredicates.NOT_SPECTATING.and(EntityPredicates.IS_ALIVE).and(new Predicate<Entity>() {
		public boolean test(@Nullable Entity ent) {
			return ent.canBeCollidedWith() && !(ent instanceof EntityShadowFey);
		}
	});
	
	protected void shootArrowAt(LivingEntity target, float distanceFactor) {
		EntityArrowEx entitytippedarrow = new EntityArrowEx(this.world, this);
		entitytippedarrow.setFilter(SHADOW_FEY_ARROW_FILTER);
		double d0 = target.getPosX() - this.getPosX();
		double d1 = target.getBoundingBox().minY + (double)(target.getHeight() / 3.0F) - entitytippedarrow.getPosY();
		double d2 = target.getPosZ() - this.getPosZ();
		double d3 = (double)MathHelper.sqrt(d0 * d0 + d2 * d2);
		entitytippedarrow.shoot(d0, d1 + d3 * 0.20000000298023224D, d2, 1.6F, .5f);
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

		ItemStack itemstack = this.getHeldItem(Hand.OFF_HAND);

		if (!itemstack.isEmpty() && itemstack.getItem() == Items.TIPPED_ARROW)
		{
			entitytippedarrow.setPotionEffect(itemstack);
		}
		else if (rand.nextBoolean() && rand.nextBoolean() && rand.nextBoolean())
		{
			entitytippedarrow.addEffect(new EffectInstance(Effects.POISON, 600));
		}

		//this.playSound(SoundEvents.ENTITY_SKELETON_SHOOT, 1.0F, 1.0F / (this.getRNG().nextFloat() * 0.4F + 0.8F));
		this.world.addEntity(entitytippedarrow);
	}
	
	protected void slashAt(LivingEntity target, float distanceFactor) {
		if (this.attackEntityAsMob(target)) {
			this.heal(1f);
		}
	}

	@Override
	public void attackEntityWithRangedAttack(LivingEntity target, float distanceFactor) {
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
		return this.isPotionActive(Effects.HASTE)
				? getDefaultSwingAnimationDuration() - (1 + this.getActivePotionEffect(Effects.HASTE).getAmplifier())
				: (this.isPotionActive(Effects.MINING_FATIGUE)
						? getDefaultSwingAnimationDuration() + (1 + this.getActivePotionEffect(Effects.MINING_FATIGUE).getAmplifier()) * 2
						: getDefaultSwingAnimationDuration());
	}

	@Override
	public void swingArm(Hand hand) {
		int unused; //Why not just call super?
		ItemStack stack = this.getHeldItem(hand);
		if (!stack.isEmpty()) {
			if (stack.getItem().onEntitySwing(stack, this)) {
				return;
			}
		}
		
		if (!this.isSwingInProgress || this.swingProgressInt >= this.getArmSwingAnimationEnd() / 2 || this.swingProgressInt < 0) {
			this.swingProgressInt = -1;
			this.isSwingInProgress = true;
			this.swingingHand = hand;

			if (this.world instanceof ServerWorld) {
				((ServerWorld)this.world).getChunkProvider().sendToAllTracking(this, new SAnimateHandPacket(this, hand == Hand.MAIN_HAND ? 0 : 3));
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
			fey = new EntityFairy(FairyEntities.Fairy, world);
			break;
		case 1:
			fey = new EntityDwarf(FairyEntities.Dwarf, world);
			break;
		case 2:
			fey = new EntityGnome(FairyEntities.Gnome, world);
			break;
		case 3:
			fey = new EntityElf(FairyEntities.Elf, world);
			break;
		case 4:
		default:
			fey = new EntityElfArcher(FairyEntities.ElfArcher, world);
			break;
		}
		fey.copyLocationAndAnglesFrom(this);
		fey.onInitialSpawn((IServerWorld) world, world.getDifficultyForLocation(fey.getPosition()), SpawnReason.CONVERSION, (ILivingEntityData)null, null);
		
		this.remove();
		world.addEntity(fey);
		fey.setCursed(true);
		
		this.world.playEvent((PlayerEntity)null, 1027, new BlockPos((int)this.getPosX(), (int)this.getPosY(), (int)this.getPosZ()), 0);
		
		for (PlayerEntity player : ((ServerWorld) world).getPlayers()) {
			if (player.getDistanceSq(this) < 36) {
				INostrumMagic attr = NostrumMagica.getMagicWrapper(player);
				if (attr != null) {
					attr.giveFullLore(ShadowFeyConversionLore.instance());
				}
			}
		}
	}
	
	@Override
	public void tick() {
		super.tick();
		
		this.updateArmSwingProgress();
		
		// I'll just go ahead and loop this. Can't be that big.
		if (!world.isRemote) {
			boolean morphing = false;
			for (PlayerEntity player : ((ServerWorld) world).getPlayers()) {
				if ((isDangerItem(player.getHeldItemMainhand()) || isDangerItem(player.getHeldItemOffhand()))
						&& player.getDistanceSq(this) < 36) {
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
	public ILivingEntityData onInitialSpawn(IServerWorld world, DifficultyInstance difficulty, SpawnReason reason, @Nullable ILivingEntityData livingdata, @Nullable CompoundNBT tag) {
		livingdata = super.onInitialSpawn(world, difficulty, reason, livingdata, tag);
		
		this.setLeftHanded(this.rand.nextBoolean());
		
		return livingdata;
	}
	
	@Override
	public boolean canSpawn(IWorld worldIn, SpawnReason spawnReasonIn) {
		// Shadow fey amass ar mies in the twilight forest. So let's make sure to only spawn them when other things can spawn.
		if ((spawnReasonIn == SpawnReason.NATURAL || spawnReasonIn == SpawnReason.CHUNK_GENERATION)
				&& this.world.getBiome(this.getPosition()).getMobSpawnInfo().getSpawners(EntityClassification.MONSTER).size() <= 2) {
			return false;
		}
		
		return super.canSpawn(worldIn, spawnReasonIn);
	}
	
	@Override
	protected SoundEvent getHurtSound(DamageSource damageSourceIn) {
		return NostrumFairiesSounds.SHADOW_FEY_HURT.getEvent();
	}
	
	@Override
	protected SoundEvent getDeathSound() {
		return NostrumFairiesSounds.SHADOW_FEY_HURT.getEvent();
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
