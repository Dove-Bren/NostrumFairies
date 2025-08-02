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
import com.smanzana.nostrummagica.client.particles.NostrumParticles;
import com.smanzana.nostrummagica.client.particles.NostrumParticles.SpawnParams;
import com.smanzana.nostrummagica.client.particles.ParticleTargetBehavior.TargetBehavior;
import com.smanzana.nostrummagica.entity.tasks.AttackRangedGoal;
import com.smanzana.nostrummagica.entity.tasks.SpellAttackGoal;
import com.smanzana.nostrummagica.loretag.ELoreCategory;
import com.smanzana.nostrummagica.loretag.IEntityLoreTagged;
import com.smanzana.nostrummagica.loretag.Lore;
import com.smanzana.nostrummagica.spell.EAlteration;
import com.smanzana.nostrummagica.spell.EMagicElement;
import com.smanzana.nostrummagica.spell.Spell;
import com.smanzana.nostrummagica.spell.component.SpellEffectPart;
import com.smanzana.nostrummagica.spell.component.SpellShapePart;
import com.smanzana.nostrummagica.spell.component.shapes.NostrumSpellShapes;
import com.smanzana.nostrummagica.util.TargetLocation;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundAnimatePacket;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.AvoidEntityGoal;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.monster.RangedAttackMob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.ServerLevelAccessor;

public class EntityShadowFey extends Monster implements RangedAttackMob {
	
	public static final String ID = "shadow_fey";
	
	protected static final EntityDataAccessor<BattleStanceShadowFey> STANCE  = SynchedEntityData.<BattleStanceShadowFey>defineId(EntityShadowFey.class, BattleStanceShadowFey.instance());
	protected static final EntityDataAccessor<Boolean> MORPHING = SynchedEntityData.<Boolean>defineId(EntityShadowFey.class, EntityDataSerializers.BOOLEAN);

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
	
	public EntityShadowFey(EntityType<? extends EntityShadowFey> type, Level world) {
		super(type, world);
		this.xpReward = 9;
		
		idleChatTicks = -1;
		
		initSpells();
	}

	// TODO would be cool to make them take arrows, or use arrows to do more damage or something.

	// only available on server
	protected boolean shouldUseBow() {
		LivingEntity target = this.getTarget();
		if (target != null && this.distanceToSqr(target) < 9) {
			return false;
		}
		
		return true;
	}
	
	public void setBattleStance(BattleStanceShadowFey stance) {
		this.entityData.set(STANCE, stance);
	}
	
	public BattleStanceShadowFey getStance() {
		return this.entityData.get(STANCE);
	}
	
	public void setMorphing(boolean morphing) {
		entityData.set(MORPHING, morphing);
	}
	
	public boolean getMorphing() {
		return entityData.get(MORPHING);
	}

