#ifndef OPENGL_3_TEXTURE_INCLUDED_H
#define OPENGL_3_TEXTURE_INCLUDED_H

#include "../itexture.h"
#include <GL/glew.h>

class OpenGL3Texture : public ITexture
{
public:
	OpenGL3Texture(int width, int height, unsigned char* data, 
			int filter, float anisotropy, int internalFormat, 
			int format, bool clamp);
	virtual ~OpenGL3Texture();

	virtual void Bind(unsigned int samplerSlot);
	virtual int GetWidth();
	virtual int GetHeight();
private:
	GLuint m_textureID;
	int m_width;
	int m_height;

	OpenGL3Texture(OpenGL3Texture& other) { (void)other; }
	void operator=(const OpenGL3Texture& other) { (void)other;}
};

#endif
