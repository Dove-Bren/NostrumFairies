package com.smanzana.nostrumfairies.entity.navigation;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.google.common.collect.Lists;
import com.smanzana.nostrumfairies.utils.Paths;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.pathfinder.Path;
import net.minecraft.world.level.pathfinder.Node;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class CompositePath extends Path {

	private final List<Path> paths;
	private int compoundPathIndex;
	
	// Path cached stuff
	private int cachedTotalLength;
	private int cachedCurrIndex;
	private Path cachedCurrPath;
	private int cachedCurrPathIndex;
	
	public CompositePath(List<Path> paths) {
		super(new ArrayList<>(), paths.get(paths.size() - 1).getTarget(), paths.get(paths.size() - 1).canReach());
		this.paths = fixPaths(paths);
		compoundPathIndex = 0;
		
		refreshCache(true);
	}
	
	private List<Path> fixPaths(List<Path> inputs) {
		// The pathfinding code treats two points next to eachother as blashphemous.
		// Each path should have a end point that matches the start point of the previous.
		List<Path> output = new ArrayList<>(inputs.size());
		output.add(inputs.get(0));
		int i = 1;
		while (i < inputs.size()) {
			output.add(Paths.TrimStart(inputs.get(i)));
			i++;
		}
		
		return output;
	}
	
	public CompositePath(Collection<Path> paths) {
		this(Lists.newArrayList(paths));
	}
	
	protected void refreshCache(boolean changedLength) {
		if (changedLength) {
			cachedTotalLength = calcTotalPathLength(paths);
		}
		
		if (changedLength || cachedCurrIndex != compoundPathIndex) {
			this.cachedCurrIndex = compoundPathIndex;
			PathAndIndex adjusted = calcPathForIndex(paths, cachedCurrIndex);
			this.cachedCurrPath = adjusted == null ? null : adjusted.path;
			this.cachedCurrPathIndex = adjusted == null ? -1 : adjusted.pathIndex;
		}
	}
	
	protected static PathAndIndex calcPathForIndex(List<Path> paths, int compoundIndex) {
		int pathIndex = 0;
		while (pathIndex < paths.size()) {
			Path path = paths.get(pathIndex);
			if (path.getNodeCount() > compoundIndex) {
				return new PathAndIndex(path, compoundIndex, pathIndex);
			}
			pathIndex++;
			compoundIndex -= path.getNodeCount();
		}
		
		return null;
	}
	
	protected static int calcTotalPathLength(List<Path> paths) {
		int totalLength = 0;
		
		for (Path path : paths) {
			totalLength += path.getNodeCount();
		}
		
		return totalLength;
	}
	
	public Path getCurrentPath() {
		refreshCache(false);
		return cachedCurrPath;
	}
	
	public BlockPos getSegmentStart() {
		// We trim off the start, so we have to reconstruct it
		refreshCache(false);
		if (cachedCurrPathIndex - 1 >= 0) {
			Node point = paths.get(cachedCurrPathIndex-1).getNode(0);
			return new BlockPos(point.x, point.y, point.z);
		}
		
		return null;
	}
	
	public BlockPos getSegmentEnd() {
		Path segment = getCurrentPath();
		if (segment != null) {
			Node point = segment.getEndNode();
			return new BlockPos(point.x, point.y, point.z);
		}
		
		return null;
	}
	
	/**
	 * Directs this path to the next point in its array
	 */
	public void advance() {
		final Path curr = getCurrentPath();
		if (curr != null) {
			curr.advance();
			compoundPathIndex++;
		}
	}

	/**
	 * Returns true if this path has reached the end
	 */
	public boolean isDone() {
		refreshCache(false);
		return cachedCurrPath == null;
	}

	/**
	 * returns the last PathPoint of the Array
	 */
	public Node getEndNode() {
		return this.paths.size() > 0 ? paths.get(paths.size() - 1).getEndNode() : null;
	}

	/**
	 * return the PathPoint located at the specified PathIndex, usually the current one
	 */
	public Node getNode(int index) {
		if (index == cachedCurrIndex) {
			return cachedCurrPath.getNode(cachedCurrPath.getNextNodeIndex());
		}
		
		PathAndIndex adjusted = calcPathForIndex(paths, index);
		return adjusted.path.getNode(adjusted.index);
	}

	public void replaceNode(int index, Node point) {
		if (index == cachedCurrIndex) {
			cachedCurrPath.replaceNode(cachedCurrPath.getNextNodeIndex(), point);
		}
		
		PathAndIndex adjusted = calcPathForIndex(paths, index);
		adjusted.path.replaceNode(adjusted.index, point);
	}

	public int getNodeCount() {
		return this.cachedTotalLength;
	}

	public void truncateNodes(int length) {
		throw new RuntimeException("Can't change path length of compositep path");
	}

	public int getNextNodeIndex() {
		return this.compoundPathIndex;
	}

	public void setNextNodeIndex(int currentPathIndexIn) {
		if (currentPathIndexIn != this.compoundPathIndex) {
			if (currentPathIndexIn > this.compoundPathIndex) {
				// Jumping forward
				for (int i = currentPathIndexIn - this.compoundPathIndex; i > 0; i--) {
					this.advance();
				}
				this.compoundPathIndex = currentPathIndexIn;
				return;
			}
			
			// For all paths before it, set index to max
			// For all paths after it, set index to 0
			// for path it's on, set to actual
			int indicies = currentPathIndexIn;
			for (Path p : this.paths) {
				if (indicies == 0) {
					// This is a path after
					p.setNextNodeIndex(0);
				} else if (p.getNodeCount() > indicies) {
					// This is the path where we are actually at
					p.setNextNodeIndex(indicies);
					indicies = 0;
				} else {
					p.setNextNodeIndex(p.getNodeCount());
					indicies -= p.getNodeCount();
				}
			}
			
			this.compoundPathIndex = currentPathIndexIn;
		}
	}

	/**
	 * Gets the vector of the PathPoint associated with the given index.
	 */
	public Vec3 getEntityPosAtNode(Entity entityIn, int index) {
		if (index == cachedCurrIndex) {
			return cachedCurrPath.getEntityPosAtNode(entityIn, cachedCurrPath.getNextNodeIndex());
		}
		
		PathAndIndex adjusted = calcPathForIndex(paths, index);
		return adjusted.path.getEntityPosAtNode(entityIn, adjusted.index);
	}

	/**
	 * returns the current PathEntity target node as Vector3d
	 */
	public Vec3 getNextEntityPos(Entity entityIn) {
		return this.getEntityPosAtNode(entityIn, this.compoundPathIndex);
	}

	public Vec3 getCurrentPos() {
		Node pathpoint = this.getNode(compoundPathIndex);
		return new Vec3((double)pathpoint.x, (double)pathpoint.y, (double)pathpoint.z);
	}

	/**
	 * Returns true if the EntityPath are the same. Non instance related equals.
	 */
	public boolean sameAs(Path pathentityIn) {
		if (pathentityIn == null || !(pathentityIn instanceof CompositePath)) {
			return false;
		}
		
		CompositePath other = (CompositePath) pathentityIn;
		if (other.paths.size() != this.paths.size()) {
			return false;
		}
		
		for (int i = 0; i < paths.size(); i++) {
			if (!paths.get(i).sameAs(other.paths.get(i))) {
				return false;
			}
		}

		return true;
	}

	@OnlyIn(Dist.CLIENT)
	@Override
	public Node[] getOpenSet() {
		refreshCache(false);
		return this.cachedCurrPath.getOpenSet();
	}

	@OnlyIn(Dist.CLIENT)
	@Override
	public Node[] getClosedSet() {
		refreshCache(false);
		return this.cachedCurrPath.getClosedSet();
	}

	@OnlyIn(Dist.CLIENT)
	@Override
	public BlockPos getTarget() { // getTarget
		refreshCache(false);
		return this.cachedCurrPath.getTarget();
	}
	
	
	protected static final class PathAndIndex {
		public final Path path;
		public final int index;
		public final int pathIndex;
		
		public PathAndIndex(Path path, int index, int pathIndex) {
			this.path = path;
			this.index = index;
			this.pathIndex = pathIndex;
		}
	}
	
}
