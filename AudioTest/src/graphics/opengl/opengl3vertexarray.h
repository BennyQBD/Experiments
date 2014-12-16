#ifndef OPENGL_3_VERTEX_ARRAY_INCLUDED_H
#define OPENGL_3_VERTEX_ARRAY_INCLUDED_H

#include "../ivertexarray.h"

class OpenGL3VertexArray : public IVertexArray
{
public:
	virtual ~OpenGL3VertexArray();
	OpenGL3VertexArray(float** vertexData, unsigned int* vertexElementSizes,
			unsigned int numVertexComponents, unsigned int numVertices,
			unsigned int* indices, unsigned int numIndices);

	inline unsigned int GetVAO() const         { return m_vertexArrayObject; }
	inline const unsigned int* GetBuffers() const { return m_buffers; }
	inline unsigned int GetNumBuffers() const     { return m_numBuffers; }
	inline unsigned int GetNumElements() const    { return m_numElements; }
private:
	unsigned int  m_vertexArrayObject;
	unsigned int* m_buffers;
	unsigned int  m_numBuffers;
	unsigned int  m_numElements;

	OpenGL3VertexArray(OpenGL3VertexArray& other) :
		m_vertexArrayObject(0),
		m_buffers(0),
		m_numBuffers(0),
		m_numElements(0) {(void)other;}
	void operator=(const OpenGL3VertexArray& other) { (void)other;}
};

#endif
