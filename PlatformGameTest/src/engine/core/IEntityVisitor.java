package engine.core;

public interface IEntityVisitor {
	public void visit(Entity entity, EntityComponent component);
}
