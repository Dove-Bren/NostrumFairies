package com.smanzana.nostrumfairies.entity.fey;

import javax.annotation.Nullable;

import com.smanzana.nostrumfairies.entity.FairyEntities;
import com.smanzana.nostrumfairies.entity.ResidentType;
import com.smanzana.nostrumfairies.items.FeyStoneMaterial;
import com.smanzana.nostrumfairies.logistics.task.ILogisticsTask;
import com.smanzana.nostrumfairies.logistics.task.LogisticsSubTask;
import com.smanzana.nostrumfairies.logistics.task.LogisticsTaskChopTree;
import com.smanzana.nostrumfairies.logistics.task.LogisticsTaskDepositItem;
import com.smanzana.nostrumfairies.logistics.task.LogisticsTaskWorkBlock;
import com.smanzana.nostrumfairies.serializers.ArmPoseElf;
import com.smanzana.nostrumfairies.serializers.FairyGeneralStatus;
import com.smanzana.nostrumfairies.sound.NostrumFairiesSounds;
import com.smanzana.nostrumfairies.tiles.HomeBlockTileEntity;
import com.smanzana.nostrumfairies.utils.ItemDeepStack;
import com.smanzana.nostrummagica.client.gui.infoscreen.InfoScreenTabs;
import com.smanzana.nostrummagica.entity.tasks.AttackRangedGoal;
import com.smanzana.nostrummagica.loretag.Lore;
import com.smanzana.nostrummagica.spell.EAlteration;
import com.smanzana.nostrummagica.spell.EMagicElement;
import com.smanzana.nostrummagica.spell.Spell;
import com.smanzana.nostrummagica.spell.component.SpellEffectPart;
import com.smanzana.nostrummagica.spell.component.SpellShapePart;
import com.smanzana.nostrummagica.spell.component.shapes.NostrumSpellShapes;

import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ILivingEntityData;
import net.minecraft.entity.IRangedAttackMob;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.ai.attributes.AttributeModifierMap;
import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraft.entity.ai.goal.HurtByTargetGoal;
import net.minecraft.entity.ai.goal.SwimGoal;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.util.DamageSource;
import net.minecraft.util.NonNullList;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.IServerWorld;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;

public class EntityElf extends EntityFeyBase implements IItemCarrierFey, IRangedAttackMob {
	
	public static final String ID = "elf";

	private static Spell SPELL_VINES = null;
	private static Spell SPELL_POISON_WIND = null;
	
	private static void initSpells() {
		if (SPELL_VINES == null) {
			SPELL_VINES = Spell.CreateAISpell("Ancient Vines");
			SPELL_VINES.addPart(new SpellShapePart(NostrumSpellShapes.AI));
			SPELL_VINES.addPart(new SpellEffectPart(EMagicElement.EARTH, 2, EAlteration.INFLICT));
			SPELL_VINES.addPart(new SpellEffectPart(EMagicElement.LIGHTNING, 1, EAlteration.INFLICT));
			
			SPELL_POISON_WIND = Spell.CreateAISpell("Poison Wind");
			SPELL_POISON_WIND.addPart(new SpellShapePart(NostrumSpellShapes.Cutter));
			SPELL_POISON_WIND.addPart(new SpellEffectPart(EMagicElement.WIND, 1, null));
			SPELL_POISON_WIND.addPart(new SpellEffectPart(EMagicElement.WIND, 1, EAlteration.INFLICT));
		}
	}
	
	protected static final DataParameter<ArmPoseElf> POSE  = EntityDataManager.<ArmPoseElf>createKey(EntityElf.class, ArmPoseElf.instance());
	
	private @Nullable BlockPos movePos;
	private @Nullable Entity moveEntity;
	
	public EntityElf(EntityType<? extends EntityElf> type, World world) {
		super(type, world);
		this.workDistanceSq = 32 * 32;
		
		initSpells();
	}

	@Override
	public String getLoreKey() {
		return "elf";
	}

	@Override
	public String getLoreDisplayName() {
		return "elf";
	}

	@Override
	public Lore getBasicLore() {
		return new Lore("test lore for test fairy lol");
	}

