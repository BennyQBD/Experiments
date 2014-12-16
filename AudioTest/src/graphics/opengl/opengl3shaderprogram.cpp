#include "opengl3shaderprogram.h"
#include <sstream>
#include <cassert>
#include <fstream>

class TypedData
{
public:
	TypedData(const std::string& name, const std::string& type) :
		m_name(name),
		m_type(type) {}
		
	inline const std::string& GetName() const { return m_name; }
	inline const std::string& GetType() const { return m_type; }
private:
	std::string m_name;
	std::string m_type;
};

class UniformStruct
{
public:
	UniformStruct(const std::string& name, const std::vector<TypedData>& memberNames) :
		m_name(name),
		m_memberNames(memberNames) {}
		
	inline const std::string& GetName()                   const { return m_name; }
	inline const std::vector<TypedData>& GetMemberNames() const { return m_memberNames; }
private:
	std::string            m_name;
	std::vector<TypedData> m_memberNames;
};

namespace Util
{
	std::vector<std::string> Split(const std::string &s, char delim)
	{
		std::vector<std::string> elems;
        
		const char* cstr = s.c_str();
		unsigned int strLength = (unsigned int)s.length();
		unsigned int start = 0;
		unsigned int end = 0;
			
		while(end <= strLength)
		{
			while(end <= strLength)
			{
				if(cstr[end] == delim)
					break;
				end++;
			}
				
			elems.push_back(s.substr(start, end - start));
			start = end + 1;
			end = start;
		}
			
		return elems;
	}
}

static void AddShader(GLuint shaderProgram, const std::string& text, GLenum type, std::vector<GLuint>* shaders);
static void AddAllAttributes(GLuint program, const std::string& vertexShaderText, const std::string& attributeKeyword);
static void AddUniform(GLuint shaderProgram, const std::string& uniformName, const std::string& uniformType, const std::vector<UniformStruct>& structs, std::map<std::string, GLint>* uniformMap);
static void AddShaderUniforms(GLuint shaderProgram, const std::string& shaderText, std::vector<std::string>* uniformNames, std::vector<std::string>* uniformTypes, std::map<std::string, GLint>* uniformMap);
static void CheckShaderError(GLuint shader, int flag, bool isProgram, const std::string& errorMessage);
static std::vector<UniformStruct> FindUniformStructs(const std::string& shaderText);
static std::string FindUniformStructName(const std::string& structStartToOpeningBrace);
static std::vector<TypedData> FindUniformStructComponents(const std::string& openingBraceToClosingBrace);
static std::string LoadShader(const std::string& fileName);

OpenGL3ShaderProgram::OpenGL3ShaderProgram(const std::string& inputText, 
		const std::string& shaderVersion, bool loadFromFile)
{
	std::string shaderText = inputText;
	if(loadFromFile)
	{
		shaderText = LoadShader(inputText);
	}

	m_program = glCreateProgram();

	if(m_program == 0) 
	{
		throw IShaderProgram::Exception("Error creating shader program\n");
    }

	std::string version = shaderVersion;
	std::string vertexShaderText = "#version " + version + "\n#define VS_BUILD\n#define GLSL_VERSION " + version + "\n" + shaderText;
	std::string fragmentShaderText = "#version " + version + "\n#define FS_BUILD\n#define GLSL_VERSION " + version + "\n" + shaderText;
    
    AddShader(m_program, vertexShaderText, GL_VERTEX_SHADER, &m_shaders);
	AddShader(m_program, fragmentShaderText, GL_FRAGMENT_SHADER, &m_shaders);
	
	AddAllAttributes(m_program, vertexShaderText, "attribute");
	
	glLinkProgram(m_program);
	CheckShaderError(m_program, GL_LINK_STATUS, true, "Error linking shader program");

    glValidateProgram(m_program);
	CheckShaderError(m_program, GL_VALIDATE_STATUS, true, "Invalid shader program");

	AddShaderUniforms(m_program, shaderText, &m_uniformNames, &m_uniformTypes, &m_uniformMap);
}

OpenGL3ShaderProgram::~OpenGL3ShaderProgram()
{
	for(std::vector<GLuint>::const_iterator it = m_shaders.begin();
		   	it != m_shaders.end(); ++it) 
	{
		glDetachShader(m_program, *it);
		glDeleteShader(*it);
	}
	glDeleteProgram(m_program);
}

void OpenGL3ShaderProgram::Bind()
{
	glUseProgram(m_program);
}

