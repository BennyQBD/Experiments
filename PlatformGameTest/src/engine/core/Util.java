package engine.core;

public class Util {
	public static double saturate(double val) {
		return clamp(val, 0.0, 1.0);
	}

	public static double clamp(double val, double min, double max) {
		if(val < min) {
			val = min;
		} else if(val > max) {
			val = max;
		}
		return val;
	}

	public static int floorMod(int num, int den) {
		if(den < 0) {
			throw new IllegalArgumentException(
					"floorMod does not currently support negative" +
					"denominators");
		}
		if(num > 0) {
			return num % den;
		} else {
			int mod = (-num) % den;
			if(mod != 0) {
				mod = den - mod;
			}
			return mod;
		}
	}

	public static void boundsCheck(int index, int min, int max) {
		if(index > max) {
			throw new IndexOutOfBoundsException(index + " is more than " + max);
		}
		if(index < min) {
			throw new IndexOutOfBoundsException(index + " is less than " + min);
		}
	}

//	public static int floorDiv(int num, int den) {
//		if(num > 0) {
//			return num / den;
//		} else {
//			int floor = -((-num)/den);
//			int mod = (-num) % den;
//			if(mod != 0) {
//				floor--;
//			}
//			return floor;
//		}
//	}
//
}
