#include "opengl3vertexarray.h"
#include <GL/glew.h>

OpenGL3VertexArray::OpenGL3VertexArray(float** vertexData, unsigned int* vertexElementSizes,
			unsigned int numVertexComponents, unsigned int numVertices,
			unsigned int* indices, unsigned int numIndices)
{
	unsigned int numBuffers = numVertexComponents + 1;

	GLuint VAO;
	GLuint* buffers = new GLuint[numBuffers];

	glGenVertexArrays(1, &VAO);
	glBindVertexArray(VAO);

	glGenBuffers(numBuffers, buffers);
	for(unsigned int i = 0; i < numVertexComponents; i++)
	{
		glBindBuffer(GL_ARRAY_BUFFER, buffers[i]);
		glBufferData(GL_ARRAY_BUFFER, 
				vertexElementSizes[i] * sizeof(float) * numVertices, 
				vertexData[i], GL_STATIC_DRAW);

		glEnableVertexAttribArray(i);
		glVertexAttribPointer(i, vertexElementSizes[i], GL_FLOAT, GL_FALSE, 0, 0);
	}
	
	glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, buffers[numVertexComponents]);
	glBufferData(GL_ELEMENT_ARRAY_BUFFER, numIndices * sizeof(unsigned int), indices, GL_STATIC_DRAW);

	m_vertexArrayObject = VAO;
	m_buffers = buffers;
	m_numBuffers = numBuffers;
	m_numElements = numIndices;
}

OpenGL3VertexArray::~OpenGL3VertexArray()
{
	GLuint vao = this->GetVAO();
	glDeleteVertexArrays(1, &vao);
	glDeleteBuffers(this->GetNumBuffers(), this->GetBuffers());
	delete[] this->GetBuffers();
}
