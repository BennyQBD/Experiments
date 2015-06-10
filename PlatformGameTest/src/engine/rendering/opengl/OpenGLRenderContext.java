package engine.rendering.opengl;

import static org.lwjgl.opengl.GL11.*;

import java.nio.ByteBuffer;

import org.lwjgl.BufferUtils;

import engine.rendering.ARGBColor;
import engine.rendering.IBitmap;
import engine.rendering.IRenderContext;
import engine.rendering.LightMap;
import engine.rendering.SpriteSheet;
import engine.util.Util;

public class OpenGLRenderContext implements IRenderContext {
	private final int width;
	private final int height;
	private SpriteSheet boundTex;
	private int texId;
	private int texWidth;
	private int texHeight;

	public OpenGLRenderContext(int width, int height) {
		this.width = width;
		this.height = height;
		this.boundTex = null;

		glMatrixMode(GL_PROJECTION);
		glLoadIdentity();
		glOrtho(0, width, height, 0, 1, -1);
		glMatrixMode(GL_MODELVIEW);

		glDisable(GL_DEPTH_TEST);
		glEnable(GL_TEXTURE_2D);

		glEnable(GL_BLEND);
		glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);

		initTexture();
	}

	@Override
	public void dispose() {
		glDeleteTextures(texId);
	}

	private void initTexture() {
		this.texId = glGenTextures();
		glBindTexture(GL_TEXTURE_2D, texId);

		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_REPEAT);
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_REPEAT);

		glTexParameterf(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
		glTexParameterf(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);

		this.texWidth = 256;
		this.texHeight = 256;
		allocTex(null);
	}

	private void allocTex(ByteBuffer buffer) {
		glBindTexture(GL_TEXTURE_2D, texId);
		glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA8, texWidth, texHeight, 0,
				GL_RGBA, GL_UNSIGNED_BYTE, buffer);
	}

	private boolean bind(SpriteSheet sheet) {
		int id = sheet.getSheet().getHardwareAccelerationID();
		if (id != -1) {
			glBindTexture(GL_TEXTURE_2D, id);
			return true;
		} else {
			glBindTexture(GL_TEXTURE_2D, texId);
		}

		if (sheet == boundTex) {
			return false;
		}
		IBitmap image = sheet.getSheet();
		ByteBuffer buffer = BufferUtils.createByteBuffer(image.getHeight()
				* image.getWidth() * 4);

		int[] pixels = image.getPixels(null, 0, 0, image.getWidth(), image.getHeight());
		for (int i = 0; i < pixels.length; i++) {
			int pixel = pixels[i];
			buffer.put((byte) ((pixel >> 16) & 0xFF));
			buffer.put((byte) ((pixel >> 8) & 0xFF));
			buffer.put((byte) ((pixel) & 0xFF));
			buffer.put((byte) ((pixel >> 24) & 0xFF));
		}

		buffer.flip();
		int width = image.getWidth();
		int height = image.getHeight();

		boolean realloc = false;
		if (width > texWidth) {
			realloc = true;
			texWidth = width;
		}

		if (height > texHeight) {
			realloc = true;
			texHeight = height;
		}

		if (realloc) {
			allocTex(buffer);
		} else {
			glTexSubImage2D(GL_TEXTURE_2D, 0, 0, 0, image.getWidth(),
					image.getHeight(), GL_RGBA, GL_UNSIGNED_BYTE, buffer);
		}

		boundTex = sheet;
		return false;
	}

	@Override
	public int getWidth() {
		return width;
	}

	@Override
	public int getHeight() {
		return height;
	}

	@Override
	public void clear(double a, double r, double g, double b) {
		glClearColor((float) r, (float) g, (float) b, (float) a);
		glClear(GL_COLOR_BUFFER_BIT);
	}

	@Override
	public void drawSprite(SpriteSheet sheet, int index, int x, int y,
			double transparency, boolean flipX, boolean flipY, int colorMask) {
		int width = sheet.getSpriteWidth();
		int height = sheet.getSpriteHeight();
		double texMinX = ((double) sheet.getStartX(index) / ((double) sheet
				.getSheet().getWidth()));
		double texMinY = ((double) sheet.getStartY(index) / ((double) sheet
				.getSheet().getHeight()));
		double texMaxX = texMinX + ((double) sheet.getSpriteWidth())
				/ ((double) sheet.getSheet().getWidth());
		double texMaxY = texMinY + ((double) sheet.getSpriteHeight())
				/ ((double) sheet.getSheet().getHeight());

		boolean isInHardware = bind(sheet);

		double texScaleX = (double) (sheet.getSheet().getWidth()) / texWidth;
		double texScaleY = (double) (sheet.getSheet().getHeight()) / texHeight;

		if(!isInHardware) {
			texMinX *= texScaleX;
			texMinY *= texScaleY;
			texMaxX *= texScaleX;
			texMaxY *= texScaleY;
		}

		if (flipX) {
			double temp = texMinX;
			texMinX = texMaxX;
			texMaxX = temp;
		}

		if (flipY) {
			double temp = texMinY;
			texMinY = texMaxY;
			texMaxY = temp;
		}

		glColor4f((float) ARGBColor.getComponentd(colorMask, 1),
				(float) ARGBColor.getComponentd(colorMask, 2),
				(float) ARGBColor.getComponentd(colorMask, 3),
				(float) transparency);

		glBegin(GL_QUADS);
		{
			glTexCoord2f((float) texMinX, (float) texMinY);
			glVertex2f(x, y);
			glTexCoord2f((float) texMinX, (float) texMaxY);
			glVertex2f(x, y + height);
			glTexCoord2f((float) texMaxX, (float) texMaxY);
			glVertex2f(x + width, y + height);
			glTexCoord2f((float) texMaxX, (float) texMinY);
			glVertex2f(x + width, y);
		}
		glEnd();
	}

	@Override
	public int drawString(String str, SpriteSheet font, int x, int y,
			int color, int wrapX) {
		int maxLength = (wrapX - x) / font.getSpriteWidth();
		if (wrapX <= x || wrapX <= 0 || str.length() < maxLength) {
			drawStringLine(str, font, x, y, color);
			return font.getSpriteHeight();
		}
		int yStart = y;
		str = Util.wrapString(str, maxLength);
		String[] strs = str.split("\n");
		for (int i = 0; i < strs.length; i++) {
			String[] wrappedStrings = strs[i].split("(?<=\\G.{" + maxLength
					+ "})");
			for (int j = 0; j < wrappedStrings.length; j++, y += font
					.getSpriteHeight()) {
				drawStringLine(wrappedStrings[j], font, x, y, color);
			}
		}
		return y - yStart;
	}

	private void drawStringLine(String str, SpriteSheet font, int x, int y,
			int color) {
		for (int i = 0; i < str.length(); i++, x += font.getSpriteWidth()) {
			char c = str.charAt(i);
			drawSprite(font, (int) c, x, y, 1.0, false, false, color);
		}
	}

	@Override
	public void clearLighting() {
		// TODO Auto-generated method stub

	}

	@Override
	public void drawLight(LightMap light, int x, int y, int mapStartX,
			int mapStartY, int width, int height) {
		// TODO Auto-generated method stub

	}

	@Override
	public void applyLighting(double ambientLightAmt) {
		// TODO Auto-generated method stub

	}
}
