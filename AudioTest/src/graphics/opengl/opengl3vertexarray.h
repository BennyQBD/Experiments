#ifndef OPENGL_3_VERTEX_ARRAY_INCLUDED_H
#define OPENGL_3_VERTEX_ARRAY_INCLUDED_H

#include "../ivertexarray.h"

class OpenGL3VertexArray : public IVertexArray
{
public:
	virtual ~OpenGL3VertexArray();
	OpenGL3VertexArray(const unsigned int vertexArrayObject, 
			const unsigned int* buffers, const unsigned int numBuffers, 
			const unsigned int numElements) :
		m_vertexArrayObject(vertexArrayObject),
		m_buffers(buffers),
		m_numBuffers(numBuffers),
		m_numElements(numElements) {}

	inline unsigned int GetVAO() const         { return m_vertexArrayObject; }
	inline const unsigned int* GetBuffers() const { return m_buffers; }
	inline unsigned int GetNumBuffers() const     { return m_numBuffers; }
	inline unsigned int GetNumElements() const    { return m_numElements; }
private:
	const unsigned int  m_vertexArrayObject;
	const unsigned int* m_buffers;
	const unsigned int  m_numBuffers;
	const unsigned int  m_numElements;

	OpenGL3VertexArray(OpenGL3VertexArray& other) :
		m_vertexArrayObject(0),
		m_buffers(0),
		m_numBuffers(0),
		m_numElements(0) {(void)other;}
	void operator=(const OpenGL3VertexArray& other) { (void)other;}
};

#endif
