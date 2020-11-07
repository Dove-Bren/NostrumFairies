package com.smanzana.nostrumfairies.entity.navigation;

import java.lang.reflect.Field;

import net.minecraft.entity.EntityLiving;
import net.minecraft.pathfinding.PathNavigate;
import net.minecraft.pathfinding.PathNavigateGround;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.ReflectionHelper;

public class PathNavigatorGroundFixed extends PathNavigateGround {

	public PathNavigatorGroundFixed(EntityLiving entitylivingIn, World worldIn) {
		super(entitylivingIn, worldIn);
	}
	
	protected void clearStuckEntity() {
		this.clearPathEntity();
	}

	@Override
	protected void checkForStuck(Vec3d positionVec3) {
		//Field totalTicksField = ReflectionHelper.findField(PathNavigate.class, "totalTicks");
		final int totalTicks = ReflectionHelper.getPrivateValue(PathNavigate.class, this, "totalTicks", "field_75510_g");
		Field ticksAtLastPosField = ReflectionHelper.findField(PathNavigate.class, "ticksAtLastPos", "field_75520_h");
		Field lastPosCheckField = ReflectionHelper.findField(PathNavigate.class, "lastPosCheck", "field_75521_i");
		Field timeoutCachedNodeField = ReflectionHelper.findField(PathNavigate.class, "timeoutCachedNode", "field_188557_k");
		Field timeoutLimitField = ReflectionHelper.findField(PathNavigate.class, "timeoutLimit", "field_188560_n");
		Field timeoutTimerField = ReflectionHelper.findField(PathNavigate.class, "timeoutTimer", "field_188558_l");
		Field lastTimeoutCheckField = ReflectionHelper.findField(PathNavigate.class, "lastTimeoutCheck", "field_188559_m");
		
		try {
			if (totalTicks - ticksAtLastPosField.getInt(this) > 100) {
				if (positionVec3.squareDistanceTo((Vec3d) lastPosCheckField.get(this)) < 2.25D) {
					this.clearPathEntity();
				}
	
				ticksAtLastPosField.setInt(this, totalTicks);
				lastPosCheckField.set(this, positionVec3);
			}
	
			if (this.currentPath != null && !this.currentPath.isFinished()) {
				Vec3d vec3d = this.currentPath.getCurrentPos();
	
				if (vec3d.equals(timeoutCachedNodeField.get(this))) {
					timeoutTimerField.setLong(this, timeoutTimerField.getLong(this) + (System.currentTimeMillis() - lastTimeoutCheckField.getLong(this)));
					//timeoutTimer += System.currentTimeMillis() - lastTimeoutCheckField.getInt(this);
				} else {
					timeoutCachedNodeField.set(this, vec3d);
					double d0 = positionVec3.distanceTo((Vec3d) timeoutCachedNodeField.get(this));
					timeoutLimitField.setDouble(this, this.theEntity.getAIMoveSpeed() > 0.0F ? d0 / (double)this.theEntity.getAIMoveSpeed() * 1000.0D : 0.0D);
					timeoutTimerField.setLong(this, 0);
				}
	
				if (timeoutLimitField.getDouble(this) > 0.0D && (double)timeoutTimerField.getLong(this) > timeoutLimitField.getDouble(this) * 3.0D) {
					timeoutCachedNodeField.set(this, Vec3d.ZERO);
					timeoutTimerField.setLong(this, 0L);
					timeoutLimitField.setDouble(this, 0.0D);
					clearStuckEntity();
				}
	
				lastTimeoutCheckField.setLong(this, System.currentTimeMillis());
			}
		} catch (Exception e) {
			System.out.println("lol: " + e);
			e.printStackTrace();
		}
	}

}
