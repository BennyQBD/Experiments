#include "opengl3renderdevice.h"
#include "opengl3vertexarray.h"
#include "opengl3shaderprogram.h"
#include "opengl3texture.h"
#include "../staticlibs/stb_image.h"

#include <assimp/Importer.hpp>
#include <assimp/scene.h>
#include <assimp/postprocess.h>

#include <GL/glew.h>
#include <sstream>
#include <cassert>

OpenGL3RenderDevice::OpenGL3RenderDevice()
{
	int majorVersion;
	int minorVersion;
		
	glGetIntegerv(GL_MAJOR_VERSION, &majorVersion); 
	glGetIntegerv(GL_MINOR_VERSION, &minorVersion);
		
	m_version = (unsigned int)(majorVersion * 100 + minorVersion * 10);

	if(m_version >= 330)
	{
		std::ostringstream convert;
		convert << m_version;
	
		m_shaderVersion = convert.str();
	}
	else if(m_version >= 320)
	{
		m_shaderVersion = "150";
	}
	else if(m_version >= 310)
	{
		m_shaderVersion = "140";
	}
	else if(m_version >= 300)
	{
		m_shaderVersion = "130";
	}
	else if(m_version >= 210)
	{
		m_shaderVersion = "120";
	}
	else if(m_version >= 200)
	{
		m_shaderVersion = "110";
	}
	else
	{
		std::ostringstream out;
		out << "Error: OpenGL Version " << majorVersion << "." 
			<< minorVersion << " does not support shaders.";
		throw IRenderDevice::Exception(out.str());
	}
}

IVertexArray* OpenGL3RenderDevice::CreateVertexArrayFromFile(const std::string& fileName)
{
	Assimp::Importer importer;
		
	const aiScene* scene = importer.ReadFile(fileName.c_str(), 
											 aiProcess_Triangulate |
											 aiProcess_GenSmoothNormals | 
											 aiProcess_FlipUVs |
											 aiProcess_CalcTangentSpace);
	
	if(!scene)
	{
		std::ostringstream out;
		out <<  "Mesh load failed!: " << fileName;
		throw IRenderDevice::Exception(out.str());
	}
	
	const aiMesh* model = scene->mMeshes[0];
	
	std::vector<Vector3f> positions;
	std::vector<Vector2f> texCoords;
	std::vector<Vector3f> normals;
	std::vector<Vector3f> tangents;
	std::vector<unsigned int> indices;

	const aiVector3D aiZeroVector(0.0f, 0.0f, 0.0f);
	for(unsigned int i = 0; i < model->mNumVertices; i++) 
	{
		const aiVector3D pos = model->mVertices[i];
		const aiVector3D normal = model->mNormals[i];
		const aiVector3D texCoord = model->HasTextureCoords(0) ? model->mTextureCoords[0][i] : aiZeroVector;
		const aiVector3D tangent = model->mTangents[i];

		positions.push_back(Vector3f(pos.x, pos.y, pos.z));
		texCoords.push_back(Vector2f(texCoord.x, texCoord.y));
		normals.push_back(Vector3f(normal.x, normal.y, normal.z));
		tangents.push_back(Vector3f(tangent.x, tangent.y, tangent.z));
	}

	for(unsigned int i = 0; i < model->mNumFaces; i++)
	{
		const aiFace& face = model->mFaces[i];
		assert(face.mNumIndices == 3);
		indices.push_back(face.mIndices[0]);
		indices.push_back(face.mIndices[1]);
		indices.push_back(face.mIndices[2]);
	}
	
	return CreateVertexArray(IndexedModel(indices, positions, texCoords, normals, tangents));
}

