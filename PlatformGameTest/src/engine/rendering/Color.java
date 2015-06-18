package engine.rendering;

public class Color {
	public static final Color WHITE = new Color(1.0, 1.0, 1.0);
	
	private static final int ARGB_COMPONENT_BITS = 8;
	private static final int ARGB_COMPONENT_MASK = (1 << ARGB_COMPONENT_BITS) - 1;
	private static final int ARGB_NUM_COMPONENTS = 4;
	
	private double red;
	private double green;
	private double blue;
	
	public Color(double red, double green, double blue) {
		this.red = red;
		this.green = green;
		this.blue = blue;
	}
	
	public static int makeARGB(int a, int r, int g, int b) {
		return ((a & Color.ARGB_COMPONENT_MASK) << Color.getComponentShift(0))
				| ((r & Color.ARGB_COMPONENT_MASK) << Color.getComponentShift(1))
				| ((g & Color.ARGB_COMPONENT_MASK) << Color.getComponentShift(2))
				| (b & Color.ARGB_COMPONENT_MASK) << Color.getComponentShift(3);
	}

	public static int makeARGB(int r, int g, int b) {
		return Color.makeARGB(Color.ARGB_COMPONENT_MASK, r, g, b);
	}

	public static int makeARGB(double a, double r, double g, double b) {
		return Color.makeARGB(Color.doubleToComponent(a), Color.doubleToComponent(r),
				Color.doubleToComponent(g), Color.doubleToComponent(b));
	}

	public static int makeARGB(double r, double g, double b) {
		return Color.makeARGB(1.0, r, g, b);
	}
	
	private static int doubleToComponent(double c) {
		return (int) (c * Color.ARGB_COMPONENT_MASK + 0.5);
	}

	private static int getComponentShift(int component) {
		return (Color.ARGB_COMPONENT_BITS * (Color.ARGB_NUM_COMPONENTS - component - 1));
	}

	public double getRed() {
		return red;
	}

	public double getGreen() {
		return green;
	}

	public double getBlue() {
		return blue;
	}
}