	@Override
	public Lore getDeepLore() {
		return new Lore("test lore for test fairy lol");
	}

	@Override
	public InfoScreenTabs getTab() {
		return InfoScreenTabs.INFO_ENTITY;
	}
	
	private static final NonNullList<ItemStack> EMPTY = NonNullList.create();

	@Override
	public NonNullList<ItemStack> getCarriedItems() {
		return EMPTY;
	}

	@Override
	public boolean canAccept(ItemStack stack) {
		return false;
	}
	
	@Override
	public boolean canAccept(ItemDeepStack stack) {
		return false;
	}

	@Override
	public void addItem(ItemStack stack) {
		;
	}
	
	@Override
	public void removeItem(ItemStack stack) {
		;
	}
	
	@Override
	protected boolean onStatusChange(FairyGeneralStatus from, FairyGeneralStatus to) {

		// We want to just drop our task if our status changes from WORKING
		if (from == FairyGeneralStatus.WORKING) {
			this.forfitTask();
		}
		
		switch (to) {
		case IDLE:
			setActivitySummary("status.elf.relax");
			break;
		case REVOLTING:
			setActivitySummary("status.elf.revolt");
			break;
		case WANDERING:
			setActivitySummary("status.elf.wander");
			break;
		case WORKING:
			; // set by task
			break;
		}
		
		return true;
	}

	@Override
	public ResidentType getHomeType() {
		return ResidentType.ELF;
	}
	
	@Override
	protected boolean canPerformTask(ILogisticsTask task) {
		if (task instanceof LogisticsTaskChopTree) {
			LogisticsTaskChopTree chop = (LogisticsTaskChopTree) task;
			
			if (chop.getWorld() != this.world) {
				return false;
			}
			
			// Check where the tree is
			BlockPos pickup = chop.getChopLocation();
			if (pickup == null || !this.canReach(pickup, true)) {
				return false;
			}
			
			pickup = findEmptySpot(pickup, true);
			if (null == pickup) {
				return false;
			}
			
			// Check for pathing
			if (this.getDistanceSq(pickup) < .2) {
				return true;
			}
			if (this.navigator.tryMoveToXYZ(pickup.getX(), pickup.getY(), pickup.getZ(), 1.0)) {
				navigator.clearPath();
				return true;
			}
		}
		
		return false;
	}
	
	@Override
	protected boolean shouldPerformTask(ILogisticsTask task) {
		//return this.heldItem.isEmpty();
		return true;
	}

	@Override
	protected void onTaskChange(ILogisticsTask oldTask, ILogisticsTask newTask) {
//		if (oldTask != null && !heldItem().isEmpty()) {
//			// I guess drop our item
//			dropItem();
//		}
		// Task should have checked if we could hold what it needed, if it's item related.
		// Assuming it did, our current inventory is fine. We'll do that task, maybe use our
		// inventory, and then be idle with an item afterwards -- whicih will prompt
		// us to go return it.
		
		if (newTask == null) {
			;
		} else if (newTask instanceof LogisticsTaskChopTree) {
			setActivitySummary("status.elf.work.chop");
		} else if (newTask instanceof LogisticsTaskDepositItem) {
			setActivitySummary("status.generic.return");
		} else {
			setActivitySummary("status.generic.working");
		}
	}
	
