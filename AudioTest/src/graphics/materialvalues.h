#ifndef MATERIAL_VALUES_INCLUDED_H
#define MATERIAL_VALUES_INCLUDED_H

#include "itexture.h"
#include "../core/math3d.h"
#include <map>
#include <stdexcept>

class MaterialValues
{
public:
	inline void SetVector3f(const std::string& name, const Vector3f& value)     { m_vector3fMap[name] = value; }
	inline void SetMatrix4f(const std::string& name, const Matrix4f& value)     { m_matrix4fMap[name] = value; }
	inline void SetFloat(const std::string& name, float value)                  { m_floatMap[name] = value; }
	inline void SetTexture(const std::string& name, ITexture* value) { m_textureMap[name] = value; }
	
	const Vector3f& GetVector3f(const std::string& name)     const;
	const Matrix4f& GetMatrix4f(const std::string& name)     const;
	float GetFloat(const std::string& name)                  const;
	ITexture* GetTexture(const std::string& name) const;

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
protected:
private:
	std::map<std::string, Vector3f>      m_vector3fMap;
	std::map<std::string, Matrix4f>      m_matrix4fMap;
	std::map<std::string, float>         m_floatMap;
	std::map<std::string, ITexture*> m_textureMap;
};

class RendererValues : public MaterialValues
{
public:
	inline void SetSamplerSlot(const std::string& name, unsigned int value) { m_samplerMap[name] = value; }

	unsigned int GetSamplerSlot(const std::string& name) const { return m_samplerMap.find(name)->second; }
private:
	std::map<std::string, unsigned int> m_samplerMap;
};


#endif
