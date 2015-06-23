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
	private double offsetX;
	private double offsetY;

	public LightComponent(Entity entity, LightMap light, double offsetX, double offsetY) {
		super(entity, ID);
		this.light = light;
		int halfWidth = light.getWidth() / 2;
		int halfHeight = light.getHeight() / 2;
		entity.fitAABB(new AABB(-halfWidth, -halfHeight, halfWidth, halfHeight));
		this.color = Color.WHITE;
		this.offsetX = offsetX;
		this.offsetY = offsetY;
	}

	@Override
	public void render(IRenderContext target, double viewportX, double viewportY) {
		target.drawLight(light,
				getEntity().getX() - viewportX + offsetX,
				getEntity().getY() - viewportY + offsetY,
				0, 0, light.getWidth(), light.getHeight(), color);
	}

	public void setIntensity(double amt) {
		this.color = new Color(amt, amt, amt);
	}
}
