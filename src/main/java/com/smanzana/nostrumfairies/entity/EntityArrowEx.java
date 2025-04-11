package com.smanzana.nostrumfairies.entity;

import java.util.function.Predicate;

import javax.annotation.Nullable;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.Arrow;
import net.minecraft.network.protocol.Packet;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.level.Level;
import net.minecraftforge.fml.network.NetworkHooks;

/**
 * Just a more generalized version of EntityTippedArrow lol. Stupid vanilla.
 * @author Skyler
 *
 */
public class EntityArrowEx extends Arrow {
	
	private static final Predicate<Entity> ARROW_TARGETS_VANILLA = EntitySelector.NO_SPECTATORS.and(EntitySelector.ENTITY_STILL_ALIVE).and(new Predicate<Entity>() {
		@Override
		public boolean test(@Nullable Entity p_apply_1_) {
			return p_apply_1_.isPickable();
		}
	});
	
	public static final String ID = "nostrum_custom_arrow";
	
	protected Predicate<? super Entity> filter;
	
	public EntityArrowEx(EntityType<? extends EntityArrowEx> type, Level worldIn) {
		super(type, worldIn);
		filter = ARROW_TARGETS_VANILLA;
	}
	
	public EntityArrowEx(Level worldIn) {
		this(FairyEntities.ArrowEx, worldIn);
	}

	public EntityArrowEx(Level worldIn, double x, double y, double z) {
		this(worldIn);
		this.setPos(x, y , z);
	}

	public EntityArrowEx(Level worldIn, LivingEntity shooter) {
		this(worldIn);
		this.setOwner(shooter);
		
		// This is baked in to parent versions that we can't call
		this.setPos(shooter.getX(), shooter.getY() + (double)shooter.getEyeHeight() - 0.10000000149011612D, shooter.getZ());
	}
	
	public void setFilter(Predicate<Entity> filter) {
		this.filter = filter;
	}
	
//	@Nullable
//	@Override
//	protected EntityRayTraceResult rayTraceEntities(Vector3d startVec, Vector3d endVec) {
//		return ProjectileHelper.rayTraceEntities(this.world, this, startVec, endVec, this.getBoundingBox().expand(this.getMotion()).grow(1.0D), (ent) -> {
//			return this.filter.test(ent);
//		});
//	}
	
	@Override
	protected boolean canHitEntity(Entity ent) { // CanHit
		return super.canHitEntity(ent) && this.filter.test(ent);
	}
	
	@Override
	public Packet<?> getAddEntityPacket() {
		// Have to override and use forge to use with non-living Entity types even though parent defines
		return NetworkHooks.getEntitySpawningPacket(this);
	}
}
