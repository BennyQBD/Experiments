#ifndef SHADER_INCLUDED_H
#define SHADER_INCLUDED_H

#include "../resourceManagement/resource.h"
#include "ishaderprogram.h"

class Shader : public Resource
{
public:
	inline IShaderProgram* GetShaderProgram() { return (IShaderProgram*)GetData(); }
};

#endif

