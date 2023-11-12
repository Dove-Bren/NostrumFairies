package com.smanzana.nostrumfairies.utils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.google.common.collect.Lists;
import com.smanzana.nostrumfairies.entity.navigation.PathPublic;

import net.minecraft.pathfinding.Path;
import net.minecraft.pathfinding.PathPoint;
import net.minecraft.util.math.BlockPos;

public final class Paths {
	
	public static Path TrimStart(Path path) {
		List<PathPoint> points = new ArrayList<PathPoint>(path.getCurrentPathLength() - 1);
		// NOTE: start at 1 to chop of 'start' which is usually in a block lol
		for (PathPoint point : path.func_215746_d()) {
			points.add(new PathPoint(point.x, point.y, point.z));
		}
		
		return new Path(points, path.func_224770_k(), path.func_224771_h());
	}

	/**
	 * Create one big path with all the individual points of the provided paths.
	 * @param paths
	 * @return
	 */
	public static Path Combine(Path ... paths) {
		return Combine(Lists.newArrayList(paths));
	}
	
	public static Path Combine(Collection<Path> paths) {
		ArrayList<PathPoint> points = new ArrayList<>(paths.size() * 5); // estimate
		boolean first = true;
		BlockPos finalDest = null;
		boolean finalFlagged = false;
		for (Path path : paths) {
			// NOTE: start at 1 to chop of 'start' which is usually in a block lol
			for (int i = first ? 0 : 1; i < path.getCurrentPathLength(); i++) {
				// isn't this dumb? lol. Wish there was .getPathPoints();
				PathPoint point = path.getPathPointFromIndex(i);
				points.add(new PathPoint(point.x, point.y, point.z));
			}
			first = false;
			finalDest = path.func_224770_k();
			finalFlagged = path.func_224771_h();
		}
		
		return new Path(points, finalDest, finalFlagged);
	}
	
	/**
	 * Checks if the path provided actaully reached the intended destination
	 * @param path
	 * @return
	 */
	public static boolean IsComplete(Path path, BlockPos target, int maxDistance) {
		PathPoint end = path.getFinalPathPoint();
		return target.distanceSq(end.x, end.y, end.z, true) <= Math.pow(maxDistance, 2); // 0 or 1 blocks away, please!
	}
	
	public static PathPublic ClonePath(Path path) {
		if (path == null) {
			return null;
		}
		
		if (path instanceof PathPublic) {
			return new PathPublic(((PathPublic) path).getPathPoints(), path.func_224770_k(), path.func_224771_h());
		}
		
		int size = path.getCurrentPathLength();
		List<PathPoint> points = new ArrayList<>(size);
		for (int i = 0; i < size; i++) {
			points.add(path.getPathPointFromIndex(i));
		}
		
		return new PathPublic(points, path.func_224770_k(), path.func_224771_h());
	}
	
}