	@Override
	protected void onIdleTick() {
		this.setPose(ArmPoseElf.IDLE);
		
		// See if we're too far away from our home block
		if (this.navigator.noPath()) {
			BlockPos home = this.getHome();
			if (home != null && !this.canReach(this.getPosition(), false)) {
				
				// Go to a random place around our home
				final BlockPos center = home;
				BlockPos targ = null;
				int attempts = 20;
				final double maxDistSq = Math.min(25, this.wanderDistanceSq);
				do {
					double dist = this.rand.nextDouble() * Math.sqrt(maxDistSq);
					float angle = (float) (this.rand.nextDouble() * (2 * Math.PI));
					float tilt = (float) (this.rand.nextDouble() * (2 * Math.PI)) * .5f;
					
					targ = new BlockPos(new Vector3d(
							center.getX() + (Math.cos(angle) * dist),
							center.getY() + (Math.cos(tilt) * dist),
							center.getZ() + (Math.sin(angle) * dist)));
					while (targ.getY() > 0 && world.isAirBlock(targ)) {
						targ = targ.down();
					}
					if (targ.getY() < 256) {
						targ = targ.up();
					}
					
					// We've hit a non-air block. Make sure there's space above it
					BlockPos airBlock = null;
					for (int i = 0; i < Math.ceil(this.getHeight()); i++) {
						if (airBlock == null) {
							airBlock = targ.up();
						} else {
							airBlock = airBlock.up();
						}
						
						if (!world.isAirBlock(airBlock)) {
							targ = null;
							break;
						}
					}
				} while (targ == null && attempts > 0);
				
				if (targ == null) {
					targ = center.up();
				}
				if (!this.getNavigator().tryMoveToXYZ(targ.getX() + .5, targ.getY(), targ.getZ() + .5, 1.0f)) {
					this.getMoveHelper().setMoveTo(targ.getX() + .5, targ.getY(), targ.getZ() + .5, 1.0f);
				}
				
			}
		}
	}

	@Override
	protected void onTaskTick(ILogisticsTask task) {
		LogisticsSubTask sub = task.getActiveSubtask();
		if (sub != null) {
			switch (sub.getType()) {
			case ATTACK:
				setPose(ArmPoseElf.ATTACKING);
				this.faceEntity(sub.getEntity(), 30, 180);
				break;
			case BREAK:
				setPose(ArmPoseElf.WORKING);
				BlockPos pos = sub.getPos();
				double d0 = pos.getX() - this.getPosX();
		        double d2 = pos.getZ() - this.getPosZ();
				float desiredYaw = (float)(MathHelper.atan2(d2, d0) * (180D / Math.PI)) - 90.0F;
				this.setRotation(desiredYaw, .2f);
				//this.rotationYaw = desiredYaw;
				//this.rotationPitch = 1;
				if (this.isSwingInProgress) {
					// On the client, spawn some particles if we're using our wand
					if (ticksExisted % 5 == 0 && getElfPose() == ArmPoseElf.WORKING) {
						world.addParticle(ParticleTypes.DRAGON_BREATH,
								getPosX(), getPosY(), getPosZ(),
								0, 0.3, 0
								);
					}
					if (taskTickCount % 15 == 0 && getElfPose() == ArmPoseElf.WORKING && rand.nextBoolean()) {
						world.playSound(null, getPosX(), getPosY(), getPosZ(), SoundEvents.BLOCK_WOOD_HIT, SoundCategory.NEUTRAL, 1f, 1.6f);
					}
				} else {
					task.markSubtaskComplete();
					if (task.getActiveSubtask() != sub) {
						setPose(ArmPoseElf.IDLE);
						break;
					}
					this.swingArm(this.getActiveHand());
				}
				break;
			case IDLE:
				setPose(ArmPoseElf.IDLE);
				if (this.navigator.noPath()) {
					if (movePos == null) {
						final BlockPos center = sub.getPos();
						BlockPos targ = null;
						int attempts = 20;
						final double maxDistSq = 25;
						do {
							double dist = this.rand.nextDouble() * Math.sqrt(maxDistSq);
							float angle = (float) (this.rand.nextDouble() * (2 * Math.PI));
							float tilt = (float) (this.rand.nextDouble() * (2 * Math.PI)) * .5f;
							
							targ = new BlockPos(new Vector3d(
									center.getX() + (Math.cos(angle) * dist),
									center.getY() + (Math.cos(tilt) * dist),
									center.getZ() + (Math.sin(angle) * dist)));
							while (targ.getY() > 0 && world.isAirBlock(targ)) {
								targ = targ.down();
							}
							if (targ.getY() < 256) {
								targ = targ.up();
							}
							
							// We've hit a non-air block. Make sure there's space above it
							BlockPos airBlock = null;
							for (int i = 0; i < Math.ceil(this.getHeight()); i++) {
								if (airBlock == null) {
									airBlock = targ.up();
								} else {
									airBlock = airBlock.up();
								}
								
								if (!world.isAirBlock(airBlock)) {
									targ = null;
									break;
								}
							}
						} while (targ == null && attempts > 0);
						
						if (targ == null) {
							targ = center.up();
						}
						if (!this.getNavigator().tryMoveToXYZ(targ.getX() + .5, targ.getY(), targ.getZ() + .5, 1.0f)) {
							this.getMoveHelper().setMoveTo(targ.getX() + .5, targ.getY(), targ.getZ() + .5, 1.0f);
						}
						this.movePos = targ;
					} else {
						task.markSubtaskComplete();
						// Cheat and see if we just finished idling
						if (sub != task.getActiveSubtask()) {
							this.movePos = null;
						}
					}
				}
				break;
			case MOVE:
				{
					setPose(ArmPoseElf.IDLE);
					this.feyMoveToTask(task);
				}
				break;
			}
		}
	}