	@Override
	protected void registerGoals() {
		int priority = 1;

		this.goalSelector.addGoal(priority++, new FloatGoal(this));
		//EntityCreature theEntityIn, Class<T> classToAvoidIn, Predicate <? super T > avoidTargetSelectorIn, float avoidDistanceIn, double farSpeedIn, double nearSpeedIn
		this.goalSelector.addGoal(priority++, new AvoidEntityGoal<Player>(this, Player.class, 5, 1, 1.2, (player) -> {
			return isDangerItem(player.getMainHandItem()) || isDangerItem(player.getOffhandItem());
		}));
		this.goalSelector.addGoal(priority++, new AttackRangedGoal<EntityShadowFey>(this, 1.0, 0, 25) { // All delay in animation
			@Override
			public boolean hasWeaponEquipped(EntityShadowFey elf) {
				return shouldUseBow() && !elf.getMorphing();
			}
			
			@Override
			protected boolean isAttackAnimationComplete(EntityShadowFey elf) {
				return !elf.swinging;
			}
			
			@Override
			protected void startAttackAnimation(EntityShadowFey elf) {
				elf.swing(InteractionHand.OFF_HAND);
			}
		});
		
		this.goalSelector.addGoal(priority++, new AttackRangedGoal<EntityShadowFey>(this, 0.75, 20, 1) {
			@Override
			public boolean hasWeaponEquipped(EntityShadowFey elf) {
				return !shouldUseBow() && !elf.getMorphing();
			}
			
			@Override
			protected boolean isAttackAnimationComplete(EntityShadowFey elf) {
				return elf.attackAnim >= .7f || !elf.swinging;// slash is .7 through animation
			}
			
			@Override
			protected void startAttackAnimation(EntityShadowFey elf) {
				elf.swing(InteractionHand.MAIN_HAND);
			}
			
			@Override
			protected void resetAttackAnimation(EntityShadowFey elf) {
				elf.swinging = false;
				elf.swingTime = 0;
			}
		});
		
		this.goalSelector.addGoal(priority++, new SpellAttackGoal<EntityShadowFey>(this, 60, 10, true, (elf) -> {
			return elf.getTarget() != null && !elf.getMorphing();
		}, new Spell[]{SPELL_SLOW}));
		this.goalSelector.addGoal(priority++, new WaterAvoidingRandomStrollGoal(this, 1.0D));
		this.goalSelector.addGoal(priority++, new LookAtPlayerGoal(this, Player.class, 8.0F));
		this.goalSelector.addGoal(priority++, new RandomLookAroundGoal(this));
		
		priority = 1;
		this.targetSelector.addGoal(priority++, new HurtByTargetGoal(this).setAlertOthers(EntityShadowFey.class));
		this.targetSelector.addGoal(priority++, new NearestAttackableTargetGoal<EntityFeyBase>(this, EntityFeyBase.class, 5, true, false, null));
		this.targetSelector.addGoal(priority++, new NearestAttackableTargetGoal<Player>(this, Player.class, 10, true, false, (player) -> {
			return !player.isShiftKeyDown();
		}));
		this.targetSelector.addGoal(priority++, new NearestAttackableTargetGoal<Mob>(this, Mob.class, 5, true, false, (living) -> {
			return living != null && living.isAlive() && !(living instanceof Enemy);
		}));
	}

	public static final AttributeSupplier.Builder BuildAttributes() {
		return Monster.createMonsterAttributes()
				.add(Attributes.MOVEMENT_SPEED, .24)
				.add(Attributes.MAX_HEALTH, 14.0)
				.add(Attributes.ATTACK_DAMAGE, 4.0)
				.add(Attributes.ARMOR, 2.0)
				.add(Attributes.FOLLOW_RANGE, 20.0)
				.add(NostrumAttributes.magicResist, 0)
			;
	}
	
	@Override
	protected void defineSynchedData() {
		super.defineSynchedData();
		entityData.define(STANCE, BattleStanceShadowFey.IDLE);
		entityData.define(MORPHING, false);
	}
	
