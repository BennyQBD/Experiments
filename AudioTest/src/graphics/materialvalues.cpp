#include "materialvalues.h"
#include <sstream>

void MaterialValues::SetTexture(const std::string& name, const Texture& value)
{
	std::map<std::string, Texture>::iterator it = m_textureMap.find(name);
	if(it != m_textureMap.end())
	{
		it->second = value;
	}
	else
	{
		m_textureMap.insert(std::pair<std::string, Texture>(name, value));
	}
}

const Vector3f& MaterialValues::GetVector3f(const std::string& name) const
{
	std::map<std::string, Vector3f>::const_iterator it = m_vector3fMap.find(name);
	if(it != m_vector3fMap.end())
	{
		return it->second;
	}
	
	std::ostringstream out;
	out << "Error: " << name << " is not a Vector3f in this material";
	throw Exception(out.str());
}

const Matrix4f& MaterialValues::GetMatrix4f(const std::string& name) const
{
	std::map<std::string, Matrix4f>::const_iterator it = m_matrix4fMap.find(name);
	if(it != m_matrix4fMap.end())
	{
		return it->second;
	}
		
	std::ostringstream out;
	out << "Error: " << name << " is not a Matrix4f in this material";
	throw Exception(out.str());
}

float MaterialValues::GetFloat(const std::string& name) const
{
	std::map<std::string, float>::const_iterator it = m_floatMap.find(name);
	if(it != m_floatMap.end())
	{
		return it->second;
	}
		
	std::ostringstream out;
	out << "Error: " << name << " is not a float in this material";
	throw Exception(out.str());
}

ITexture* MaterialValues::GetTexture(const std::string& name)
{
	std::map<std::string, Texture>::iterator it = m_textureMap.find(name);
	if(it != m_textureMap.end())
	{
		return (it->second).GetTexture();
	}
		
	std::ostringstream out;
	out << "Error: " << name << " is not a Texture in this material";
	throw Exception(out.str());
}

