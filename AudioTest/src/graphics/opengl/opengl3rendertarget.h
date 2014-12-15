#ifndef OPENGL_3_RENDER_TARGET_INCLUDED_H
#define OPENGL_3_RENDER_TARGET_INCLUDED_H

#include "../irendertarget.h"

class OpenGL3RenderTarget : public IRenderTarget
{
public:
	OpenGL3RenderTarget(unsigned int index, int width, int height) :
		m_index(index),
		m_width(width),
		m_height(height) {}

	virtual void Bind();
private:
	unsigned int m_index;
	int          m_width;
	int          m_height;
};

#endif
