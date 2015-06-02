package engine.core;

import java.awt.event.KeyEvent;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.Comparator;
import java.util.TreeSet;
import java.util.Arrays;

import engine.rendering.IRenderContext;
import engine.rendering.IBitmap;
import engine.rendering.ArrayBitmap;
import engine.rendering.ARGBColor;
import engine.parsing.Config;

public class Scene {
	private ISpatialStructure<Entity> structure;

	private Entity player;
//	private IBitmap bricks;
//	private IBitmap sand;
	private IBitmap playerSprite;
	private void loadLevel(Config config, IInput input) throws Exception {
		IBitmap level = new ArrayBitmap("./res/" + config.getString("level.data"));
		BitmapFactory bitmaps = new BitmapFactory();

		IBitmap background = bitmaps.get("./res/simprock24.png");
		IBitmap background1 = bitmaps.get("./res/simprockborder.png");
//		IBitmap background2 = bitmaps.get("./res/simprockborder2.png");
		IBitmap background2 = bitmaps.get("./res/simprockborder3.png");
		IBitmap background3 = bitmaps.get("./res/simprockborder4.png");
		IBitmap background4 = bitmaps.get("./res/simprockborder5.png");
		IBitmap diamond = bitmaps.get("./res/diamond.png");
		IBitmap diamond2 = bitmaps.get("./res/diamond2.png");
		int tileSize = 16;
		for(int j = 0; j < level.getHeight(); j++) {
			for(int i = 0; i < level.getWidth(); i++) {
				int color = level.getPixel(i, j) & 0x00FFFFFF;
				int x = i*tileSize;
				int y = j*tileSize;
				if(((int)(Math.random() * 10)) % 10 != 0) {
					add(x, y, -1, false, background);
				} else {
					int randBack = ((int)(Math.random() * 4)) % 4;
					if(randBack == 0) {
						add(x, y, -1, false, background1);
					} else if(randBack == 1) {
						add(x, y, -1, false, background2);
					} else if(randBack == 2) {
						add(x, y, -1, false, background3);
					} else {
						add(x, y, -1, false, background4);
					}
				}
				if(color == 255) {
					player = new Entity(structure, x, y, 1, true);
					new SpriteComponent(player, playerSprite);
					new PlayerComponent(player,
							new InputListener(input, new int[] { KeyEvent.VK_LEFT }),
							new InputListener(input, new int[] { KeyEvent.VK_RIGHT }),
							new InputListener(input, new int[] { KeyEvent.VK_SHIFT }),
							new InputListener(input, new int[] { KeyEvent.VK_SPACE }),
							new InputListener(input, new int[] { KeyEvent.VK_DOWN }));
				} else if(color == 254) {
					Entity entity = new Entity(structure, x, y, 2, false);
					new CollectableComponent(entity, 10);
					new SpriteComponent(entity, diamond2);
				} else if(color == 253) {
					Entity entity = new Entity(structure, x, y, 2, false);
					new CollectableComponent(entity, 100);
					new SpriteComponent(entity, diamond);
				} else if(color == 250) {
					Entity entity = new Entity(structure, x, y, 1, true);
					new SpriteComponent(entity, playerSprite);
					new EnemyComponent(entity);
				} else if(color != 0) {
					String tileName = "level."+color;
					IBitmap tex = bitmaps.get("./res/" + config.getString(tileName+".tex"));
					boolean blocking = config.getBoolean(tileName+".blocking");
					double layer = config.getDouble(tileName+".layer");
					add(x, y, layer, blocking, tex).setDitherable(true);
				}
			}
		}
	}

