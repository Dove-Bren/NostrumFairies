package com.smanzana.nostrumfairies.entity.fey;

import java.io.IOException;

import javax.annotation.Nullable;

import com.smanzana.nostrumfairies.blocks.FeyHomeBlock.HomeBlockTileEntity;
import com.smanzana.nostrumfairies.blocks.FeyHomeBlock.ResidentType;
import com.smanzana.nostrumfairies.logistics.task.ILogisticsTask;
import com.smanzana.nostrumfairies.logistics.task.LogisticsSubTask;
import com.smanzana.nostrumfairies.logistics.task.LogisticsTaskChopTree;
import com.smanzana.nostrumfairies.logistics.task.LogisticsTaskDepositItem;
import com.smanzana.nostrumfairies.utils.ItemDeepStack;
import com.smanzana.nostrummagica.client.gui.infoscreen.InfoScreenTabs;
import com.smanzana.nostrummagica.entity.tasks.EntityAIAttackRanged;
import com.smanzana.nostrummagica.loretag.Lore;
import com.smanzana.nostrummagica.spells.EAlteration;
import com.smanzana.nostrummagica.spells.EMagicElement;
import com.smanzana.nostrummagica.spells.Spell;
import com.smanzana.nostrummagica.spells.Spell.SpellPart;
import com.smanzana.nostrummagica.spells.components.shapes.SingleShape;
import com.smanzana.nostrummagica.spells.components.triggers.AITargetTrigger;
import com.smanzana.nostrummagica.spells.components.triggers.MagicCutterTrigger;

import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.IEntityLivingData;
import net.minecraft.entity.IRangedAttackMob;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.EntityAIHurtByTarget;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializer;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.World;

public class EntityElf extends EntityFeyBase implements IItemCarrierFey, IRangedAttackMob {

	public static enum ArmPose {
		IDLE,
		WORKING,
		ATTACKING;
		
		public final static class PoseSerializer implements DataSerializer<ArmPose> {
			
			private PoseSerializer() {
				DataSerializers.registerSerializer(this);
			}
			
			@Override
			public void write(PacketBuffer buf, ArmPose value) {
				buf.writeEnumValue(value);
			}

			@Override
			public ArmPose read(PacketBuffer buf) throws IOException {
				return buf.readEnumValue(ArmPose.class);
			}

			@Override
			public DataParameter<ArmPose> createKey(int id) {
				return new DataParameter<>(id, this);
			}
		}
		
		public static final PoseSerializer Serializer = new PoseSerializer();
	}
	
	private static Spell SPELL_VINES = null;
	private static Spell SPELL_POISON_WIND = null;
	
	private static void initSpells() {
		if (SPELL_VINES == null) {
			SPELL_VINES = new Spell("Ancient Vines");
			SPELL_VINES.addPart(new SpellPart(AITargetTrigger.instance()));
			SPELL_VINES.addPart(new SpellPart(SingleShape.instance(), EMagicElement.EARTH, 2, EAlteration.INFLICT));
			SPELL_VINES.addPart(new SpellPart(SingleShape.instance(), EMagicElement.LIGHTNING, 1, EAlteration.INFLICT));
			
			SPELL_POISON_WIND = new Spell("Poison Wind");
			SPELL_POISON_WIND.addPart(new SpellPart(MagicCutterTrigger.instance()));
			SPELL_POISON_WIND.addPart(new SpellPart(SingleShape.instance(), EMagicElement.WIND, 1, null));
			SPELL_POISON_WIND.addPart(new SpellPart(SingleShape.instance(), EMagicElement.WIND, 1, EAlteration.INFLICT));
		}
	}
	
	protected static final DataParameter<ArmPose> POSE  = EntityDataManager.<ArmPose>createKey(EntityElf.class, ArmPose.Serializer);
	
	private @Nullable BlockPos movePos;
	private @Nullable Entity moveEntity;
	
	public EntityElf(World world) {
		super(world);
		this.height = 0.99f;
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
	
	private static final ItemStack[] EMPTY = new ItemStack[0];

	@Override
	public ItemStack[] getCarriedItems() {
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
			
			if (chop.getWorld() != this.worldObj) {
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
				navigator.clearPathEntity();
				return true;
			}
		}
		
		return false;
	}
	
	@Override
	protected boolean shouldPerformTask(ILogisticsTask task) {
		//return this.heldItem == null;
		return true;
	}

