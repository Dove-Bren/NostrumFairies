package com.smanzana.nostrumfairies.entity.navigation;

import net.minecraft.pathfinding.Path;
import net.minecraft.pathfinding.PathPoint;

public class PathPublic extends Path {

	protected PathPoint[] pathpoints;
	
	public PathPublic(PathPoint[] pathpoints) {
		super(pathpoints);
		this.pathpoints = pathpoints;
	}
	
	public PathPoint[] getPathPoints() {
		return pathpoints;
	}

}
