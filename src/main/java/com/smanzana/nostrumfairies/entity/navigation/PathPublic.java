package com.smanzana.nostrumfairies.entity.navigation;

import java.util.List;

import net.minecraft.world.level.pathfinder.Path;
import net.minecraft.world.level.pathfinder.Node;
import net.minecraft.core.BlockPos;

public class PathPublic extends Path {

	protected List<Node> pathpoints;
	
	public PathPublic(List<Node> pathpoints, BlockPos target, boolean flagged) {
		super(pathpoints, target, flagged);
		this.pathpoints = pathpoints;
	}
	
	public List<Node> getPathPoints() {
		return pathpoints;
	}

}
