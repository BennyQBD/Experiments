#include "opengl3texture.h"
#include <sstream>
#include <cassert>

static GLfloat GetOpenGLFilter(int filter)
{
	switch(filter)
	{
		case ITexture::FILTER_NEAREST_NO_MIPMAP:
			return GL_NEAREST;
		case ITexture::FILTER_NEAREST_NEAREST_MIPMAP:
			return GL_NEAREST_MIPMAP_NEAREST;
		case ITexture::FILTER_NEAREST_LINEAR_MIPMAP:
			return GL_NEAREST_MIPMAP_LINEAR;
		case ITexture::FILTER_LINEAR_NO_MIPMAP:
			return GL_LINEAR;
		case ITexture::FILTER_LINEAR_NEAREST_MIPMAP:
			return GL_LINEAR_MIPMAP_NEAREST;
		case ITexture::FILTER_LINEAR_LINEAR_MIPMAP:
			return GL_LINEAR_MIPMAP_LINEAR;
		default:
			std::ostringstream out;
			out << "Invalid texture filter: " << filter;
			throw ITexture::Error(out.str());
	};
}

static GLint GetOpenGLFormat(int format, bool compress)
{
	switch(format)
	{
		case ITexture::FORMAT_R:
			return GL_RED;
		case ITexture::FORMAT_RG:
			return GL_RG;
		case ITexture::FORMAT_RGB:
			if(!compress)
			{
				return GL_RGB;
			}
			else
			{
				return GL_COMPRESSED_RGB_S3TC_DXT1_EXT;
			}
		case ITexture::FORMAT_RGBA:
			if(!compress)
			{
				return GL_RGBA;
			}
			else
			{
				return GL_COMPRESSED_RGBA_S3TC_DXT3_EXT;
			}
		case ITexture::FORMAT_DEPTH:
			return GL_DEPTH_COMPONENT;
		case ITexture::FORMAT_DEPTH_AND_STENCIL:
			return GL_DEPTH_STENCIL;
		default:
			std::ostringstream out;
			out << "Invalid texture format: " << format;
			throw ITexture::Error(out.str());
	}
}

OpenGL3Texture::OpenGL3Texture(int width, int height, unsigned char* data, 
			int filterIn, float anisotropy, int internalFormatIn, 
			int formatIn, bool clamp, bool compress)
{
	GLfloat filter = GetOpenGLFilter(filterIn);
	GLint format = GetOpenGLFormat(formatIn, false);
	GLint internalFormat = GetOpenGLFormat(internalFormatIn, compress);
	GLenum textureTarget = GL_TEXTURE_2D;
	GLuint textureHandle;

	glGenTextures(1, &textureHandle);
	glBindTexture(textureTarget, textureHandle);
			
	glTexParameterf(textureTarget, GL_TEXTURE_MIN_FILTER, filter);
	glTexParameterf(textureTarget, GL_TEXTURE_MAG_FILTER, filter);
		
	if(clamp)
	{
		glTexParameterf(textureTarget, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
		glTexParameterf(textureTarget, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);
	}
		
	glTexImage2D(textureTarget, 0, internalFormat, width, height, 0, format, GL_UNSIGNED_BYTE, data);
		
	if(filter == GL_NEAREST_MIPMAP_NEAREST ||
		filter == GL_NEAREST_MIPMAP_LINEAR ||
		filter == GL_LINEAR_MIPMAP_NEAREST ||
		filter == GL_LINEAR_MIPMAP_LINEAR)
	{
		glGenerateMipmap(textureTarget);

		if(anisotropy > 0.0f)
		{
			GLfloat maxAnisotropy;
			glGetFloatv(GL_MAX_TEXTURE_MAX_ANISOTROPY_EXT, &maxAnisotropy);
			glTexParameterf(textureTarget, GL_TEXTURE_MAX_ANISOTROPY_EXT, 
				anisotropy < maxAnisotropy ? anisotropy : maxAnisotropy);
		}
	}
	else
	{
		glTexParameteri(textureTarget, GL_TEXTURE_BASE_LEVEL, 0);
		glTexParameteri(textureTarget, GL_TEXTURE_MAX_LEVEL, 0);
	}

	m_width = width;
	m_height = height;
	m_textureID = textureHandle;
}

OpenGL3Texture::~OpenGL3Texture()
{
	glDeleteTextures(1, &m_textureID);
}

void OpenGL3Texture::Bind(unsigned int samplerSlot)
{
	assert(samplerSlot >= 0 && samplerSlot <= 31);
	glActiveTexture(GL_TEXTURE0 + samplerSlot);
	glBindTexture(GL_TEXTURE_2D, m_textureID);
}

int OpenGL3Texture::GetWidth()
{
	return m_width;
}

int OpenGL3Texture::GetHeight()
{
	return m_height;
}