	@Override
	protected void registerGoals() {
		int priority = 1;
		this.goalSelector.addGoal(priority++, new SwimGoal(this));
		//this.goalSelector.addGoal(priority++, new AttackRangedGoal(this, 1.0, 20 * 3, 10));
		this.goalSelector.addGoal(priority++, new AttackRangedGoal<EntityElf>(this, 1.0, 20 * 3, 10) {
			@Override
			public boolean hasWeaponEquipped(EntityElf elf) {
				return true;
			}
			
			@Override
			protected boolean isAttackAnimationComplete(EntityElf elf) {
				return true;
			}
		});
		
//		this.goalSelector.addGoal(priority++, new SpellAttackGoal<EntityElf>(this, 60, 10, true, (elf) -> {
//			return elf.getAttackTarget() != null;
//		}, new Spell[]{SPELL_VINES}));
//		this.goalSelector.addGoal(priority++, new SpellAttackGoal<EntityElf>(this, 20, 4, true, (elf) -> {
//			return elf.getAttackTarget() != null;
//		}, new Spell[]{SPELL_POISON_WIND}));
		
		priority = 1;
		this.targetSelector.addGoal(priority++, new HurtByTargetGoal(this).setCallsForHelp(EntityElf.class));
	}

	public static final AttributeModifierMap.MutableAttribute BuildAttributes() {
		return EntityFeyBase.BuildFeyAttributes()
				.createMutableAttribute(Attributes.MOVEMENT_SPEED, .28)
				.createMutableAttribute(Attributes.MAX_HEALTH, 8.0)
			;
	}
	
	@Override
	public void writeAdditional(CompoundNBT compound) {
		super.writeAdditional(compound);
	}
	
	@Override
	public void readAdditional(CompoundNBT compound) {
		super.readAdditional(compound);
	}

	@Override
	protected boolean canMergeMoreJobs() {
		return true;
	}
	
	@Override
	protected void collideWithEntity(Entity entityIn) {
		super.collideWithEntity(entityIn);
	}

	@Override
	protected String getRandomName() {
		final String[] names = {
			"Lorsan Leolee",
			"Pharom Zinrieth",
			"Luirlan Helefir",
			"Folre Venpetor",
			"Theodred Trisvaris",
			"Evindal Daeneiros",
			"Ninleyn Heryarus",
			"Neldor Morbanise",
			"Theodmer Caimyar",
			"Aithlin Fasandoral",
			"Gwynnestri Yinhice",
			"Anhaern Hertris",
			"Amarille Perbella",
			"Mathienne Zylsandoral",
			"Isarrel Dorqirelle",
			"Helartha Morrona",
			"Kethryllia Urimyar",
			"Enania Virzana",
			"Seldanna Vallar",
			"Lierin Inatris",
			"Aithlin Daris",
			"Ayen Morthana",
			"Nelaeryn Chaelar",
			"Ciliren Yelsandoral",
			"Elaith Omajeon",
			"Saleh Rosys",
			"Aerith Naefiel",
			"Galan Heilana",
			"Elyon Farro",
			"Elred Wysamaer",
		};
		return names[rand.nextInt(names.length)];
	}

	@Override
	protected void onCombatTick() {
		setPose(ArmPoseElf.ATTACKING);
	}
	
