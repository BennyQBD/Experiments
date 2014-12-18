#ifndef BASIC_RENDERER_INCLUDED_H
#define BASIC_RENDERER_INCLUDED_H

#include "graphics/irenderer.h"

class BasicRenderer : public IRenderer
{
public:
	BasicRenderer(IRenderContext* context, IRenderTarget* target, 
			IShaderProgram* shader, Camera* camera,
			RendererValues* rendererValues)
	{
		m_params.context = context;
		m_params.target = target;
		m_params.shader = shader;
		m_params.camera = camera;
		m_params.renderValues = rendererValues;
	}
	
	virtual void Render(const std::vector<Entity*>& entities)
	{	
		m_params.context->ClearScreen(m_params.target, 0.0f, 0.0f, 0.0f, 0.0f);
		m_params.context->ClearDepth(m_params.target);
		for(std::vector<Entity*>::const_iterator it = entities.begin(); 
				it != entities.end(); ++it)
		{
			(*it)->Render(m_params);
		}
	}
private:
	RenderParams m_params;
};

#endif
