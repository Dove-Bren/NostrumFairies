package com.smanzana.nostrumfairies.entity.fey;

import java.util.Collection;
import java.util.Iterator;

import javax.annotation.Nullable;

import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import com.smanzana.nostrumfairies.entity.EntityTippedArrowEx;
import com.smanzana.nostrumfairies.items.FeyStoneMaterial;
import com.smanzana.nostrumfairies.logistics.ILogisticsComponent;
import com.smanzana.nostrumfairies.logistics.LogisticsNetwork;
import com.smanzana.nostrumfairies.logistics.task.ILogisticsTask;
import com.smanzana.nostrumfairies.serializers.ArmPoseElf;
import com.smanzana.nostrumfairies.serializers.BattleStanceElfArcher;
import com.smanzana.nostrummagica.entity.tasks.EntityAIAttackRanged;
import com.smanzana.nostrummagica.entity.tasks.EntitySpellAttackTask;
import com.smanzana.nostrummagica.spells.EAlteration;
import com.smanzana.nostrummagica.spells.EMagicElement;
import com.smanzana.nostrummagica.spells.Spell;
import com.smanzana.nostrummagica.spells.Spell.SpellPart;
import com.smanzana.nostrummagica.spells.components.shapes.SingleShape;
import com.smanzana.nostrummagica.spells.components.triggers.SelfTrigger;

import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.EntityAIHurtByTarget;
import net.minecraft.entity.ai.EntityAINearestAttackableTarget;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Enchantments;
import net.minecraft.init.Items;
import net.minecraft.init.MobEffects;
import net.minecraft.item.ItemStack;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.EntitySelectors;
import net.minecraft.util.EnumHand;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;

public class EntityElfArcher extends EntityElf {
	
	protected static final DataParameter<BattleStanceElfArcher> STANCE  = EntityDataManager.<BattleStanceElfArcher>createKey(EntityElfArcher.class, BattleStanceElfArcher.instance());

	private static Spell SPELL_HASTE = null;
	
	private static void initSpells() {
		if (SPELL_HASTE == null) {
			SPELL_HASTE = new Spell("Elven Haste");
			SPELL_HASTE.addPart(new SpellPart(SelfTrigger.instance()));
			SPELL_HASTE.addPart(new SpellPart(SingleShape.instance(), EMagicElement.WIND, 1, EAlteration.SUPPORT));
		}
	}

	protected int idleTicks;
	protected BlockPos patrolTarget;
	protected int patrolTargetTicks; // time at the target pos
	
	public EntityElfArcher(World world) {
		super(world);
		this.height = 0.90f;
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
		EntityLivingBase target = this.getAttackTarget();
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
	protected void initEntityAI() {
		int priority = 1;
		this.tasks.addTask(priority++, new EntityAIAttackRanged<EntityElfArcher>(this, 1.0, 0, 25) { // All delay in animation
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
				elf.swingArm(EnumHand.OFF_HAND);
			}
		});
		
		this.tasks.addTask(priority++, new EntityAIAttackRanged<EntityElfArcher>(this, 0.75, 10, 3) {
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
				elf.swingArm(EnumHand.MAIN_HAND);
			}
		});
		
		this.tasks.addTask(priority++, new EntitySpellAttackTask<EntityElfArcher>(this, 60, 10, false, (elf) -> {
			return elf.getAttackTarget() != null;
		}, new Spell[]{SPELL_HASTE}));
		
		priority = 1;
		this.targetTasks.addTask(priority++, new EntityAIHurtByTarget(this, true, new Class[0]));
		
		// Note this means we'll stop doing a logistics ATTACK task to do this. Perhaps I should make a 'logistics target' task here
		// which you can slot with higher priority so that the following stuff only happens when no task is present?
		this.targetTasks.addTask(priority++, new EntityAINearestAttackableTarget<EntityMob>(this, EntityMob.class, 5, true, true, EntityMob.VISIBLE_MOB_SELECTOR));
	}

	@Override
	protected void applyEntityAttributes() {
		super.applyEntityAttributes();
		this.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(0.28D);
		this.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(10.0D);
		this.getAttributeMap().registerAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).setBaseValue(4.0D);
		this.getEntityAttribute(SharedMonsterAttributes.ARMOR).setBaseValue(2.0D);
		this.getEntityAttribute(SharedMonsterAttributes.FOLLOW_RANGE).setBaseValue(Math.sqrt(MAX_FAIRY_DISTANCE_SQ));
	}
	
	@Override
	protected void entityInit() {
		super.entityInit();
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
		if (this.ticksExisted % 10 == 0 && this.getPose() == ArmPoseElf.WORKING) {
			
			double angle = this.rotationYawHead + ((this.isLeftHanded() ? -1 : 1) * 22.5);
			double xdiff = Math.sin(angle / 180.0 * Math.PI) * .4;
			double zdiff = Math.cos(angle / 180.0 * Math.PI) * .4;
			
			double x = posX - xdiff;
			double z = posZ + zdiff;
			world.spawnParticle(EnumParticleTypes.DRAGON_BREATH, x, posY + 1.25, z, 0, .015, 0, new int[0]);
		}
	}
	
	@SuppressWarnings("unchecked")
	protected static final Predicate<Entity> ELF_ARCHER_ARROW_FILTER = Predicates.and(EntitySelectors.NOT_SPECTATING, EntitySelectors.IS_ALIVE, new Predicate<Entity>() {
		public boolean apply(@Nullable Entity ent) {
			return ent.canBeCollidedWith() && !(ent instanceof EntityFeyBase) && !(ent instanceof EntityPlayer);
		}
	});
	
	protected void shootArrowAt(EntityLivingBase target, float distanceFactor) {
		EntityTippedArrowEx entitytippedarrow = new EntityTippedArrowEx(this.world, this);
		entitytippedarrow.setFilter(ELF_ARCHER_ARROW_FILTER);
		double d0 = target.posX - this.posX;
		double d1 = target.getEntityBoundingBox().minY + (double)(target.height / 3.0F) - entitytippedarrow.posY;
		double d2 = target.posZ - this.posZ;
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

		ItemStack itemstack = this.getHeldItem(EnumHand.OFF_HAND);

		if (!itemstack.isEmpty() && itemstack.getItem() == Items.TIPPED_ARROW)
		{
			entitytippedarrow.setPotionEffect(itemstack);
		}
		else if (rand.nextBoolean() && rand.nextBoolean() && rand.nextBoolean())
		{
			entitytippedarrow.addEffect(new PotionEffect(MobEffects.POISON, 600));
		}

		//this.playSound(SoundEvents.ENTITY_SKELETON_SHOOT, 1.0F, 1.0F / (this.getRNG().nextFloat() * 0.4F + 0.8F));
		this.world.spawnEntity(entitytippedarrow);
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
	
	@Override
	public String getSpecializationName() {
		return "Elven Warrior";
	}

	@Override
	public FeyStoneMaterial getCurrentSpecialization() {
		return FeyStoneMaterial.AQUAMARINE;
	}
}
