package engine.rendering;

public class ARGBColor {
	private static final int COMPONENT_BITS = 8;
	private static final int COMPONENT_MASK = (1 << COMPONENT_BITS) - 1;
	private static final int NUM_COMPONENTS = 4;

	private static byte doubleToComponent(double c) {
		return (byte)(((int)(c * COMPONENT_MASK + 0.5)) & COMPONENT_MASK);
	}

	public static int makeColor(double r, double g, double b) {
		return makeColor(
				doubleToComponent(r),
				doubleToComponent(g),
				doubleToComponent(b));
	}

	public static int makeColor(byte r, byte g, byte b) {
		return makeColor((byte)0xFF, r, g, b);
	}

	public static int makeColor(byte a, byte r, byte g, byte b) {
		return ((a & COMPONENT_MASK) << getComponentShift(0)) | 
			((r & COMPONENT_MASK) << getComponentShift(1)) | 
			((g & COMPONENT_MASK) << getComponentShift(2)) | 
			(b & COMPONENT_MASK) << getComponentShift(3);
	}

	public static byte getComponent(int color, int component) {
		if(component >= NUM_COMPONENTS || component < 0) {
			throw new IllegalArgumentException(
					"Component must be in range of 0-" + NUM_COMPONENTS);
		}

		return (byte)(
				(color >> getComponentShift(component)) & COMPONENT_MASK);
	}

	private static int getComponentShift(int component) {
		return (COMPONENT_BITS * (NUM_COMPONENTS - component - 1));
	}
}
