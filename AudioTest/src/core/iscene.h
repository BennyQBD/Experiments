#ifndef I_SCENE_INCLUDED_H
#define I_SCENE_INCLUDED_H

#include "enginesystems.h"
#include "entity.h"
#include "../graphics/irenderer.h"
#include "resourceManager.h"

class IScene
{
public:
	virtual ~IScene() {}
	virtual void Init(ResourceManager* resources, float aspect) = 0;
	virtual void Update(EngineSystems& systems, float delta) = 0;
	virtual void Render(IRenderer* renderer) = 0;
	virtual IScene* Add(Entity* child) = 0;
};

#endif
