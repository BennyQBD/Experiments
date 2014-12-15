#ifndef I_TEXTURE_INCLUDED_H
#define I_TEXTURE_INCLUDED_H

#include <stdexcept>

class ITexture
{
public:
	enum
	{
		FILTER_NEAREST_NO_MIPMAP,
		FILTER_NEAREST_NEAREST_MIPMAP,
		FILTER_NEAREST_LINEAR_MIPMAP,
		FILTER_LINEAR_NO_MIPMAP,
		FILTER_LINEAR_NEAREST_MIPMAP,
		FILTER_LINEAR_LINEAR_MIPMAP
	};

	enum
	{
		FORMAT_R,
		FORMAT_RG,
		FORMAT_RGB,
		FORMAT_RGBA,
		FORMAT_DEPTH,
		FORMAT_DEPTH_AND_STENCIL
	};

	virtual ~ITexture() {}

	virtual void Bind(unsigned int samplerSlot) = 0;
	virtual int GetWidth() = 0;
	virtual int GetHeight() = 0;

	class Exception : public std::runtime_error
	{
	public:
		Exception(const std::string& error) :
			std::runtime_error(error) {}
	};
	
	class Error : public std::logic_error
	{
	public:
		Error(const std::string& error) :
			std::logic_error(error) {}
	};
};

#endif