	public Scene(Config config, IInput input) {
		//this.structure = new QuadTree<Entity>(new AABB(0, 0, 256*16, 256*16), 8);
		this.structure = new Grid<Entity>(16, 256, 256);
		try {
//			bricks = new ArrayBitmap("./res/simprockplatform2.png");
			//bricks = new ArrayBitmap("./res/simprock.png");
//			sand = new ArrayBitmap("./res/simprock.png");
			playerSprite = new ArrayBitmap("./res/playertest.png");
			//playerSprite = new ArrayBitmap("./res/simprock.png");

			loadLevel(config, input);
		} catch(Exception e) {
//			bricks = sand = playerSprite = null;
			e.printStackTrace();
		}

//		int TILE_SIZE = 16;
//		int gridHeight = 256;
//		int gridWidth  = 256;
//		for(int j = 0; j < gridHeight; j++) {
//			for(int i = 0; i < gridWidth; i++) {
//				int index = i + j * gridWidth;
//				//if(index % 23 == 0 || index % 33 == 0) {
//				int rand = (int)(Math.random()*10);
//				if(rand == 0) {
//					add(i * TILE_SIZE, j * TILE_SIZE, 0, true, bricks);
//				} else if(rand != 1) {
//					add(i * TILE_SIZE, j * TILE_SIZE, 0, false, sand);
//				}
//			}
//		}
//
//		player = add(gridWidth/2 * TILE_SIZE, gridHeight/2 * TILE_SIZE, 1, true, playerSprite);
	}

	public Set<Entity> queryRange(Set<Entity> result, AABB aabb) {
		return structure.queryRange(result, aabb);
	}

	public Entity add(double posX, double posY, double posZ, 
			boolean isBlocking, IBitmap sprite) {
		Entity result = new Entity(structure, posX, posY, posZ, isBlocking);
		new SpriteComponent(result, sprite);
		return result;
	}

	public void remove(Entity entity) {
		structure.remove(entity);
	}

	public void update(double delta) {
		Set<Entity> entities = structure.queryRange(
				new HashSet<Entity>(),
				player.getAABB().expand(200, 200, 0));

		Iterator<Entity> it = entities.iterator();
		while (it.hasNext()) {
			Entity current = (Entity) it.next();
			current.update(delta);
		}
	}
	
	private void drawBackground(IRenderContext target, 
			double r, double g, double b, 
			int parallax, int x, int y) {
		int width = target.getWidth();
		int height = target.getHeight();
		for(int j = 0; j < height; j++) {
			int adjustedJ = Util.floorMod(j+(y/parallax), height);
			double jFract = ((double)(adjustedJ)/(double)(height-1));
//			jFract *= 2.0 * Math.PI;
//			jFract = Math.sin(jFract);
//			jFract = (jFract + 1.0)/2.0;
			jFract *= 2.0;
			if(jFract > 1.0) {
				jFract = 1.0 - (jFract - 1.0);
			}
			int color = ARGBColor.makeColor(jFract*r, jFract*g, jFract*b);

			for(int i = 0; i < width; i++) {
				target.drawPixel(i, j, color);
			}
		}
		
	}

	public void render(IRenderContext target) {
		//target.clear((byte)0x00);
		double viewportOffsetX = Math.round((target.getWidth()-player.getAABB().getWidth())/2);
		double viewportOffsetY = Math.round((target.getHeight()-player.getAABB().getHeight())/2);
//		double viewportX = player.getAABB().getMinX() - 7*16;
//		double viewportY = player.getAABB().getMinY() - 6*16;
		double viewportX = player.getAABB().getMinX() - viewportOffsetX;
		double viewportY = player.getAABB().getMinY() - viewportOffsetY;


		drawBackground(target, 1.0, 0.0, 0.0, 4, 
				(int)Math.round(viewportX), (int)Math.round(viewportY));

		Comparator<Entity> spriteSorter = new Comparator<Entity>() {
			public int compare(Entity e0, Entity e1) {
				if (e0.getAABB().getMinZ() > e1.getAABB().getMinZ()) {
					return 1;
				}
				if (e0.getAABB().getMinZ() < e1.getAABB().getMinZ()) {
					return -1;
				}
				return 0;

			}
		};

		Set<Entity> renderableEntities = structure.queryRange(
				new HashSet<Entity>(),
				new AABB(viewportX, viewportY, 
					viewportX + target.getWidth(), 
					viewportY + target.getHeight()));
		
		Entity[] entities = renderableEntities.toArray(
				new Entity[renderableEntities.size()]);
		Arrays.sort(entities, spriteSorter);
//		Debug.log(renderableEntities.size());

		for(int i = 0; i < entities.length; i++) {
			Entity entity = entities[i];
			
			entity.render(target, 
					(int)Math.round(viewportX),
					(int)Math.round(viewportY));
		}
	}
}

