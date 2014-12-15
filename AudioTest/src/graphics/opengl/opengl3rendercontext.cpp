#include "opengl3rendercontext.h"
#include "opengl3vertexarray.h"
#include <GL/glew.h>

OpenGL3RenderContext::OpenGL3RenderContext()
{
	glClearColor(0.0f, 0.0f, 0.0f, 0.0f);

	glFrontFace(GL_CW);
	glCullFace(GL_BACK);
	glEnable(GL_CULL_FACE);
	glEnable(GL_DEPTH_TEST);
}

void OpenGL3RenderContext::ClearScreen(IRenderTarget* target, 
		float r, float g, float b, float a)
{
	target->Bind();
	glClearColor(r, g, b, a);
	glClear(GL_COLOR_BUFFER_BIT);
}

void OpenGL3RenderContext::ClearDepth(IRenderTarget* target)
{
	target->Bind();
	glClear(GL_DEPTH_BUFFER_BIT);
}


void OpenGL3RenderContext::DrawVertexArray(IRenderTarget* target, 
			IShaderProgram* program, IVertexArray* vertexArray, 
			const UniformData& uniforms)
{
	target->Bind();
	program->Bind();
	program->UpdateUniforms(uniforms);

	// TODO: If there is a better way to do this, let me know.
	OpenGL3VertexArray* array = (OpenGL3VertexArray*)vertexArray;
	glBindVertexArray(array->GetVAO());
	glDrawElements(GL_TRIANGLES, (GLsizei)array->GetNumElements(), GL_UNSIGNED_INT, 0);
}