void OpenGL3ShaderProgram::UpdateUniforms(const UniformData& uniformData)
{
	Matrix4f worldMatrix = uniformData.world;//transform.GetTransformation();
	Matrix4f projectedMatrix = uniformData.viewProjection
		* worldMatrix;//camera.GetViewProjection() * worldMatrix;

	MaterialValues* material = uniformData.material;
	RendererValues* renderData = uniformData.renderData;
	
	for(unsigned int i = 0; i < m_uniformNames.size(); i++)
	{
		std::string uniformName = m_uniformNames[i];
		std::string uniformType = m_uniformTypes[i];
		
		if(uniformName.substr(0, 2) == "R_")
		{
			std::string unprefixedName = uniformName.substr(2, uniformName.length());
			
			//if(unprefixedName == "lightMatrix")
			//	SetUniformMatrix4f(uniformName, rendererData.GetLightMatrix() * worldMatrix);
			if(uniformType == "sampler2D")
			{
				unsigned int samplerSlot = renderData->GetSamplerSlot(unprefixedName);
				renderData->GetTexture(unprefixedName)->Bind(samplerSlot);
				SetUniformi(uniformName, (int)samplerSlot);
			}
			else if(uniformType == "mat4")
				SetUniformMatrix4f(uniformName, renderData->GetMatrix4f(unprefixedName));
			else if(uniformType == "vec3")
				SetUniformVector3f(uniformName, renderData->GetVector3f(unprefixedName));
			else if(uniformType == "float")
				SetUniformf(uniformName, renderData->GetFloat(unprefixedName));
			else
			{
				std::ostringstream out;
				out << "Invalid Render Uniform: " << uniformName << " of type: " << uniformType;
				throw IShaderProgram::Exception(out.str());
			}
			/*
			else if(uniformType == "DirectionalLight")
				SetUniformDirectionalLight(uniformName, *(const DirectionalLight*)&rendererData.GetActiveLight());
			else if(uniformType == "PointLight")
				SetUniformPointLight(uniformName, *(const PointLight*)&rendererData.GetActiveLight());
			else if(uniformType == "SpotLight")
				SetUniformSpotLight(uniformName, *(const SpotLight*)&rendererData.GetActiveLight());
			else
				rendererData.UpdateUniformStruct(transform, material, *this, uniformName, uniformType);
			*/
		}
		else if(uniformType == "sampler2D")
		{
			unsigned int samplerSlot = renderData->GetSamplerSlot(uniformName);
			material->GetTexture(uniformName)->Bind(samplerSlot);
			SetUniformi(uniformName, (int)samplerSlot);
		}
		else if(uniformName.substr(0, 2) == "T_")
		{
			if(uniformName == "T_MVP")
				SetUniformMatrix4f(uniformName, projectedMatrix);
			else if(uniformName == "T_model")
				SetUniformMatrix4f(uniformName, worldMatrix);
			else
			{
				std::ostringstream out;
				out << "Invalid Transform Uniform: " << uniformName;
				throw IShaderProgram::Exception(out.str());
			}
		}
		else if(uniformName.substr(0, 2) == "C_")
		{
//			if(uniformName == "C_eyePos")
//				SetUniformVector3f(uniformName, camera.GetTransform().GetTransformedPos());
//			else
//			{
				std::ostringstream out;
				out << "Invalid Camera Uniform: " << uniformName;
				throw IShaderProgram::Exception(out.str());
//			}
		}
		else
		{
			if(uniformType == "vec3")
				SetUniformVector3f(uniformName, material->GetVector3f(uniformName));
			else if(uniformType == "float")
				SetUniformf(uniformName, material->GetFloat(uniformName));
			else if(uniformType == "mat4")
				SetUniformMatrix4f(uniformName, material->GetMatrix4f(uniformName));
			else
			{
				std::ostringstream out;
				out << uniformType << " is not supported by the Material class";
				throw IShaderProgram::Exception(out.str());
			}
		}
	}
}

void OpenGL3ShaderProgram::SetUniformi(const std::string& uniformName, int value) const
{
	glUniform1i(m_uniformMap.at(uniformName), value);
}

void OpenGL3ShaderProgram::SetUniformf(const std::string& uniformName, float value) const
{
	glUniform1f(m_uniformMap.at(uniformName), value);
}

void OpenGL3ShaderProgram::SetUniformMatrix4f(const std::string& uniformName, const Matrix4f& value) const
{
	glUniformMatrix4fv(m_uniformMap.at(uniformName), 1, GL_FALSE, &(value[0][0]));
}

void OpenGL3ShaderProgram::SetUniformVector3f(const std::string& uniformName, const Vector3f& value) const
{
	glUniform3f(m_uniformMap.at(uniformName), value.GetX(), value.GetY(), value.GetZ());
}