	@Override
	protected void registerData() {
		super.registerData();
		dataManager.register(POSE, ArmPoseElf.IDLE);
	}
	
	@Override
	public ILivingEntityData onInitialSpawn(IServerWorld world, DifficultyInstance difficulty, SpawnReason reason, @Nullable ILivingEntityData livingdata, @Nullable CompoundNBT tag) {
		livingdata = super.onInitialSpawn(world, difficulty, reason, livingdata, tag);
		
		// Elves are 70:30 lefthanded
		if (this.rand.nextFloat() < .7f) {
			this.setLeftHanded(true);
		}
		
		return livingdata;
	}
	
	public ArmPoseElf getElfPose() {
		return dataManager.get(POSE);
	}
	
	public void setPose(ArmPoseElf pose) {
		this.dataManager.set(POSE, pose);
	}
	
	@Override
	protected int getDefaultSwingAnimationDuration() {
		return 40;
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

	@Override
	public void attackEntityWithRangedAttack(LivingEntity target, float distanceFactor) {
		if (rand.nextFloat() < .1) {
			SPELL_VINES.cast(this, 1.0f);
		} else {
			SPELL_POISON_WIND.cast(this, 1.0f);
		}
	}
	
	@Override
	public String getSpecializationName() {
		return "Wood Elf";
	}

	@Override
	protected String getUnlocPrefix() {
		return "elf";
	}
	
	@Override
	protected boolean shouldJoin(BlockPos pos, BlockState state, HomeBlockTileEntity te) {
		return rand.nextBoolean() && rand.nextBoolean();
	}

	@Override
	protected void onWanderTick() {
		// Wander around
		if (this.navigator.noPath() && ticksExisted % 50 == 0 && rand.nextBoolean() && rand.nextBoolean()) {
			if (!EntityFeyBase.FeyLazyFollowNearby(this, EntityFeyBase.DOMESTIC_FEY_AND_PLAYER_FILTER, 20, 4, 8)) {
				// Go to a random place
				EntityFeyBase.FeyWander(this, this.getPosition(), Math.min(10, Math.sqrt(this.wanderDistanceSq)));
			}
		}
		
		if (this.getAttackTarget() == null) {
			this.setPose(ArmPoseElf.IDLE);
		}
	}

	@Override
	protected void onRevoltTick() {
		// TODO Auto-generated method stub
		;
	}
	
	@Override
	protected float getGrowthForTask(ILogisticsTask task) {
		if (task instanceof LogisticsTaskChopTree) {
			return 1.2f;
		}
		if (task instanceof LogisticsTaskWorkBlock) {
			return 0.7f;
		}
		
		return 0f;
	}

	@Override
	public EntityFeyBase switchToSpecialization(FeyStoneMaterial material) {
		if (world.isRemote) {
			return this;
		}
		
		EntityFeyBase replacement = null;
		if (material != this.getCurrentSpecialization()) {
			if (material == FeyStoneMaterial.GARNET) {
				// Crafting
				replacement = new EntityElfCrafter(FairyEntities.ElfCrafter, world);
			} else if (material == FeyStoneMaterial.AQUAMARINE) {
				// Archery
				replacement = new EntityElfArcher(FairyEntities.ElfArcher, world);
			} else {
				replacement = new EntityElf(FairyEntities.Elf, world);
			}
		}
		
		if (replacement != null) {
			// Kill this entity and add the other one
			replacement.copyFrom(this);
			//this.remove();
			((ServerWorld) world).removeEntity(this);
			world.addEntity(replacement);
		}
		
		return replacement == null ? this : replacement;
	}

	@Override
	public FeyStoneMaterial getCurrentSpecialization() {
		return null;
	}
	
	@Override
	protected SoundEvent getHurtSound(DamageSource source) {
		return NostrumFairiesSounds.ELF_HURT.getEvent();
	}
	
	@Override
	protected SoundEvent getDeathSound() {
		return NostrumFairiesSounds.ELF_DIE.getEvent();
	}
	
	@Override
	protected @Nullable NostrumFairiesSounds getIdleSound() {
		return NostrumFairiesSounds.ELF_IDLE;
	}
}
