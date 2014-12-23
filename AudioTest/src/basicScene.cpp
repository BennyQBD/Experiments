#include "basicScene.h"

void BasicScene::Update(EngineSystems& systems, float delta)
{
	for(unsigned int i = 0; i < m_entities.size(); i++)
	{
		m_entities[i]->Update(systems, delta);
	}
}

BasicScene::~BasicScene()
{
	for(unsigned int i = 0; i < m_entities.size(); i++)
	{
		if(m_entities[i]) { delete m_entities[i]; }
	}
}

void BasicScene::Render(IRenderer* renderer)
{
	renderer->Render(m_entities);
}

IScene* BasicScene::Add(Entity* child)
{
	m_entities.push_back(child);
	return this;
}

