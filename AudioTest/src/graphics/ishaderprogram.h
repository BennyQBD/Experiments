#ifndef I_SHADER_PROGRAM_INCLUDED_H
#define I_SHADER_PROGRAM_INCLUDED_H

#include "materialvalues.h"
#include "camera.h"

#include "../core/math3d.h"
#include <stdexcept>

struct UniformData
{
	Transform* transform;
	Camera* camera;
	MaterialValues* material;
	RendererValues* renderData;
};

class IShaderProgram
{
public:
	virtual ~IShaderProgram() {}
	virtual void Bind() = 0;
	virtual void UpdateUniforms(const UniformData& uniformData) = 0;

	class Exception : public std::runtime_error
	{
	public:
		Exception(const std::string& error) :
			std::runtime_error(error) {}
	};
};

#endif
