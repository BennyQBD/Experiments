#ifndef ENTITY_INCLUDED_H
#define ENTITY_INCLUDED_H

#include "transform.h"
#include "iinput.h"
#include "../graphics/renderparams.h"

#include <vector>

class EntityComponent;

class Entity
{
public:
	Entity(const Vector3f& pos = Vector3f(0,0,0), 
			const Quaternion& rot = Quaternion(0,0,0,1), float scale = 1.0f) :
		m_transform(pos, rot, scale) {}
	
	virtual ~Entity();

	Entity* Add(EntityComponent* component);
	
	void Update(IInput* input, float delta);
	void Render(RenderParams& params);

	inline Transform* GetTransform() { return &m_transform; }
private:
	std::vector<EntityComponent*> m_components;
	Transform m_transform;

	Entity(const Entity& other) { (void)other; }
	void operator=(const Entity& other) { (void)other; }
};

#endif
