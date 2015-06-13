package engine.util;

import engine.core.entity.Entity;
import engine.core.entity.EntityComponent;

public class LinkComponent extends EntityComponent {
	public static final String COMPONENT_NAME = "LinkComponent";
	private Entity linked;

	public LinkComponent(Entity entity, Entity toLink) {
		super(entity, COMPONENT_NAME);
		this.linked = toLink;
	}
	
	@Override
	public void update(double delta) {
		if(linked.getRemoved()) {
			getEntity().remove();
		}
	}
}
