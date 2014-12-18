#ifndef BASIC_SCENE_INCLUDED_H
#define BASIC_SCENE_INCLUDED_H

#include "core/iscene.h"
#include "graphics/ishaderprogram.h"
#include <vector>

class BasicScene : public IScene
{
public:
	BasicScene() {}
	virtual ~BasicScene();
	virtual void Update(IInput* input, float delta);
	virtual void Render(IRenderer* renderer);
	virtual IScene* Add(Entity* child);
private:
	std::vector<Entity*> m_entities;

	BasicScene(const BasicScene& other) { (void)other; }
	void operator=(const BasicScene& other) { (void)other; }
};

#endif
