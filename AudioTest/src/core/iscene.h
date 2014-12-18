#ifndef I_SCENE_INCLUDED_H
#define I_SCENE_INCLUDED_H

#include "iinput.h"
#include "entity.h"
#include "../graphics/irenderer.h"
#include "../graphics/irenderdevice.h"

class IScene
{
public:
	virtual ~IScene() {}
	virtual void Init(IRenderDevice* device, float aspect) = 0;
	virtual void Update(IInput* input, float delta) = 0;
	virtual void Render(IRenderer* renderer) = 0;
	virtual IScene* Add(Entity* child) = 0;
};

#endif