	@Override
	public void baseTick() {
		super.baseTick();
		
		if (this.getTarget() != null && !this.getTarget().isAlive()) {
			this.setTarget(null);
		}
		
		if (!level.isClientSide) {
			if (this.getTarget() == null || this.getMorphing()) {
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
			if (level.isClientSide) {
				if (idleChatTicks == 0) {
					NostrumFairiesSounds.SHADOW_FEY_IDLE.play(NostrumFairies.proxy.getPlayer(), level, getX(), getY(), getZ());
					idleChatTicks = -1;
				}
				
				if (idleChatTicks == -1) {
					idleChatTicks = (random.nextInt(10) + 5) * 20; 
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
	
	protected static final Predicate<Entity> SHADOW_FEY_ARROW_FILTER = EntitySelector.NO_SPECTATORS.and(EntitySelector.ENTITY_STILL_ALIVE).and(new Predicate<Entity>() {
		public boolean test(@Nullable Entity ent) {
			return ent.isPickable() && !(ent instanceof EntityShadowFey);
		}
	});
	
	protected void shootArrowAt(LivingEntity target, float distanceFactor) {
		EntityArrowEx entitytippedarrow = new EntityArrowEx(this.level, this);
		entitytippedarrow.setFilter(SHADOW_FEY_ARROW_FILTER);
		double d0 = target.getX() - this.getX();
		double d1 = target.getBoundingBox().minY + (double)(target.getBbHeight() / 3.0F) - entitytippedarrow.getY();
		double d2 = target.getZ() - this.getZ();
		double d3 = Math.sqrt(d0 * d0 + d2 * d2);
		entitytippedarrow.shoot(d0, d1 + d3 * 0.20000000298023224D, d2, 1.6F, .5f);
		int i = EnchantmentHelper.getEnchantmentLevel(Enchantments.POWER_ARROWS, this);
		int j = EnchantmentHelper.getEnchantmentLevel(Enchantments.PUNCH_ARROWS, this);
		entitytippedarrow.setBaseDamage((double)(distanceFactor * 2.0F) + this.random.nextGaussian() * 0.25D + 3);

		if (i > 0)
		{
			entitytippedarrow.setBaseDamage(entitytippedarrow.getBaseDamage() + (double)i * 0.5D + 0.5D);
		}

		if (j > 0)
		{
			entitytippedarrow.setKnockback(j);
		}

		boolean flag = this.isOnFire() && this.random.nextBoolean();
		flag = flag || EnchantmentHelper.getEnchantmentLevel(Enchantments.FLAMING_ARROWS, this) > 0;

		if (flag)
		{
			entitytippedarrow.setSecondsOnFire(100);
		}

		ItemStack itemstack = this.getItemInHand(InteractionHand.OFF_HAND);

		if (!itemstack.isEmpty() && itemstack.getItem() == Items.TIPPED_ARROW)
		{
			entitytippedarrow.setEffectsFromItem(itemstack);
		}
		else if (random.nextBoolean() && random.nextBoolean() && random.nextBoolean())
		{
			entitytippedarrow.addEffect(new MobEffectInstance(MobEffects.POISON, 600));
		}

		//this.playSound(SoundEvents.ENTITY_SKELETON_SHOOT, 1.0F, 1.0F / (this.getRNG().nextFloat() * 0.4F + 0.8F));
		this.level.addFreshEntity(entitytippedarrow);
	}
	
	protected void slashAt(LivingEntity target, float distanceFactor) {
		if (this.doHurtTarget(target)) {
			this.heal(1f);
			NostrumParticles.FILLED_ORB.spawn(level, new SpawnParams(
					10, target.getX(), target.getY() + target.getBbHeight()/2f, target.getZ(), .1, 20, 10,
					new TargetLocation(this, true)
				).color(0xFFDD2200).setTargetBehavior(TargetBehavior.JOIN));
		}
	}

	@Override
	public void performRangedAttack(LivingEntity target, float distanceFactor) {
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
		return this.hasEffect(MobEffects.DIG_SPEED)
				? getDefaultSwingAnimationDuration() - (1 + this.getEffect(MobEffects.DIG_SPEED).getAmplifier())
				: (this.hasEffect(MobEffects.DIG_SLOWDOWN)
						? getDefaultSwingAnimationDuration() + (1 + this.getEffect(MobEffects.DIG_SLOWDOWN).getAmplifier()) * 2
						: getDefaultSwingAnimationDuration());
	}

	@Override
	public void swing(InteractionHand hand) {
		int unused; //Why not just call super?
		ItemStack stack = this.getItemInHand(hand);
		if (!stack.isEmpty()) {
			if (stack.getItem().onEntitySwing(stack, this)) {
				return;
			}
		}
		
		if (!this.swinging || this.swingTime >= this.getArmSwingAnimationEnd() / 2 || this.swingTime < 0) {
			this.swingTime = -1;
			this.swinging = true;
			this.swingingArm = hand;

			if (this.level instanceof ServerLevel) {
				((ServerLevel)this.level).getChunkSource().broadcast(this, new ClientboundAnimatePacket(this, hand == InteractionHand.MAIN_HAND ? 0 : 3));
			}
		}
	}
	
	@Override
	protected void updateSwingTime() {
		int i = this.getArmSwingAnimationEnd();

		if (this.swinging) {
			++this.swingTime;
			
			if (this.swingTime >= i) {
				this.swingTime = 0;
				this.swinging = false;
			}
		}else {
			this.swingTime = 0;
		}

		this.attackAnim = (float)this.swingTime / (float)i;
	}
	
	protected void transform() {
		final EntityFeyBase fey;
		switch (random.nextInt(5)) {
		case 0:
			fey = new EntityFairy(FairyEntities.Fairy, level);
			break;
		case 1:
			fey = new EntityDwarf(FairyEntities.Dwarf, level);
			break;
		case 2:
			fey = new EntityGnome(FairyEntities.Gnome, level);
			break;
		case 3:
			fey = new EntityElf(FairyEntities.Elf, level);
			break;
		case 4:
		default:
			fey = new EntityElfArcher(FairyEntities.ElfArcher, level);
			break;
		}
		fey.copyPosition(this);
		fey.finalizeSpawn((ServerLevelAccessor) level, level.getCurrentDifficultyAt(fey.blockPosition()), MobSpawnType.CONVERSION, (SpawnGroupData)null, null);
		
		this.discard();
		level.addFreshEntity(fey);
		fey.setCursed(true);
		
		this.level.levelEvent((Player)null, 1027, new BlockPos((int)this.getX(), (int)this.getY(), (int)this.getZ()), 0);
		
		for (Player player : ((ServerLevel) level).players()) {
			if (player.distanceToSqr(this) < 36) {
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
		
		this.updateSwingTime();
		
		// I'll just go ahead and loop this. Can't be that big.
		if (!level.isClientSide) {
			boolean morphing = false;
			for (Player player : ((ServerLevel) level).players()) {
				if ((isDangerItem(player.getMainHandItem()) || isDangerItem(player.getOffhandItem()))
						&& player.distanceToSqr(this) < 36) {
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
	public SpawnGroupData finalizeSpawn(ServerLevelAccessor world, DifficultyInstance difficulty, MobSpawnType reason, @Nullable SpawnGroupData livingdata, @Nullable CompoundTag tag) {
		livingdata = super.finalizeSpawn(world, difficulty, reason, livingdata, tag);
		
		this.setLeftHanded(this.random.nextBoolean());
		
		return livingdata;
	}
	
	@Override
	public boolean checkSpawnRules(LevelAccessor worldIn, MobSpawnType spawnReasonIn) {
		// Shadow fey amass ar mies in the twilight forest. So let's make sure to only spawn them when other things can spawn.
		if ((spawnReasonIn == MobSpawnType.NATURAL || spawnReasonIn == MobSpawnType.CHUNK_GENERATION)
				&& this.level.getBiome(this.blockPosition()).value().getMobSettings().getMobs(MobCategory.MONSTER).unwrap().size() <= 2) {
			return false;
		}
		
		return super.checkSpawnRules(worldIn, spawnReasonIn);
	}
	
	@Override
	protected SoundEvent getHurtSound(DamageSource damageSourceIn) {
		return NostrumFairiesSounds.SHADOW_FEY_HURT.getEvent();
	}
	
	@Override
	protected SoundEvent getDeathSound() {
		return NostrumFairiesSounds.SHADOW_FEY_HURT.getEvent();
	}
	
	public static final class ShadowFeyConversionLore implements IEntityLoreTagged<EntityShadowFey> {
		
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
		public ELoreCategory getCategory() {
			return ELoreCategory.ENTITY;
		}

		@Override
		public EntityType<? extends EntityShadowFey> getEntityType() {
			return FairyEntities.ShadowFey;
		}
	}
}
