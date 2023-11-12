package com.smanzana.nostrumfairies.entity.navigation;

import java.util.List;

import net.minecraft.pathfinding.Path;
import net.minecraft.pathfinding.PathPoint;
import net.minecraft.util.math.BlockPos;

public class PathPublic extends Path {

	protected List<PathPoint> pathpoints;
	
	public PathPublic(List<PathPoint> pathpoints, BlockPos target, boolean flagged) {
		super(pathpoints, target, flagged);
		this.pathpoints = pathpoints;
	}
	
	public List<PathPoint> getPathPoints() {
		return pathpoints;
	}

}
