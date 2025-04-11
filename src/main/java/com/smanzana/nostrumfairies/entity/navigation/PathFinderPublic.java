package com.smanzana.nostrumfairies.entity.navigation;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.annotation.Nullable;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.pathfinder.Target;
import net.minecraft.world.level.pathfinder.NodeEvaluator;
import net.minecraft.world.level.pathfinder.Path;
import net.minecraft.world.level.pathfinder.PathFinder;
import net.minecraft.world.level.pathfinder.BinaryHeap;
import net.minecraft.world.level.pathfinder.Node;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.PathNavigationRegion;

/**
 * I WISH this just made some protected funcs public. But it does not!
 * Defers to vanilla for calls that were already public, and creates public wrappers AND COPIES THE IMPLEMENTATION
 * of the private ones.
 * I could use reflection, but i don't want to.
 * @author Skyler
 *
 */
public class PathFinderPublic extends PathFinder {

	public PathFinderPublic(NodeEvaluator processor, int maxAttempts) {
		super(processor, maxAttempts);
		this.nodeProcessor = processor;
		this.maxVisitedNodes = maxAttempts;
	}
	
	/*************** BEGIN COPIED STUFF ***************************/
	
	/** Selection of path points to add to the path */
	private final Node[] pathOptions = new Node[32];
	private final int maxVisitedNodes;
	private final NodeEvaluator nodeProcessor;
	/** The path being generated */
	private final BinaryHeap path = new BinaryHeap();

	@Nullable
	public Path findPath(PathNavigationRegion p_227478_1_, Mob p_227478_2_, Set<BlockPos> p_227478_3_, float p_227478_4_, int p_227478_5_, float p_227478_6_) {
		this.path.clear();
		this.nodeProcessor.prepare(p_227478_1_, p_227478_2_);
		Node pathpoint = this.nodeProcessor.getStart();
		Map<Target, BlockPos> map = p_227478_3_.stream().collect(Collectors.toMap((p_224782_1_) -> {
			return this.nodeProcessor.getGoal((double)p_224782_1_.getX(), (double)p_224782_1_.getY(), (double)p_224782_1_.getZ());
		}, Function.identity()));
		Path path = this.findPath(pathpoint, map, p_227478_4_, p_227478_5_, p_227478_6_);
		this.nodeProcessor.done();
		return path;
	}

	// If THIS were protected instead, this would all look a lot nicer.
	// (and the processor var)
	@Nullable
	public Path findPath(Node p_227479_1_, Map<Target, BlockPos> p_227479_2_, float p_227479_3_, int p_227479_4_, float p_227479_5_) {
		Set<Target> set = p_227479_2_.keySet();
		p_227479_1_.g = 0.0F;
		p_227479_1_.h = this.getBestH(p_227479_1_, set);
		p_227479_1_.f = p_227479_1_.h;
		this.path.clear();
		this.path.insert(p_227479_1_);
		Set<Node> set1 = ImmutableSet.of();
		int i = 0;
		Set<Target> set2 = Sets.newHashSetWithExpectedSize(set.size());
		int j = (int)((float)this.maxVisitedNodes * p_227479_5_);

		while(!this.path.isEmpty()) {
			++i;
			if (i >= j) {
				break;
			}

			Node pathpoint = this.path.pop();
			pathpoint.closed = true;

			for(Target flaggedpathpoint : set) {
				if (pathpoint.distanceManhattan(flaggedpathpoint) <= (float)p_227479_4_) {
					flaggedpathpoint.setReached();
					set2.add(flaggedpathpoint);
				}
			}

			if (!set2.isEmpty()) {
				break;
			}

			if (!(pathpoint.distanceTo(p_227479_1_) >= p_227479_3_)) {
				int k = this.nodeProcessor.getNeighbors(this.pathOptions, pathpoint);

				for(int l = 0; l < k; ++l) {
					Node pathpoint1 = this.pathOptions[l];
					float f = pathpoint.distanceTo(pathpoint1);
					pathpoint1.walkedDistance = pathpoint.walkedDistance + f;
					float f1 = pathpoint.g + f + pathpoint1.costMalus;
					if (pathpoint1.walkedDistance < p_227479_3_ && (!pathpoint1.inOpenSet() || f1 < pathpoint1.g)) {
						pathpoint1.cameFrom = pathpoint;
						pathpoint1.g = f1;
						pathpoint1.h = this.getBestH(pathpoint1, set) * 1.5F;
						if (pathpoint1.inOpenSet()) {
							this.path.changeCost(pathpoint1, pathpoint1.g + pathpoint1.h);
						} else {
							pathpoint1.f = pathpoint1.g + pathpoint1.h;
							this.path.insert(pathpoint1);
						}
					}
				}
			}
		}

		Optional<Path> optional = !set2.isEmpty() ? set2.stream().map((p_224778_2_) -> {
			return this.reconstructPath(p_224778_2_.getBestNode(), p_227479_2_.get(p_224778_2_), true);
		}).min(Comparator.comparingInt(Path::getNodeCount)) : set.stream().map((p_224777_2_) -> {
			return this.reconstructPath(p_224777_2_.getBestNode(), p_227479_2_.get(p_224777_2_), false);
		}).min(Comparator.comparingDouble(Path::getDistToTarget).thenComparingInt(Path::getNodeCount));
		return !optional.isPresent() ? null : optional.get();
	}

	private float getBestH(Node p_224776_1_, Set<Target> p_224776_2_) {
		float f = Float.MAX_VALUE;

		for(Target flaggedpathpoint : p_224776_2_) {
			float f1 = p_224776_1_.distanceTo(flaggedpathpoint);
			flaggedpathpoint.updateBest(f1, p_224776_1_);
			f = Math.min(f1, f);
		}

		return f;
	}

	private Path reconstructPath(Node p_224780_1_, BlockPos p_224780_2_, boolean p_224780_3_) {
		List<Node> list = Lists.newArrayList();
		Node pathpoint = p_224780_1_;
		list.add(0, p_224780_1_);

		while(pathpoint.cameFrom != null) {
			pathpoint = pathpoint.cameFrom;
			list.add(0, pathpoint);
		}

		return new Path(list, p_224780_2_, p_224780_3_);
	}
}
