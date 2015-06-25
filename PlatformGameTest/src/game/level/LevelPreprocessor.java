package game.level;

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
		c = c & 0xFF;
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
		int r = 33;
		int rl = getPixel(level, i - 1, j, width) & 0xFFFFFF;
		int rr = getPixel(level, i + 1, j, width) & 0xFFFFFF;
		int rt = getPixel(level, i, j - 1, width) & 0xFFFFFF;
		int rb = getPixel(level, i, j + 1, width) & 0xFFFFFF;

		if (notWall(rl)) {
			if (notWall(rr)) {
				if (notWall(rt)) {
					if (notWall(rb)) {
						r = 21;
					} else {
						r = 20;
					}
				} else {
					if (notWall(rb)) {
						r = 35;
					} else {
						r = 37;
					}
				}
			} else {
				if (notWall(rt)) {
					if (notWall(rb)) {
						r = 19;
					} else {
						r = 3;
					}
				} else {
					if (notWall(rb)) {
						r = 48;
					} else {
						r = 32;
					}
				}
			}
		} else {
			if (notWall(rr)) {
				if (notWall(rt)) {
					if (notWall(rb)) {
						r = 36;
					} else {
						r = 5;
					}
				} else {
					if (notWall(rb)) {
						r = 50;
					} else {
						r = 34;
					}
				}
			} else {
				if (notWall(rt)) {
					if (notWall(rb)) {
						r = 52;
					} else {
						r = 4;
					}
				} else {
					if (notWall(rb)) {
						r = 49;
					} else {
						r = 33;
					}
				}
			}
		}

		return r;
	}

	private static int processPlatform(int[] level, int i, int j, int width) {
		int r = 1;
		int rl = getPixel(level, i - 1, j, width) & 0xFFFFFF;
		int rr = getPixel(level, i + 1, j, width) & 0xFFFFFF;
		int rb = getPixel(level, i, j + 1, width) & 0xFFFFFF;

		if (notWall(rl)) {
			if (notWall(rr)) {
				if (notWall(rb)) {
					r = 53;
				} else {
					r = 51;
				}
			} else {
				if (notWall(rb)) {
					r = 16;
				} else {
					r = 0;
				}
			}
		} else {
			if (notWall(rr)) {
				if (notWall(rb)) {
					r = 18;
				} else {
					r = 2;
				}
			} else {
				if (notWall(rb)) {
					r = 17;
				} else {
					r = 1;
				}
			}
		}

		return r;
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
				int r = (pixel >> 16) & 0xFF;
				int b = (pixel) & 0xFF;
				if(b == 246) {
					continue;
				}

				if (r == 33) {
					r = processWall(level, i, j, width);
				} else if (r == 1) {
					r = processPlatform(level, i, j, width);
				} else if(r == 17) {
					r = processPlatform(level, i, j, width);
				} 
				
//				if((pixel & 0xFFFFFF) == 0 || b >= 200) {
//					continue;
//				}
//				
//				int newB = 1;
//				if(b == 14 || b == 30 || b == 46 || b == 62) {
//					newB = 2;
//				}
//				
//				pixel = (b << 16) | (pixel & 0xFF00FF00) | newB;

				pixel = (pixel & 0xFF00FFFF) | (r << 16);
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