static void AddShaderUniforms(GLuint shaderProgram, const std::string& shaderText, std::vector<std::string>* uniformNames, std::vector<std::string>* uniformTypes, std::map<std::string, GLint>* uniformMap)
{
	static const std::string UNIFORM_KEY = "uniform";
		
	std::vector<UniformStruct> structs = FindUniformStructs(shaderText);

	size_t uniformLocation = shaderText.find(UNIFORM_KEY);
	while(uniformLocation != std::string::npos)
	{
		bool isCommented = false;
		size_t lastLineEnd = shaderText.rfind('\n', uniformLocation);
		
		if(lastLineEnd != std::string::npos)
		{
			std::string potentialCommentSection = shaderText.substr(lastLineEnd,uniformLocation - lastLineEnd);
			isCommented = potentialCommentSection.find("//") != std::string::npos || potentialCommentSection.find('#') != std::string::npos;
		}
		
		if(!isCommented)
		{
			size_t begin = uniformLocation + UNIFORM_KEY.length();
			size_t end = shaderText.find(";", begin);
			
			std::string uniformLine = shaderText.substr(begin + 1, end-begin - 1);
			
			begin = uniformLine.find(" ");
			std::string uniformName = uniformLine.substr(begin + 1);
			std::string uniformType = uniformLine.substr(0, begin);
			
			uniformNames->push_back(uniformName);
			uniformTypes->push_back(uniformType);
			AddUniform(shaderProgram, uniformName, uniformType, structs, uniformMap);
		}
		uniformLocation = shaderText.find(UNIFORM_KEY, uniformLocation + UNIFORM_KEY.length());
	}
}

static void AddUniform(GLuint shaderProgram, const std::string& uniformName, const std::string& uniformType, const std::vector<UniformStruct>& structs, std::map<std::string, GLint>* uniformMap)
{
	bool addThis = true;

	for(unsigned int i = 0; i < structs.size(); i++)
	{
		if(structs[i].GetName().compare(uniformType) == 0)
		{
			addThis = false;
			for(unsigned int j = 0; j < structs[i].GetMemberNames().size(); j++)
			{
				AddUniform(shaderProgram, uniformName + "." + structs[i].GetMemberNames()[j].GetName(), structs[i].GetMemberNames()[j].GetType(), structs, uniformMap);
			}
		}
	}

	if(!addThis)
		return;

	GLint location = glGetUniformLocation(shaderProgram, uniformName.c_str());

	assert(location != (GLint)0xFFFFFFFF);

	uniformMap->insert(std::pair<std::string, unsigned int>(uniformName, location));
}


static void CheckShaderError(GLuint shader, int flag, bool isProgram, const std::string& errorMessage)
{
	GLint success = 0;
    GLchar error[1024] = { 0 };

	if(isProgram)
		glGetProgramiv(shader, flag, &success);
	else
		glGetShaderiv(shader, flag, &success);

	if(!success)
	{
		if(isProgram)
			glGetProgramInfoLog(shader, sizeof(error), NULL, error);
		else
			glGetShaderInfoLog(shader, sizeof(error), NULL, error);

		fprintf(stderr, "%s: '%s'\n", errorMessage.c_str(), error);
	}
}

static void AddAllAttributes(GLuint program, const std::string& vertexShaderText, const std::string& attributeKeyword)
{
	int currentAttribLocation = 0;
	size_t attributeLocation = vertexShaderText.find(attributeKeyword);
	while(attributeLocation != std::string::npos)
	{
		bool isCommented = false;
		size_t lastLineEnd = vertexShaderText.rfind('\n', attributeLocation);
		
		if(lastLineEnd != std::string::npos)
		{
			std::string potentialCommentSection = vertexShaderText.substr(lastLineEnd,attributeLocation - lastLineEnd);
			isCommented = potentialCommentSection.find("//") != std::string::npos || potentialCommentSection.find('#') != std::string::npos;
		}
		
		if(!isCommented)
		{
			size_t begin = attributeLocation + attributeKeyword.length();
			size_t end = vertexShaderText.find(";", begin);
			
			std::string attributeLine = vertexShaderText.substr(begin + 1, end-begin - 1);
			
			begin = attributeLine.find(" ");
			std::string attributeName = attributeLine.substr(begin + 1);
			
			glBindAttribLocation(program, currentAttribLocation, attributeName.c_str());
			currentAttribLocation++;
		}
		attributeLocation = vertexShaderText.find(attributeKeyword, attributeLocation + attributeKeyword.length());
	}
}

