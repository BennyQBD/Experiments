package engine.rendering.awt;

import java.awt.BorderLayout;
import java.awt.Canvas;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.image.BufferStrategy;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;

import javax.swing.JFrame;
import javax.swing.JPanel;

import engine.audio.IAudioDevice;
import engine.input.IInput;
import engine.input.awt.AWTInput;
import engine.rendering.IDisplay;
import engine.rendering.IRenderContext;
import engine.rendering.IRenderDevice;
import engine.rendering.RenderContext;

public class AWTDisplay extends Canvas implements IDisplay {
	private static final long serialVersionUID = 1L;
	private final JFrame frame;
	private final RenderContext frameBuffer;
	private final BufferedImage displayImage;
	private final int[] displayComponents;
	private final BufferStrategy bufferStrategy;
	private final Graphics graphics;
	private final int scaledWidth;
	private final int scaledHeight;
	private final AWTInput input;
	private final IRenderDevice device;
	
	@Override
	public IAudioDevice getAudioDevice() {
		return null;
	}

	@Override
	public IRenderContext getRenderContext() {
		return frameBuffer;
	}
	
	@Override
	public IRenderDevice getRenderDevice() {
		return device;
	}

	@Override
	public IInput getInput() {
		return input;
	}

	@Override
	public boolean isClosed() {
		return !frame.isShowing();
	}

	@Override
	public void dispose() {
		frameBuffer.dispose();
		frame.dispose();
	}

	public AWTDisplay(int width, int height, double scale, String title) {
		this.scaledWidth = (int) (width * scale);
		this.scaledHeight = (int) (height * scale);
		Dimension size = new Dimension(scaledWidth, scaledHeight);
		setPreferredSize(size);
		setMinimumSize(size);
		setMaximumSize(size);

		this.device = new AWTRenderDevice(width, height, scaledWidth, scaledHeight);
		this.frameBuffer = new RenderContext(device);
		this.displayImage = new BufferedImage(width, height,
				BufferedImage.TYPE_INT_RGB);
		this.displayComponents = ((DataBufferInt) displayImage.getRaster()
				.getDataBuffer()).getData();

		this.frame = new JFrame();
		JPanel panel = new JPanel(new BorderLayout());
		panel.add(this, BorderLayout.CENTER);

		frame.setContentPane(panel);
		frame.pack();
		frame.setLocationRelativeTo(null);
		frame.setResizable(false);
		frame.setVisible(true);
		frame.setTitle(title);

		createBufferStrategy(1);
		this.bufferStrategy = getBufferStrategy();
		this.graphics = bufferStrategy.getDrawGraphics();
		
		this.input = new AWTInput();
		addKeyListener(input);
		addFocusListener(input);
		addMouseListener(input);
		addMouseMotionListener(input);

		setFocusable(true);
		requestFocus();
	}

	// private static final int[] dither = new int[]
	// {
	// 1, 49, 13, 61, 4, 52, 16, 64,
	// 33, 17, 45, 29, 36, 20, 48, 32,
	// 9, 57, 5, 53, 12, 60, 8, 56,
	// 41, 25, 37, 21, 44, 28, 40, 24,
	// 3, 51, 15, 63, 2, 50, 14, 62,
	// 35, 19, 47, 31, 34, 18, 46, 30,
	// 11, 59, 7, 55, 10, 58, 6, 54,
	// 43, 27, 39, 23, 42, 26, 38, 22,
	// };
	//
	// private static int getDither(int i, int j) {
	// return dither[(i & 7) + (j & 7) * 8];
	// }
	//
	// private static byte[] ditheredComponents = null;
	// private static void initDitheredComponents() {
	// if(ditheredComponents == null) {
	// ditheredComponents = new byte[256*64];
	// for(int d = 0; d < 64; d++) {
	// for(int c = 0; c < 256; c++) {
	// int newColor = (int)(c + d * (51.0/65.0));
	// if(newColor > 255) {
	// newColor = 255;
	// }
	// if(newColor < 0) {
	// newColor = 0;
	// }
	// int shade = (int)Math.floor((newColor / 255.0) * 5);
	// ditheredComponents[d * 256 + c] =
	// (byte)(shade * 255.0/5.0);
	// }
	// }
	// }
	// }
	//
	// private byte getDitheredComponent(byte c, int d) {
	// return ditheredComponents[(d-1) * 256 + (c & 0xFF)];
	// //return (byte)((int)Math.floor((c / 255.0) * 5) * 255.0/5.0);
	// }
	//
	// private void dither() {
	// for(int y = 0, i = 0; y < frameBuffer.getHeight(); y++) {
	// for(int x = 0; x < frameBuffer.getWidth(); x++, i++) {
	// int ditherAmt = getDither(x, y);
	// int color = displayComponents[i];
	// byte b = getDitheredComponent((byte)(color & 0xFF), ditherAmt);
	// byte g = getDitheredComponent((byte)((color >> 8) & 0xFF), ditherAmt);
	// byte r = getDitheredComponent((byte)((color >> 16) & 0xFF), ditherAmt);
	//
	// displayComponents[i] = ((r & 0xFF) << 16) | ((g & 0xFF) << 8) | (b &
	// 0xFF);
	// }
	// }
	// }

	public void swapBuffers() {
		frameBuffer.getPixels(displayComponents);
		// initDitheredComponents();
		// dither();
		graphics.drawImage(displayImage, 0, 0, scaledWidth, scaledHeight, null);
		bufferStrategy.show();
	}
}
