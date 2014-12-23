#ifndef ENTITY_COMPONENT_INCLUDED_H
#define ENTITY_COMPONENT_INCLUDED_H

#include "../core/entity.h"

class EntityComponent
{
public:
	EntityComponent() :
		m_parent(0) {}
	virtual ~EntityComponent() {}

	virtual void Update(EngineSystems& systems, float delta) {}
	virtual void Render(RenderParams& params) {}
	
	inline Transform* GetTransform()             { return m_parent->GetTransform(); }
	virtual void SetParent(Entity* parent) { m_parent = parent; }
private:
	Entity* m_parent;
	
	EntityComponent(const EntityComponent& other) { (void)other; }
	void operator=(const EntityComponent& other) { (void)other; }
};


#endif
