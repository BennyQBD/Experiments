package engine.core;

import engine.rendering.IRenderContext;

public abstract class EntityComponent {
	private Entity entity;
	private String name;

	public EntityComponent(Entity entity, String name) {
		this.name = name;
		this.entity = entity;
		entity.add(this);
	}

	public String getName() {
		return name;
	}

	public Entity getEntity() {
		return entity;
	}

	public void update(double delta) {
	}

	public void render(IRenderContext target, int viewportX, int viewportY) {
	}
}
