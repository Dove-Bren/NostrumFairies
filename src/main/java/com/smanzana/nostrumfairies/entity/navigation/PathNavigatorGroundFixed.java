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

	@Override
	protected void checkForStuck(Vec3d positionVec3) {
		//Field totalTicksField = ReflectionHelper.findField(PathNavigate.class, "totalTicks");
		final int totalTicks = ReflectionHelper.getPrivateValue(PathNavigate.class, this, "totalTicks");
		Field ticksAtLastPosField = ReflectionHelper.findField(PathNavigate.class, "ticksAtLastPos");
		Field lastPosCheckField = ReflectionHelper.findField(PathNavigate.class, "lastPosCheck");
		Field timeoutCachedNodeField = ReflectionHelper.findField(PathNavigate.class, "timeoutCachedNode");
		Field timeoutLimitField = ReflectionHelper.findField(PathNavigate.class, "timeoutLimit");
		Field timeoutTimerField = ReflectionHelper.findField(PathNavigate.class, "timeoutTimer");
		Field lastTimeoutCheckField = ReflectionHelper.findField(PathNavigate.class, "lastTimeoutCheck");
		
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
					this.clearPathEntity();
				}
	
				lastTimeoutCheckField.setLong(this, System.currentTimeMillis());
			}
		} catch (Exception e) {
			System.out.println("lol");
		}
	}

}
