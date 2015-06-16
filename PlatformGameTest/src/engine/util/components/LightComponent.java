package engine.util.components;

import engine.core.entity.Entity;
import engine.core.entity.EntityComponent;
import engine.rendering.IRenderContext;
import engine.rendering.LightMap;
import engine.space.AABB;
import engine.util.IDAssigner;

public class LightComponent extends EntityComponent {
	public static final int ID = IDAssigner.getId();
	private LightMap light;

	public LightComponent(Entity entity, LightMap light) {
		super(entity, ID);
		this.light = light;
		int halfWidth = light.getWidth() / 2;
		int halfHeight = light.getHeight() / 2;
		entity.fitAABB(new AABB(-halfWidth, -halfHeight, halfWidth, halfHeight));
	}

	@Override
	public void render(IRenderContext target, int viewportX, int viewportY) {
		target.drawLight(light,
				(int) Math.round(getEntity().getAABB().getMinX()) - viewportX,
				(int) Math.round(getEntity().getAABB().getMinY()) - viewportY,
				0, 0, light.getWidth(), light.getHeight());
	}
}
