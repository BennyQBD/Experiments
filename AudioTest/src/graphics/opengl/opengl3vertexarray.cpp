#include "opengl3vertexarray.h"
#include <GL/glew.h>

OpenGL3VertexArray::~OpenGL3VertexArray()
{
	GLuint vao = this->GetVAO();
	glDeleteVertexArrays(1, &vao);
	glDeleteBuffers(this->GetNumBuffers(), this->GetBuffers());
	delete[] this->GetBuffers();
}
