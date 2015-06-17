package engine.util.components;

import engine.core.entity.Entity;
import engine.core.entity.EntityComponent;
import engine.util.IDAssigner;

public class RemoveComponent extends EntityComponent {
	public enum Type {
		FADE
	};
	public static final int ID = IDAssigner.getId();
	
	private boolean activated;
	private Type type;
	private double timer;
	private double param1;
	private double param2;
	private double param3;
	
	public RemoveComponent(Entity entity, Type type, double param1, double param2, double param3) {
		super(entity, ID);
		this.activated = false;
		this.type = type;
		this.param1 = param1;
		this.param2 = param2;
		this.param3 = param3;
		this.timer = 0.0;
	}
	
	public void activate() {
		activated = true;
		switch(type) {
		case FADE:
			int animationFrame = (int)param2;
			SpriteComponent sc = (SpriteComponent)getEntity().getComponent(SpriteComponent.ID);
			if(sc != null && animationFrame != -1) {
				sc.setFrame(animationFrame);
			}
			break;
		}
	}
	
	@Override
	public void update(double delta) {
		if(!activated) {
			return;
		}
		timer += delta;
		switch(type) {
		case FADE:
			if(timer >= param3) {
				getEntity().setBlocking(false);
			}
			if(timer >= param1) {
				getEntity().forceRemove();
			}
			
			double amt = (param1 - timer)/param1;
			SpriteComponent sc = (SpriteComponent)getEntity().getComponent(SpriteComponent.ID);
			if(sc != null) {
				sc.setTransparency(amt);
			}
			break;
		}
	}
}
