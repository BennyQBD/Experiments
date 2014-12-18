#ifndef RENDER_PARAMS_INCLUDED_H
#define RENDER_PARAMS_INCLUDED_H

#include "ishaderprogram.h"
#include "irendertarget.h"
#include "irendercontext.h"

struct RenderParams
{
	IRenderContext* context;
	IRenderTarget* target;
	IShaderProgram* shader; 
	RendererValues* renderValues;
	Camera* camera;
};

#endif
