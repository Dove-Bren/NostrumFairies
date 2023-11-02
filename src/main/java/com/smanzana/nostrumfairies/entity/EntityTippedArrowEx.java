package com.smanzana.nostrumfairies.entity;

import java.util.List;

import javax.annotation.Nullable;

import com.google.common.base.Predicate;
import com.google.common.base.Predicates;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.projectile.EntityTippedArrow;
import net.minecraft.util.EntitySelectors;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

/**
 * Just a more generalized version of EntityTippedArrow lol. Stupid vanilla.
 * @author Skyler
 *
 */
public class EntityTippedArrowEx extends EntityTippedArrow {

	@SuppressWarnings("unchecked")
	private static final Predicate<? super Entity> ARROW_TARGETS_VANILLA = Predicates.and(EntitySelectors.NOT_SPECTATING, EntitySelectors.IS_ALIVE, new Predicate<Entity>() {
		public boolean apply(@Nullable Entity p_apply_1_) {
			return p_apply_1_.canBeCollidedWith();
		}
	});
	
	protected Predicate<? super Entity> filter;
	
	public EntityTippedArrowEx(World worldIn) {
		super(worldIn);
		filter = ARROW_TARGETS_VANILLA;
	}

	public EntityTippedArrowEx(World worldIn, double x, double y, double z) {
		super(worldIn, x, y, z);
		filter = ARROW_TARGETS_VANILLA;
	}

	public EntityTippedArrowEx(World worldIn, LivingEntity shooter) {
		super(worldIn, shooter);
		filter = ARROW_TARGETS_VANILLA;
	}
	
	public void setFilter(Predicate<Entity> filter) {
		this.filter = filter;
	}
	
	@Nullable
	@Override
	protected Entity findEntityOnPath(Vec3d start, Vec3d end) {
		Entity entity = null;
		List<Entity> list = this.world.getEntitiesInAABBexcluding(this, this.getEntityBoundingBox().expand(this.motionX, this.motionY, this.motionZ).grow(1.0D), this.filter);
		double d0 = 0.0D;

		for (int i = 0; i < list.size(); ++i) {
			Entity entity1 = (Entity)list.get(i);

			if (entity1 != this.shootingEntity || this.ticksExisted >= 5) {
				AxisAlignedBB axisalignedbb = entity1.getEntityBoundingBox().grow(0.30000001192092896D);
				RayTraceResult raytraceresult = axisalignedbb.calculateIntercept(start, end);

				if (raytraceresult != null) {
					double d1 = start.squareDistanceTo(raytraceresult.hitVec);

					if (d1 < d0 || d0 == 0.0D) {
						entity = entity1;
						d0 = d1;
					}
				}
			}
		}

		return entity;
	}
}
