#ifndef I_RENDER_CONTEXT_INCLUDED_H
#define I_RENDER_CONTEXT_INCLUDED_H

#include "irendertarget.h"
#include "ivertexarray.h"
#include "ishaderprogram.h"

class IRenderContext
{
public:
	virtual ~IRenderContext() {}
	virtual void ClearScreen(
			IRenderTarget* target, float r, float g, float b, float a) = 0;
	virtual void ClearDepth(IRenderTarget* target) = 0;

	virtual void DrawVertexArray(IRenderTarget* target, 
			IShaderProgram* program, IVertexArray* vertexArray, 
			const UniformData& uniforms) = 0;
};

#endif
