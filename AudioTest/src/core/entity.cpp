#include "entity.h"
#include "../components/entityComponent.h"

Entity::~Entity()
{
	for(unsigned int i = 0; i < m_components.size(); i++)
	{
		if(m_components[i])
		{	
			delete m_components[i];
		}
	}
}

Entity* Entity::Add(EntityComponent* component)
{
	m_components.push_back(component);
	component->SetParent(this);
	return this;
}

void Entity::Update(EngineSystems& systems, float delta)
{
	for(unsigned int i = 0; i < m_components.size(); i++)
	{
		m_components[i]->Update(systems, delta);
	}
}

void Entity::Render(RenderParams& params)
{
	for(unsigned int i = 0; i < m_components.size(); i++)
	{
		m_components[i]->Render(params);
	}
}