static void AddShader(GLuint shaderProgram, const std::string& text, GLenum type, std::vector<GLuint>* shaders)
{
	GLuint shader = glCreateShader(type);

	if(shader == 0)
	{
		std::ostringstream out;
		out << "Error creating shader type " << type;
		throw IShaderProgram::Exception(out.str());
	}

	const GLchar* p[1];
	p[0] = text.c_str();
	GLint lengths[1];
	lengths[0] = (GLint)text.length();

	glShaderSource(shader, 1, p, lengths);
	glCompileShader(shader);

	GLint success;
    glGetShaderiv(shader, GL_COMPILE_STATUS, &success);
    if (!success) 
	{
        GLchar InfoLog[1024];

        glGetShaderInfoLog(shader, 1024, NULL, InfoLog);
		std::ostringstream out;
		out << "Error compiling shader type " << shader << ": '" << InfoLog << "'";
		throw IShaderProgram::Exception(out.str());
    }

	glAttachShader(shaderProgram, shader);
	shaders->push_back(shader);
}

static std::string GetFilePath(const std::string& fileName)
{
	const char* cstr = fileName.c_str();
	unsigned int strLength = (unsigned int)fileName.length();
	unsigned int end = strLength - 1;
	
	while(end != 0)
	{
		if(cstr[end] == '/')
		{
			break;
		}
		end--;
	}

	if(end == 0)
	{
		return fileName;
	}
	else
	{
		unsigned int start = 0;
		end = end + 1;
		return fileName.substr(start, end - start);
	}
}

static std::string LoadShader(const std::string& fileName)
{
	std::ifstream file;
	file.open(fileName.c_str());

	std::string filePath = GetFilePath(fileName);
	std::string output;
	std::string line;

	if(file.is_open())
	{
		while(file.good())
		{
			getline(file, line);
			
			if(line.find("#include") == std::string::npos)
			{
				output.append(line + "\n");
			}
			else
			{
				std::string includeFileName = Util::Split(line, ' ')[1];
				includeFileName = includeFileName.substr(1,includeFileName.length() - 2);

				std::string toAppend = LoadShader(filePath + includeFileName);
				output.append(toAppend + "\n");
			}
		}
	}
	else
	{
		std::ostringstream out;
		out << "Unable to load shader: " << fileName;
		throw IShaderProgram::Exception(out.str());
	}

	return output;
}

static std::vector<TypedData> FindUniformStructComponents(const std::string& openingBraceToClosingBrace)
{
	static const char charsToIgnore[] = {' ', '\n', '\t', '{'};
	static const size_t UNSIGNED_NEG_ONE = (size_t)-1;

	std::vector<TypedData> result;
	std::vector<std::string> structLines = Util::Split(openingBraceToClosingBrace, ';');

	for(unsigned int i = 0; i < structLines.size(); i++)
	{
		size_t nameBegin = UNSIGNED_NEG_ONE;
		size_t nameEnd = UNSIGNED_NEG_ONE;

		for(unsigned int j = 0; j < structLines[i].length(); j++)
		{
			bool isIgnoreableCharacter = false;

			for(unsigned int k = 0; k < sizeof(charsToIgnore)/sizeof(char); k++)
			{
				if(structLines[i][j] == charsToIgnore[k])
				{
					isIgnoreableCharacter = true;
					break;
				}
			}

			if(nameBegin == UNSIGNED_NEG_ONE && isIgnoreableCharacter == false)
			{
				nameBegin = j;
			}
			else if(nameBegin != UNSIGNED_NEG_ONE && isIgnoreableCharacter)
			{
				nameEnd = j;
				break;
			}
		}

		if(nameBegin == UNSIGNED_NEG_ONE || nameEnd == UNSIGNED_NEG_ONE)
			continue;

		TypedData newData(
			structLines[i].substr(nameEnd + 1), 
			structLines[i].substr(nameBegin, nameEnd - nameBegin));

		result.push_back(newData);
	}

	return result;
}

static std::string FindUniformStructName(const std::string& structStartToOpeningBrace)
{
	return Util::Split(Util::Split(structStartToOpeningBrace, ' ')[0], '\n')[0];
}

static std::vector<UniformStruct> FindUniformStructs(const std::string& shaderText)
{
	static const std::string STRUCT_KEY = "struct";
	std::vector<UniformStruct> result;

	size_t structLocation = shaderText.find(STRUCT_KEY);
	while(structLocation != std::string::npos)
	{
		structLocation += STRUCT_KEY.length() + 1; //Ignore the struct keyword and space

		size_t braceOpening = shaderText.find("{", structLocation);
		size_t braceClosing = shaderText.find("}", braceOpening);

		UniformStruct newStruct(
			FindUniformStructName(shaderText.substr(structLocation, braceOpening - structLocation)),
			FindUniformStructComponents(shaderText.substr(braceOpening, braceClosing - braceOpening)));

		result.push_back(newStruct);
		structLocation = shaderText.find(STRUCT_KEY, structLocation);
	}

	return result;
}
