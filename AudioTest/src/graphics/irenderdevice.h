#ifndef I_RENDER_DEVICE_INCLUDED_H
#define I_RENDER_DEVICE_INCLUDED_H

#include "ivertexarray.h"
#include "ishaderprogram.h"
#include "itexture.h"
#include <stdexcept>

class IRenderDevice
{
public:
	virtual ~IRenderDevice() {}
	virtual IVertexArray* CreateVertexArray(
			float** vertexData, unsigned int* vertexElementSizes,
			unsigned int numVertexComponents, unsigned int numVertices,
			unsigned int* indices, unsigned int numIndices) = 0;
	virtual void ReleaseVertexArray(IVertexArray* vertexArray) = 0;

	virtual IShaderProgram* CreateShaderProgram(const std::string& shaderText)
	   	= 0;
	virtual IShaderProgram* CreateShaderProgramFromFile(
			const std::string& fileName) = 0;
	virtual void ReleaseShaderProgram(IShaderProgram* shaderProgram) = 0;

	virtual ITexture* CreateTexture(int width, int height, unsigned char* data, 
			int filter, float anisotropy, int internalFormat, 
			int format, bool clamp) = 0;

	virtual void ReleaseTexture(ITexture* texture) = 0;

	class Exception : public std::runtime_error
	{
	public:
		Exception(const std::string& text) :
			std::runtime_error(text) {}
	};

};

#endif
