package com.smanzana.nostrumfairies.entity.fey;

import java.util.Collection;
import java.util.Iterator;
import java.util.function.Predicate;

import javax.annotation.Nullable;

import com.smanzana.nostrumfairies.entity.EntityArrowEx;
import com.smanzana.nostrumfairies.items.FeyStoneMaterial;
import com.smanzana.nostrumfairies.logistics.ILogisticsComponent;
import com.smanzana.nostrumfairies.logistics.LogisticsNetwork;
import com.smanzana.nostrumfairies.logistics.task.ILogisticsTask;
import com.smanzana.nostrumfairies.serializers.ArmPoseElf;
import com.smanzana.nostrumfairies.serializers.BattleStanceElfArcher;
import com.smanzana.nostrummagica.entity.tasks.AttackRangedGoal;
import com.smanzana.nostrummagica.entity.tasks.SpellAttackGoal;
import com.smanzana.nostrummagica.spell.EAlteration;
import com.smanzana.nostrummagica.spell.EMagicElement;
import com.smanzana.nostrummagica.spell.Spell;
import com.smanzana.nostrummagica.spell.component.SpellEffectPart;
import com.smanzana.nostrummagica.spell.component.SpellShapePart;
import com.smanzana.nostrummagica.spell.component.shapes.NostrumSpellShapes;

import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.InteractionHand;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;

public class EntityElfArcher extends EntityElf {
	
	public static final String ID = "elf_archer";
	
	protected static final EntityDataAccessor<BattleStanceElfArcher> STANCE  = SynchedEntityData.<BattleStanceElfArcher>defineId(EntityElfArcher.class, BattleStanceElfArcher.instance());

	private static Spell SPELL_HASTE = null;
	
	private static void initSpells() {
		if (SPELL_HASTE == null) {
			SPELL_HASTE = Spell.CreateAISpell("Elven Haste");
			SPELL_HASTE.addPart(new SpellShapePart(NostrumSpellShapes.Self));
			SPELL_HASTE.addPart(new SpellEffectPart(EMagicElement.WIND, 1, EAlteration.SUPPORT));
		}
	}

	protected int idleTicks;
	protected BlockPos patrolTarget;
	protected int patrolTargetTicks; // time at the target pos
	
	public EntityElfArcher(EntityType<? extends EntityElfArcher> type, Level world) {
		super(type, world);
		this.workDistanceSq = 24 * 24;
		
		initSpells();
	}

	// TODO would be cool to make them take arrows, or use arrows to do more damage or something.

	@Override
	protected boolean canPerformTask(ILogisticsTask task) {
		;
		
		return false;
	}
	
	@Override
	protected void onIdleTick() {
		this.setPose(ArmPoseElf.IDLE);
		
		// Elf archers patrol when idle!
		idleTicks++;
		
		if (this.navigation.isDone()) {
			LogisticsNetwork network = this.getLogisticsNetwork();
			if (network == null) {
				// Not part of a network so just wander
				EntityFeyBase.FeyWander(this, this.blockPosition(), 5);
				return;
			}
			
			if (patrolTargetTicks-- > 0) {
				return;
			}
			
			// Right after becoming idle, we stay in place for a few seconds. Collect our minds after the battle!
			// Then wander a little bit before resuming patrolling
			if (idleTicks > (20 * 5) && idleTicks <= (20 * (5 + 10))) {
				patrolTarget = null;
				EntityFeyBase.FeyWander(this, this.blockPosition(), 5);
				patrolTargetTicks = random.nextInt(20 * 5) + (20 * 3);
			} else if (idleTicks > (20 * (5 + 10))) {
				// Start patrolling. If we just started, go to a nearby logistics component.
				// Otherwise, find a logistics component connected to the current one and go to it.
				if (patrolTarget != null) {
					// Make sure there's still a component there
					ILogisticsComponent comp = network.getComponentAt(level, patrolTarget);
					if (comp == null) {
						patrolTarget = null;
					} else {
						// Get a nearby neighbor
						Collection<ILogisticsComponent> neighbors = network.getConnectedComponents(comp);
						if (neighbors.isEmpty()) {
							; // leave patrolTarget alone
						} else {
							int randIndex = random.nextInt(neighbors.size());
							Iterator<ILogisticsComponent> it = neighbors.iterator();
							int i = 0;
							while (i++ < randIndex) {
								it.next();
							}
							
							patrolTarget = it.next().getPosition();
						}
					}
				}
				
				// If we come out the above loop with no target, we  either haven't been to a node or
				// the node is gone now. So act like we need to get back to the network.
				if (patrolTarget == null) {
					ILogisticsComponent comp = network.getLogisticsFor(level, this.blockPosition());
					if (comp != null) {
						patrolTarget = comp.getPosition();
					}
				}
				
				// If no node now, we can't get back to network. Wander randomly.
				// Otherwise, move to the designated spot (+- some distance)
				if (patrolTarget != null) {
					EntityFeyBase.FeyWander(this, patrolTarget, 5);
				} else if (this.getHome() != null) {
					EntityFeyBase.FeyWander(this, this.getHome(), 5);
				} else {
					EntityFeyBase.FeyWander(this, this.blockPosition(), 5);
				}
				patrolTargetTicks = random.nextInt(20 * 5) + (20 * 3);
			}
		}
	}
	
