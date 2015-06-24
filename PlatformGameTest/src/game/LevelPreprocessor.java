package game;

import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

public class LevelPreprocessor {
	// TODO: Find a clean way to not hardcode all the numbers
	private static boolean notWall(int c) {
		if(c == 0) {
			return true;
		}
		c &= 0xFF;
		return (c > 200 && c != 249 && c != 248 && c != 220 && c != 222
						&& c != 224 && c != 226 && c != 228);
	}

	private static int getPixel(int[] data, int i, int j, int width) {
		return data[i + j * width];
	}

	private static void setPixel(int[] data, int i, int j, int width, int value) {
		data[i + j * width] = value;
	}

	private static int processWall(int[] level, int i, int j, int width) {
		int b = 33;
		int bl = getPixel(level, i - 1, j, width) & 0xFFFFFF;
		int br = getPixel(level, i + 1, j, width) & 0xFFFFFF;
		int bt = getPixel(level, i, j - 1, width) & 0xFFFFFF;
		int bb = getPixel(level, i, j + 1, width) & 0xFFFFFF;

		if (notWall(bl)) {
			if (notWall(br)) {
				if (notWall(bt)) {
					if (notWall(bb)) {
						b = 21;
					} else {
						b = 20;
					}
				} else {
					if (notWall(bb)) {
						b = 35;
					} else {
						b = 37;
					}
				}
			} else {
				if (notWall(bt)) {
					if (notWall(bb)) {
						b = 19;
					} else {
						b = 3;
					}
				} else {
					if (notWall(bb)) {
						b = 48;
					} else {
						b = 32;
					}
				}
			}
		} else {
			if (notWall(br)) {
				if (notWall(bt)) {
					if (notWall(bb)) {
						b = 36;
					} else {
						b = 5;
					}
				} else {
					if (notWall(bb)) {
						b = 50;
					} else {
						b = 34;
					}
				}
			} else {
				if (notWall(bt)) {
					if (notWall(bb)) {
						b = 52;
					} else {
						b = 4;
					}
				} else {
					if (notWall(bb)) {
						b = 49;
					} else {
						b = 33;
					}
				}
			}
		}

		return b;
	}

	private static int processPlatform(int[] level, int i, int j, int width) {
		int b = 1;
		int bl = getPixel(level, i - 1, j, width) & 0xFFFFFF;
		int br = getPixel(level, i + 1, j, width) & 0xFFFFFF;
		int bb = getPixel(level, i, j + 1, width) & 0xFFFFFF;

		if (notWall(bl)) {
			if (notWall(br)) {
				if (notWall(bb)) {
					b = 53;
				} else {
					b = 51;
				}
			} else {
				if (notWall(bb)) {
					b = 16;
				} else {
					b = 0;
				}
			}
		} else {
			if (notWall(br)) {
				if (notWall(bb)) {
					b = 18;
				} else {
					b = 2;
				}
			} else {
				if (notWall(bb)) {
					b = 17;
				} else {
					b = 1;
				}
			}
		}

		return b;
	}

	public static void processLevel(String fileName,
			String swapFileName, String outputFormat) throws IOException {
		BufferedImage image = ImageIO.read(new File(fileName));
		ImageIO.write(image, outputFormat, new File(swapFileName));
		
		int width = image.getWidth();
		int height = image.getHeight();
		int[] level = new int[width * height];
		image.getRGB(0, 0, width, height, level, 0, width);

		for (int j = 1; j < height - 1; j++) {
			for (int i = 1; i < width - 1; i++) {
				int pixel = getPixel(level, i, j, width);
				int b = pixel & 0xFF;

				if (b == 33) {
					b = processWall(level, i, j, width);
				} else if (b == 1) {
					b = processPlatform(level, i, j, width);
				} else if(b == 17) {
					b = processPlatform(level, i, j, width);
				}

				pixel = (pixel & 0xFFFFFF00) | b;
				setPixel(level, i, j, width, pixel);
			}
		}
		
		BufferedImage output = new BufferedImage(width, height,
				BufferedImage.TYPE_INT_ARGB);
		int[] data = ((DataBufferInt) output.getRaster()
				.getDataBuffer()).getData();
		for(int i = 0; i < data.length; i++) {
			data[i] = level[i];
		}
		ImageIO.write(output, outputFormat, new File(fileName));
	}
}