	@Override
	protected void onTaskChange(ILogisticsTask oldTask, ILogisticsTask newTask) {
//		if (oldTask != null && heldItem != null) {
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
			setActivitySummary("status.generic.work");
		}
	}
	
	@Override
	protected void onIdleTick() {
		this.setPose(ArmPose.IDLE);
		
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
					
					targ = new BlockPos(new Vec3d(
							center.getX() + (Math.cos(angle) * dist),
							center.getY() + (Math.cos(tilt) * dist),
							center.getZ() + (Math.sin(angle) * dist)));
					while (targ.getY() > 0 && worldObj.isAirBlock(targ)) {
						targ = targ.down();
					}
					if (targ.getY() < 256) {
						targ = targ.up();
					}
					
					// We've hit a non-air block. Make sure there's space above it
					BlockPos airBlock = null;
					for (int i = 0; i < Math.ceil(this.height); i++) {
						if (airBlock == null) {
							airBlock = targ.up();
						} else {
							airBlock = airBlock.up();
						}
						
						if (!worldObj.isAirBlock(airBlock)) {
							targ = null;
							break;
						}
					}
				} while (targ == null && attempts > 0);
				
				if (targ == null) {
					targ = center.up();
				}
				if (!this.getNavigator().tryMoveToXYZ(targ.getX() + .5, targ.getY(), targ.getZ() + .5, 1.0f)) {
					this.moveHelper.setMoveTo(targ.getX() + .5, targ.getY(), targ.getZ() + .5, 1.0f);
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
				setPose(ArmPose.ATTACKING);
				this.faceEntity(sub.getEntity(), 30, 180);
				break;
			case BREAK:
				setPose(ArmPose.WORKING);
				BlockPos pos = sub.getPos();
				double d0 = pos.getX() - this.posX;
		        double d2 = pos.getZ() - this.posZ;
				float desiredYaw = (float)(MathHelper.atan2(d2, d0) * (180D / Math.PI)) - 90.0F;
				this.setRotation(desiredYaw, .2f);
				//this.rotationYaw = desiredYaw;
				//this.rotationPitch = 1;
				if (this.isSwingInProgress) {
					// On the client, spawn some particles if we're using our wand
					if (ticksExisted % 5 == 0 && getPose() == ArmPose.WORKING) {
						worldObj.spawnParticle(EnumParticleTypes.DRAGON_BREATH,
								posX, posY, posZ,
								0, 0.3, 0,
								new int[0]);
					}
				} else {
					task.markSubtaskComplete();
					if (task.getActiveSubtask() != sub) {
						setPose(ArmPose.IDLE);
						break;
					}
					this.swingArm(this.getActiveHand());
				}
				break;
			case IDLE:
				setPose(ArmPose.IDLE);
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
							
							targ = new BlockPos(new Vec3d(
									center.getX() + (Math.cos(angle) * dist),
									center.getY() + (Math.cos(tilt) * dist),
									center.getZ() + (Math.sin(angle) * dist)));
							while (targ.getY() > 0 && worldObj.isAirBlock(targ)) {
								targ = targ.down();
							}
							if (targ.getY() < 256) {
								targ = targ.up();
							}
							
							// We've hit a non-air block. Make sure there's space above it
							BlockPos airBlock = null;
							for (int i = 0; i < Math.ceil(this.height); i++) {
								if (airBlock == null) {
									airBlock = targ.up();
								} else {
									airBlock = airBlock.up();
								}
								
								if (!worldObj.isAirBlock(airBlock)) {
									targ = null;
									break;
								}
							}
						} while (targ == null && attempts > 0);
						
						if (targ == null) {
							targ = center.up();
						}
						if (!this.getNavigator().tryMoveToXYZ(targ.getX() + .5, targ.getY(), targ.getZ() + .5, 1.0f)) {
							this.moveHelper.setMoveTo(targ.getX() + .5, targ.getY(), targ.getZ() + .5, 1.0f);
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
					setPose(ArmPose.IDLE);
					if (this.navigator.noPath()) {
						// First time through?
						if ((movePos != null && this.getDistanceSqToCenter(movePos) < 1)
							|| (moveEntity != null && this.getDistanceToEntity(moveEntity) < 1)) {
							task.markSubtaskComplete();
							movePos = null;
							moveEntity = null;
							return;
						}
						movePos = null;
						moveEntity = null;
						
						movePos = sub.getPos();
						if (movePos == null) {
							moveEntity = sub.getEntity();
							if (!this.getNavigator().tryMoveToEntityLiving(moveEntity,  1)) {
								this.moveHelper.setMoveTo(moveEntity.posX, moveEntity.posY, moveEntity.posZ, 1.0f);
							}
						} else {
							movePos = findEmptySpot(movePos, false);
							
							// Is the block we shifted to where we are?
							if (!this.getPosition().equals(movePos) && this.getDistanceSqToCenter(movePos) > 1) {
								if (!this.getNavigator().tryMoveToXYZ(movePos.getX(), movePos.getY(), movePos.getZ(), 1.0f)) {
									this.moveHelper.setMoveTo(movePos.getX(), movePos.getY(), movePos.getZ(), 1.0f);
								}
							}
						}
					}
				}
				break;
			}
		}
	}

	@Override
	protected void initEntityAI() {
		int priority = 1;
		//this.tasks.addTask(priority++, new EntityAIAttackRanged(this, 1.0, 20 * 3, 10));
		this.tasks.addTask(priority++, new EntityAIAttackRanged<EntityElf>(this, 1.0, 20 * 3, 10) {
			@Override
			public boolean hasWeaponEquipped(EntityElf elf) {
				return true;
			}
			
			@Override
			protected boolean isAttackAnimationComplete(EntityElf elf) {
				return true;
			}
		});
		
//		this.tasks.addTask(priority++, new EntitySpellAttackTask<EntityElf>(this, 60, 10, true, (elf) -> {
//			return elf.getAttackTarget() != null;
//		}, new Spell[]{SPELL_VINES}));
//		this.tasks.addTask(priority++, new EntitySpellAttackTask<EntityElf>(this, 20, 4, true, (elf) -> {
//			return elf.getAttackTarget() != null;
//		}, new Spell[]{SPELL_POISON_WIND}));
		
		priority = 1;
		this.targetTasks.addTask(priority++, new EntityAIHurtByTarget(this, true, new Class[0]));
	}

	@Override
	protected void applyEntityAttributes() {
		super.applyEntityAttributes();
		this.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(0.28D);
		this.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(8.0D);
		//this.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).setBaseValue(0.0D);
		this.getEntityAttribute(SharedMonsterAttributes.ARMOR).setBaseValue(0.0D);
		this.getEntityAttribute(SharedMonsterAttributes.FOLLOW_RANGE).setBaseValue(Math.sqrt(MAX_FAIRY_DISTANCE_SQ));
	}
	
	@Override
	public void writeEntityToNBT(NBTTagCompound compound) {
		super.writeEntityToNBT(compound);
	}
	
	@Override
	public void readEntityFromNBT(NBTTagCompound compound) {
		super.readEntityFromNBT(compound);
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
		setPose(ArmPose.ATTACKING);
	}
	
	@Override
	protected void entityInit() {
		super.entityInit();
		dataManager.register(POSE, ArmPose.IDLE);
	}
	
	@Override
	public IEntityLivingData onInitialSpawn(DifficultyInstance difficulty, @Nullable IEntityLivingData livingdata) {
		livingdata = super.onInitialSpawn(difficulty, livingdata);
		
		// Elves are 70:30 lefthanded
		if (this.rand.nextFloat() < .7f) {
			this.setLeftHanded(true);
		}
		
		return livingdata;
	}
	
	public ArmPose getPose() {
		return dataManager.get(POSE);
	}
	
	public void setPose(ArmPose pose) {
		this.dataManager.set(POSE, pose);
	}
	
	@Override
	protected int getDefaultSwingAnimationDuration() {
		return 40;
	}
	
	@Override
	protected void onCientTick() {
		if (this.ticksExisted % 10 == 0 && this.getPose() == ArmPose.WORKING) {
			
			double angle = this.rotationYawHead + ((this.isLeftHanded() ? -1 : 1) * 22.5);
			double xdiff = Math.sin(angle / 180.0 * Math.PI) * .4;
			double zdiff = Math.cos(angle / 180.0 * Math.PI) * .4;
			
			double x = posX - xdiff;
			double z = posZ + zdiff;
			worldObj.spawnParticle(EnumParticleTypes.DRAGON_BREATH, x, posY + 1.25, z, 0, .015, 0, new int[0]);
		}
	}

	@Override
	public void attackEntityWithRangedAttack(EntityLivingBase target, float distanceFactor) {
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
	protected boolean shouldJoin(BlockPos pos, IBlockState state, HomeBlockTileEntity te) {
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
		
		return 0f;
	}
}
