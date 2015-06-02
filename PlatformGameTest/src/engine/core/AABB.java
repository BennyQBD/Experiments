package engine.core;

public class AABB {
	private static final double DEFAULT_MIN_Z = Double.MIN_VALUE;
	private static final double DEFAULT_MAX_Z = Double.MAX_VALUE;

	private final double minX;
	private final double minY;
	private final double minZ;
	private final double maxX;
	private final double maxY;
	private final double maxZ;

	public AABB(double minX, double minY, double maxX, double maxY) {
		this(minX, minY, DEFAULT_MIN_Z, maxX, maxY);
	}

	public AABB(double minX, double minY, double minZ, double maxX, double maxY) {
		this(minX, minY, minZ, maxX, maxY, DEFAULT_MAX_Z);
	}

	public AABB(double minX, double minY, double minZ,
			double maxX, double maxY, double maxZ) {
		this.minX = minX;
		this.minY = minY;
		this.minZ = minZ;
		this.maxX = maxX;
		this.maxY = maxY;
		this.maxZ = maxZ;
	}

	public boolean intersects(AABB other) {
		return intersectCube(other.getMinX(), other.getMinY(), other.getMinZ(),
				other.getMaxX(), other.getMaxY(), other.getMaxZ());
	}

	public boolean intersectRect(double minX, double minY,
			double maxX, double maxY) {
		return intersectCube(minX, minY, DEFAULT_MIN_Z,
				maxX, maxY, DEFAULT_MAX_Z);
	}

	public boolean intersectCube(double minX, double minY, double minZ,
			double maxX, double maxY, double maxZ) {
		return 
			this.minX < maxX && this.maxX > minX &&
			this.minY < maxY && this.maxY > minY &&
			this.minZ < maxZ && this.maxZ > minZ;
	}

	public double resolveCollisionX(AABB other, double moveAmtX) {
		double newAmtX = moveAmtX;
		if(moveAmtX == 0.0) {
			return moveAmtX;
		}
		if(moveAmtX > 0) {
			// Our max == their min
			newAmtX = other.getMinX() - maxX;
		} else {
			// Our min == their max
			newAmtX = other.getMaxX() - minX;
		}

		if(Math.abs(newAmtX) < Math.abs(moveAmtX)) {
			moveAmtX = newAmtX;
		}
		return moveAmtX;
	}

	public double resolveCollisionY(AABB other, double moveAmtY) {
		double newAmtY = moveAmtY;
		if(moveAmtY == 0.0) {
			return moveAmtY;
		}
		if(moveAmtY > 0) {
			// Our max == their min
			newAmtY = other.getMinY() - maxY;
		} else {
			// Our min == their max
			newAmtY = other.getMaxY() - minY;
		}

		if(Math.abs(newAmtY) < Math.abs(moveAmtY)) {
			moveAmtY = newAmtY;
		}
		return moveAmtY;
	}

	public double getCenterX() {
		return (minX + maxX) / 2.0f;
	}

	public double getCenterY() {
		return (minY + maxY) / 2.0f;
	}

	public double getCenterZ() {
		return (minZ + maxZ) / 2.0f;
	}

	public double getMinX() {
		return minX;
	}

	public double getMinY() {
		return minY;
	}

	public double getMinZ() {
		return minZ;
	}

	public double getWidth() {
		return maxX - minX;
	}

	public double getHeight() {
		return maxY - minY;
	}

	public double getDepth() {
		return maxZ - minZ;
	}

	public double getMaxX() {
		return maxX;
	}

	public double getMaxY() {
		return maxY;
	}

	public double getMaxZ() {
		return maxZ;
	}

	@Override
	public String toString() {
		return 
			"(" + minX + ", " + getWidth() + "), " + 
			"(" + minY + ", " + getHeight() + "), " + 
			"(" + minZ + ", " + getDepth() + ")";
	}

	public AABB expand(double width, double height, double depth) {
		return new AABB(minX - width, minY - height, minZ - depth, 
				maxX + width, maxY + height, maxZ + depth);
	}

	public AABB move(double amtX, double amtY) {
		return new AABB(
				minX + amtX, minY + amtY, 
				maxX + amtX, maxY + amtY);
	}

	public AABB stretch(double amtX, double amtY) {
		double minX, maxX, minY, maxY;
		if(amtX < 0) {
			minX = this.minX + amtX;
			maxX = this.maxX;
		} else {
			minX = this.minX;
			maxX = this.maxX + amtX;
		}
		if(amtY < 0) {
			minY = this.minY + amtY;
			maxY = this.maxY;
		} else {
			minY = this.minY;
			maxY = this.maxY + amtY;
		}
		return new AABB(minX, minY, maxX, maxY);
	}
}
