#ifndef I_RENDERER_INCLUDED_H
#define I_RENDERER_INCLUDED_H

#include "../core/entity.h"

class IRenderer
{
public:
	virtual ~IRenderer() {}
	virtual void Render(const std::vector<Entity*>& entities) = 0;
};

#endif
