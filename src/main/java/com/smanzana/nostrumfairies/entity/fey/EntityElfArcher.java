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

import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.attributes.AttributeModifierMap;
import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraft.entity.ai.goal.HurtByTargetGoal;
import net.minecraft.entity.ai.goal.NearestAttackableTargetGoal;
import net.minecraft.entity.monster.MonsterEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.Effects;
import net.minecraft.util.EntityPredicates;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;

public class EntityElfArcher extends EntityElf {
	
	public static final String ID = "elf_archer";
	
	protected static final DataParameter<BattleStanceElfArcher> STANCE  = EntityDataManager.<BattleStanceElfArcher>createKey(EntityElfArcher.class, BattleStanceElfArcher.instance());

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
	
	public EntityElfArcher(EntityType<? extends EntityElfArcher> type, World world) {
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
		
		if (this.navigator.noPath()) {
			LogisticsNetwork network = this.getLogisticsNetwork();
			if (network == null) {
				// Not part of a network so just wander
				EntityFeyBase.FeyWander(this, this.getPosition(), 5);
				return;
			}
			
			if (patrolTargetTicks-- > 0) {
				return;
			}
			
			// Right after becoming idle, we stay in place for a few seconds. Collect our minds after the battle!
			// Then wander a little bit before resuming patrolling
			if (idleTicks > (20 * 5) && idleTicks <= (20 * (5 + 10))) {
				patrolTarget = null;
				EntityFeyBase.FeyWander(this, this.getPosition(), 5);
				patrolTargetTicks = rand.nextInt(20 * 5) + (20 * 3);
			} else if (idleTicks > (20 * (5 + 10))) {
				// Start patrolling. If we just started, go to a nearby logistics component.
				// Otherwise, find a logistics component connected to the current one and go to it.
				if (patrolTarget != null) {
					// Make sure there's still a component there
					ILogisticsComponent comp = network.getComponentAt(world, patrolTarget);
					if (comp == null) {
						patrolTarget = null;
					} else {
						// Get a nearby neighbor
						Collection<ILogisticsComponent> neighbors = network.getConnectedComponents(comp);
						if (neighbors.isEmpty()) {
							; // leave patrolTarget alone
						} else {
							int randIndex = rand.nextInt(neighbors.size());
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
					ILogisticsComponent comp = network.getLogisticsFor(world, this.getPosition());
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
					EntityFeyBase.FeyWander(this, this.getPosition(), 5);
				}
				patrolTargetTicks = rand.nextInt(20 * 5) + (20 * 3);
			}
		}
	}
	
	// only available on server
	protected boolean shouldUseBow() {
		LivingEntity target = this.getAttackTarget();
		if (target != null && this.getDistanceSq(target) < 9) {
			return false;
		}
		
		return true;
	}
	
	public void setBattleStance(BattleStanceElfArcher stance) {
		this.dataManager.set(STANCE, stance);
	}
	
	public BattleStanceElfArcher getStance() {
		return this.dataManager.get(STANCE);
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
				return !elf.isSwingInProgress;
			}
			
			@Override
			protected void startAttackAnimation(EntityElfArcher elf) {
				elf.swingArm(Hand.OFF_HAND);
			}
		});
		
		this.goalSelector.addGoal(priority++, new AttackRangedGoal<EntityElfArcher>(this, 0.75, 10, 3) {
			@Override
			public boolean hasWeaponEquipped(EntityElfArcher elf) {
				return !shouldUseBow();
			}
			
			@Override
			protected boolean isAttackAnimationComplete(EntityElfArcher elf) {
				return elf.swingProgress >= .7;// slash is .7 through animation
			}
			
			@Override
			protected void startAttackAnimation(EntityElfArcher elf) {
				elf.swingArm(Hand.MAIN_HAND);
			}
		});
		
		this.goalSelector.addGoal(priority++, new SpellAttackGoal<EntityElfArcher>(this, 60, 10, false, (elf) -> {
			return elf.getAttackTarget() != null;
		}, new Spell[]{SPELL_HASTE}));
		
		priority = 1;
		this.targetSelector.addGoal(priority++, new HurtByTargetGoal(this).setCallsForHelp(EntityElfArcher.class));
		
		// Note this means we'll stop doing a logistics ATTACK task to do this. Perhaps I should make a 'logistics target' task here
		// which you can slot with higher priority so that the following stuff only happens when no task is present?
		this.targetSelector.addGoal(priority++, new NearestAttackableTargetGoal<MonsterEntity>(this, MonsterEntity.class, 5, true, true, this::canEntityBeSeen));
	}

	public static final AttributeModifierMap.MutableAttribute BuildArcherAttributes() {
		return EntityElf.BuildAttributes()
				.createMutableAttribute(Attributes.MAX_HEALTH, 10.0)
				.createMutableAttribute(Attributes.ATTACK_DAMAGE, 4.0)
				.createMutableAttribute(Attributes.ARMOR, 2.0)
			;
	}
	
	@Override
	protected void registerData() {
		super.registerData();
		dataManager.register(STANCE, BattleStanceElfArcher.RANGED);
	}
	
	@Override
	protected void onCombatTick() {
		setPose(ArmPoseElf.ATTACKING);
		
		if (!world.isRemote) {
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
		if (this.ticksExisted % 10 == 0 && this.getElfPose() == ArmPoseElf.WORKING) {
			
			double angle = this.rotationYawHead + ((this.isLeftHanded() ? -1 : 1) * 22.5);
			double xdiff = Math.sin(angle / 180.0 * Math.PI) * .4;
			double zdiff = Math.cos(angle / 180.0 * Math.PI) * .4;
			
			double x = getPosX() - xdiff;
			double z = getPosZ() + zdiff;
			world.addParticle(ParticleTypes.DRAGON_BREATH, x, getPosY() + 1.25, z, 0, .015, 0);
		}
	}
	
	protected static final Predicate<Entity> ELF_ARCHER_ARROW_FILTER = EntityPredicates.NOT_SPECTATING.and(EntityPredicates.IS_ALIVE).and(new Predicate<Entity>() {
		public boolean test(@Nullable Entity ent) {
			return ent.canBeCollidedWith() && !(ent instanceof EntityFeyBase) && !(ent instanceof PlayerEntity);
		}
	});
	
	protected void shootArrowAt(LivingEntity target, float distanceFactor) {
		EntityArrowEx entitytippedarrow = new EntityArrowEx(this.world, this);
		entitytippedarrow.setFilter(ELF_ARCHER_ARROW_FILTER);
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
			this.heal(2f);
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
	
	@Override
	public String getSpecializationName() {
		return "Elven Warrior";
	}

	@Override
	public FeyStoneMaterial getCurrentSpecialization() {
		return FeyStoneMaterial.AQUAMARINE;
	}
}
