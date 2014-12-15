#ifndef OPENGL_3_SHADER_PROGRAM_INCLUDED_H
#define OPENGL_3_SHADER_PROGRAM_INCLUDED_H

#include "../ishaderprogram.h"
#include <GL/glew.h>
#include <string>
#include <vector>
#include <map>

class OpenGL3ShaderProgram : public IShaderProgram
{
public:
	OpenGL3ShaderProgram(const std::string& text, 
			const std::string& shaderVersion, bool loadFromFile);
	virtual ~OpenGL3ShaderProgram();
	virtual void Bind();
	virtual void UpdateUniforms(const UniformData& uniformData);

	void SetUniformi(const std::string& uniformName, int value) const;
	void SetUniformf(const std::string& uniformName, float value) const;
	void SetUniformMatrix4f(const std::string& uniformName, const Matrix4f& value) const;
	void SetUniformVector3f(const std::string& uniformName, const Vector3f& value) const;

	inline GLuint GetProgram()                                 const { return m_program; }
	inline const std::vector<GLuint>& GetShaders()             const { return m_shaders; }
	inline const std::vector<std::string>& GetUniformNames()   const { return m_uniformNames; }
	inline const std::vector<std::string>& GetUniformTypes()   const { return m_uniformTypes; }
	inline const std::map<std::string, GLint>& GetUniformMap() const { return m_uniformMap; }
private:
	GLuint                       m_program;
	std::vector<GLuint>          m_shaders;
	std::vector<std::string>     m_uniformNames;
	std::vector<std::string>     m_uniformTypes;
	std::map<std::string, GLint> m_uniformMap;

	OpenGL3ShaderProgram(OpenGL3ShaderProgram& other) { (void)other; }
	void operator=(const OpenGL3ShaderProgram& other) { (void)other;}
};

#endif