	// only available on server
	protected boolean shouldUseBow() {
		LivingEntity target = this.getTarget();
		if (target != null && this.distanceToSqr(target) < 9) {
			return false;
		}
		
		return true;
	}
	
	public void setBattleStance(BattleStanceElfArcher stance) {
		this.entityData.set(STANCE, stance);
	}
	
	public BattleStanceElfArcher getStance() {
		return this.entityData.get(STANCE);
	}

	@Override
	protected void registerGoals() {
		int priority = 1;
		this.goalSelector.addGoal(priority++, new AttackRangedGoal<EntityElfArcher>(this, 1.0, 0, 25) { // All delay in animation
			@Override
			public boolean hasWeaponEquipped(EntityElfArcher elf) {
				return shouldUseBow();
			}
			
			@Override
			protected boolean isAttackAnimationComplete(EntityElfArcher elf) {
				return !elf.swinging;
			}
			
			@Override
			protected void startAttackAnimation(EntityElfArcher elf) {
				elf.swing(InteractionHand.OFF_HAND);
			}
		});
		
		this.goalSelector.addGoal(priority++, new AttackRangedGoal<EntityElfArcher>(this, 0.75, 10, 3) {
			@Override
			public boolean hasWeaponEquipped(EntityElfArcher elf) {
				return !shouldUseBow();
			}
			
			@Override
			protected boolean isAttackAnimationComplete(EntityElfArcher elf) {
				return elf.attackAnim >= .7;// slash is .7 through animation
			}
			
			@Override
			protected void startAttackAnimation(EntityElfArcher elf) {
				elf.swing(InteractionHand.MAIN_HAND);
			}
		});
		
		this.goalSelector.addGoal(priority++, new SpellAttackGoal<EntityElfArcher>(this, 60, 10, false, (elf) -> {
			return elf.getTarget() != null;
		}, new Spell[]{SPELL_HASTE}));
		
		priority = 1;
		this.targetSelector.addGoal(priority++, new HurtByTargetGoal(this).setAlertOthers(EntityElfArcher.class));
		
		// Note this means we'll stop doing a logistics ATTACK task to do this. Perhaps I should make a 'logistics target' task here
		// which you can slot with higher priority so that the following stuff only happens when no task is present?
		this.targetSelector.addGoal(priority++, new NearestAttackableTargetGoal<Monster>(this, Monster.class, 5, true, true, this::canSee));
	}

	public static final AttributeSupplier.Builder BuildArcherAttributes() {
		return EntityElf.BuildAttributes()
				.add(Attributes.MAX_HEALTH, 10.0)
				.add(Attributes.ATTACK_DAMAGE, 4.0)
				.add(Attributes.ARMOR, 2.0)
			;
	}
	
	@Override
	protected void defineSynchedData() {
		super.defineSynchedData();
		entityData.define(STANCE, BattleStanceElfArcher.RANGED);
	}
	
	@Override
	protected void onCombatTick() {
		setPose(ArmPoseElf.ATTACKING);
		
		if (!level.isClientSide) {
			if (this.shouldUseBow()) {
				setBattleStance(BattleStanceElfArcher.RANGED);
			} else {
				setBattleStance(BattleStanceElfArcher.MELEE);
			}
		}
	}
	
	@Override
	protected int getDefaultSwingAnimationDuration() {
		if (this.shouldUseBow()) {
			return 40;
		} else {
			return 10;
		}
		
	}
	
	@Override
	protected void onCientTick() {
		if (this.tickCount % 10 == 0 && this.getElfPose() == ArmPoseElf.WORKING) {
			
			double angle = this.yHeadRot + ((this.isLeftHanded() ? -1 : 1) * 22.5);
			double xdiff = Math.sin(angle / 180.0 * Math.PI) * .4;
			double zdiff = Math.cos(angle / 180.0 * Math.PI) * .4;
			
			double x = getX() - xdiff;
			double z = getZ() + zdiff;
			level.addParticle(ParticleTypes.DRAGON_BREATH, x, getY() + 1.25, z, 0, .015, 0);
		}
	}
	
	protected static final Predicate<Entity> ELF_ARCHER_ARROW_FILTER = EntitySelector.NO_SPECTATORS.and(EntitySelector.ENTITY_STILL_ALIVE).and(new Predicate<Entity>() {
		public boolean test(@Nullable Entity ent) {
			return ent.isPickable() && !(ent instanceof EntityFeyBase) && !(ent instanceof Player);
		}
	});
	
	protected void shootArrowAt(LivingEntity target, float distanceFactor) {
		EntityArrowEx entitytippedarrow = new EntityArrowEx(this.level, this);
		entitytippedarrow.setFilter(ELF_ARCHER_ARROW_FILTER);
		double d0 = target.getX() - this.getX();
		double d1 = target.getBoundingBox().minY + (double)(target.getBbHeight() / 3.0F) - entitytippedarrow.getY();
		double d2 = target.getZ() - this.getZ();
		double d3 = (double)Mth.sqrt(d0 * d0 + d2 * d2);
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
			this.heal(2f);
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
	
	@Override
	public String getSpecializationName() {
		return "Elven Warrior";
	}

	@Override
	public FeyStoneMaterial getCurrentSpecialization() {
		return FeyStoneMaterial.AQUAMARINE;
	}
}
