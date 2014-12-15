#ifndef OPENGL_3_RENDER_CONTEXT_INCLUDED_H
#define OPENGL_3_RENDER_CONTEXT_INCLUDED_H

#include "../irendercontext.h"

class OpenGL3RenderContext : public IRenderContext
{
public:
	OpenGL3RenderContext();
	virtual void ClearScreen(
			IRenderTarget* target, float r, float g, float b, float a);
	virtual void ClearDepth(IRenderTarget* target); 

	virtual void DrawVertexArray(IRenderTarget* target, 
			IShaderProgram* program, IVertexArray* vertexArray, 
			const UniformData& uniforms);
};

#endif
