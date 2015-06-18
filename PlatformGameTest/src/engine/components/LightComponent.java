package engine.components;

import engine.core.entity.Entity;
import engine.core.entity.EntityComponent;
import engine.rendering.Color;
import engine.rendering.IRenderContext;
import engine.rendering.LightMap;
import engine.space.AABB;
import engine.util.IDAssigner;

public class LightComponent extends EntityComponent {
	public static final int ID = IDAssigner.getId();
	private LightMap light;
	private Color color;

	public LightComponent(Entity entity, LightMap light) {
		super(entity, ID);
		this.light = light;
		int halfWidth = light.getWidth() / 2;
		int halfHeight = light.getHeight() / 2;
		entity.fitAABB(new AABB(-halfWidth, -halfHeight, halfWidth, halfHeight));
		this.color = Color.WHITE;
	}

	@Override
	public void render(IRenderContext target, double viewportX, double viewportY) {
		target.drawLight(light,
				getEntity().getAABB().getMinX() - viewportX,
				getEntity().getAABB().getMinY() - viewportY,
				0, 0, light.getWidth(), light.getHeight(), color);
	}

	public void setIntensity(double amt) {
		this.color = new Color(amt, amt, amt);
	}
}