IVertexArray* OpenGL3RenderDevice::CreateVertexArray(const IndexedModel& model)
{
	if(!model.IsValid())
	{
		std::ostringstream out;
		out << "Error: Invalid mesh! The number of texCoords, normals,"
			<< " and tangents must be either 0, or equal to the number of"
			<< " positions";
		throw IRenderDevice::Exception(out.str());
	}

	float* positions = (float*)&(model.GetPositions()[0]);
	float* texCoords = (float*)&(model.GetTexCoords()[0]);
	float* normals   = (float*)&(model.GetNormals()[0]);
	float* tangents  = (float*)&(model.GetTangents()[0]);

	std::vector<float*> vertexData;
	std::vector<unsigned int> vertexElementSizes;

	vertexData.push_back(positions);
	vertexElementSizes.push_back(sizeof(model.GetPositions()[0])/sizeof(float));

	unsigned int numVertexComponents = 1;
	if(model.HasTexCoords()) 
	{
		vertexData.push_back(texCoords);
		vertexElementSizes.push_back(sizeof(model.GetTexCoords()[0])/sizeof(float));
		numVertexComponents++; 
	}
	if(model.HasNormals())   
	{ 
		vertexData.push_back(normals);
		vertexElementSizes.push_back(sizeof(model.GetNormals()[0])/sizeof(float));
		numVertexComponents++; 
	}
	if(model.HasTangents())
	{
		vertexData.push_back(tangents);
		vertexElementSizes.push_back(sizeof(model.GetTangents()[0])/sizeof(float));
		numVertexComponents++;
	}	
	
	unsigned int numVertices = (unsigned int)model.GetPositions().size();
	unsigned int* indices = (unsigned int*)&(model.GetIndices()[0]);
	unsigned int numIndices = (unsigned int)model.GetIndices().size();

	return new OpenGL3VertexArray(&vertexData[0], &vertexElementSizes[0], 
			numVertexComponents, numVertices, indices, numIndices);
}

void OpenGL3RenderDevice::ReleaseVertexArray(IVertexArray* vertexArray)
{
	if(vertexArray) { delete vertexArray; }
}

IShaderProgram* OpenGL3RenderDevice::CreateShaderProgram(const std::string& shaderText)
{
	return new OpenGL3ShaderProgram(shaderText, m_shaderVersion, false);
}

IShaderProgram* OpenGL3RenderDevice::CreateShaderProgramFromFile(
			const std::string& fileName)
{
	return new OpenGL3ShaderProgram(fileName, m_shaderVersion, true);
}

void OpenGL3RenderDevice::ReleaseShaderProgram(IShaderProgram* shaderProgram)
{
	if(shaderProgram) { delete shaderProgram; }
}

ITexture* OpenGL3RenderDevice::CreateTextureFromFile(const std::string& fileName,
			bool compress, int filter, float anisotropy, bool clamp)
{
	int x, y, numComponents;
	unsigned char* data = stbi_load(fileName.c_str(), &x, &y, &numComponents, 0);

	if(data == NULL)
	{
		std::ostringstream out;
		out << "Unable to load texture: " << fileName;
		throw ITexture::Exception(out.str());
	}

	int format = ITexture::FORMAT_RGBA;
	
	switch(numComponents)
	{
		case 1:
			format = ITexture::FORMAT_R;
			break;
		case 2:
			format = ITexture::FORMAT_RG;
			break;
		case 3:
			format = ITexture::FORMAT_RGB;
			break;
		case 4:
			format = ITexture::FORMAT_RGBA;
			break;
		default:
			std::ostringstream out;
			out << "Invalid number of texture components (" 
				<< numComponents << ") in : " << fileName;
			throw ITexture::Exception(out.str());
	}

	ITexture* texture = CreateTexture(x, y, data, format,
			format, compress, filter, anisotropy, clamp);

	stbi_image_free(data);
	return texture;
}

ITexture* OpenGL3RenderDevice::CreateTexture(int width, int height, unsigned char* data, 
			int format, int internalFormat, bool compress, int filter, float anisotropy, 
			bool clamp)
{
	return new OpenGL3Texture(width, height, data, filter, anisotropy, 
			internalFormat, format, clamp, compress);
}

void OpenGL3RenderDevice::ReleaseTexture(ITexture* texture)
{
	if(texture) { delete texture; }
}

