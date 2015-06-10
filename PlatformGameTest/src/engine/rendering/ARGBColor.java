package engine.rendering;

public class ARGBColor {
	private static final int COMPONENT_BITS = 8;
	private static final int COMPONENT_MASK = (1 << COMPONENT_BITS) - 1;
	private static final int NUM_COMPONENTS = 4;

	private static int doubleToComponent(double c) {
		return (int) (c * COMPONENT_MASK + 0.5);
	}

	public static int makeColor(double r, double g, double b) {
		return makeColor(1.0, r, g, b);
	}

	public static int makeColor(double a, double r, double g, double b) {
		return makeColor(doubleToComponent(a), doubleToComponent(r),
				doubleToComponent(g), doubleToComponent(b));
	}

	public static int makeColor(int r, int g, int b) {
		return makeColor(COMPONENT_MASK, r, g, b);
	}

	public static int makeColor(int a, int r, int g, int b) {
		return ((a & COMPONENT_MASK) << getComponentShift(0))
				| ((r & COMPONENT_MASK) << getComponentShift(1))
				| ((g & COMPONENT_MASK) << getComponentShift(2))
				| (b & COMPONENT_MASK) << getComponentShift(3);
	}

	public static int getComponent(int color, int component) {
		if (component >= NUM_COMPONENTS || component < 0) {
			throw new IllegalArgumentException(
					"Component must be in range of 0-" + NUM_COMPONENTS);
		}

		return (color >> getComponentShift(component)) & COMPONENT_MASK;
	}

	private static int getComponentShift(int component) {
		return (COMPONENT_BITS * (NUM_COMPONENTS - component - 1));
	}
	
	public static double getComponentd(int color, int component) {
		return (double)(getComponent(color, component)/255.0);
	}
}
