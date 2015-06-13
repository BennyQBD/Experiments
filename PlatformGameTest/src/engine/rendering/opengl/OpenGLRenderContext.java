package engine.rendering.opengl;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL30.*;

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
	private final int scaledWidth;
	private final int scaledHeight;
	private SpriteSheet boundTex;
	private OpenGLLightMap lightMap;
	private OpenGLBitmap softTex;
	private int lightMapTexId;

	public OpenGLRenderContext(int width, int height, int scaledWidth,
			int scaledHeight) {
		this.width = width;
		this.height = height;
		this.scaledWidth = scaledWidth;
		this.scaledHeight = scaledHeight;
		this.boundTex = null;
		lightMap = new OpenGLLightMap(width, height, 1);
		lightMap.clear();

		OpenGLUtil.bindRenderTarget(0, this.width, this.height,
				this.scaledWidth, this.scaledHeight);
		glDisable(GL_DEPTH_TEST);
		glEnable(GL_TEXTURE_2D);

		glEnable(GL_BLEND);
		glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);

		initTexture();
	}

	@Override
	public void dispose() {
		softTex.dispose();
		glDeleteTextures(lightMapTexId);
	}

	private void initTexture() {
		softTex = new OpenGLBitmap(256, 256);

		this.lightMapTexId = glGenTextures();
		glBindTexture(GL_TEXTURE_2D, lightMapTexId);

		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP);
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP);

		glTexParameterf(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
		glTexParameterf(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);

		glTexImage2D(GL_TEXTURE_2D, 0, GL_INTENSITY8, width, height, 0,
				GL_RGBA, GL_UNSIGNED_BYTE, (ByteBuffer) null);
	}

	private boolean bind(SpriteSheet sheet) {
		int id = sheet.getSheet().getHardwareID();
		if (id != -1) {
			glBindTexture(GL_TEXTURE_2D, id);
			return true;
		} else {
			glBindTexture(GL_TEXTURE_2D, softTex.getHardwareID());
		}

		if (sheet == boundTex) {
			return false;
		}
		IBitmap image = sheet.getSheet();
		int width = image.getWidth();
		int height = image.getHeight();

		boolean realloc = false;
		int newWidth = softTex.getWidth();
		int newHeight = softTex.getHeight();
		if (width > newWidth) {
			realloc = true;
			newWidth = width;
		}

		if (height > newHeight) {
			realloc = true;
			newHeight = height;
		}

		if (realloc) {
			softTex.dispose();
			softTex = new OpenGLBitmap(newWidth, newHeight);
		}

		softTex.setPixels(image.getPixels(null), 0, 0, width, height);
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
		OpenGLUtil.bindRenderTarget(0, this.width, this.height,
				this.scaledWidth, this.scaledHeight);
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

		double texScaleX = (double) (sheet.getSheet().getWidth())
				/ softTex.getWidth();
		double texScaleY = (double) (sheet.getSheet().getHeight())
				/ softTex.getHeight();

		if (!isInHardware) {
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

		OpenGLUtil.bindRenderTarget(0, this.width, this.height,
				this.scaledWidth, this.scaledHeight);
		glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
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
		lightMap.clear();
	}

	@Override
	public void drawLight(OpenGLLightMap light, int x, int y, int mapStartX,
			int mapStartY, int width, int height) {
		lightMap.addLight(light, x, y, mapStartX, mapStartY, width, height);
	}

	@Override
	public void applyLighting(double ambientLightAmt) {
		// double ambientLightScale = (1.0 - ambientLightAmt);
		// ambientLightAmt = ambientLightAmt * 255.0 + 0.5;
		// ByteBuffer buffer = BufferUtils.createByteBuffer(height * width * 4);
		// for (int j = 0; j < height; j++) {
		// for (int i = 0; i < width; i++) {
		// double lightAmt = lightMap.getLight(i, j) * ambientLightScale
		// + ambientLightAmt;
		// buffer.put((byte) ((int) lightAmt));
		// buffer.put((byte) ((int) lightAmt));
		// buffer.put((byte) ((int) lightAmt));
		// buffer.put((byte) ((int) lightAmt));
		// }
		// }
		// buffer.flip();
		//
		// bindAsRenderTarget();
		// glBindTexture(GL_TEXTURE_2D, lightMapTexId);
		// glTexSubImage2D(GL_TEXTURE_2D, 0, 0, 0, width, height, GL_RGBA,
		// GL_UNSIGNED_BYTE, buffer);
		// glBlendFunc(GL_DST_COLOR, GL_ZERO);
		// glBegin(GL_QUADS);
		// {
		// glTexCoord2f(0, 0);
		// glVertex2f(0, 0);
		// glTexCoord2f(0, 1);
		// glVertex2f(0, height);
		// glTexCoord2f(1, 1);
		// glVertex2f(width, height);
		// glTexCoord2f(1, 0);
		// glVertex2f(width, 0);
		// }
		// glEnd();

		OpenGLUtil.bindRenderTarget(0, this.width, this.height,
				this.scaledWidth, this.scaledHeight);
		glBlendFunc(GL_DST_COLOR, GL_ZERO);
		OpenGLUtil.drawRect(lightMap.getId(), 0, 0, width, height, 0, 0, 1, 1);
	}
}
