#include "resourceManager.h"

static void* CreateMeshFromFile(void* irenderdevice, const std::string& fileName, void* params)
{
	(void)params;
	IRenderDevice* device = (IRenderDevice*)irenderdevice;
	return (void*)device->CreateVertexArrayFromFile(fileName);
}

static void ReleaseMesh(void* irenderdevice, void* data)
{
	IRenderDevice* device = (IRenderDevice*)irenderdevice;
	device->ReleaseVertexArray((IVertexArray*)data);
}

static void* CreateShaderFromFile(void* irenderdevice, const std::string& fileName, void* params)
{
	(void)params;
	IRenderDevice* device = (IRenderDevice*)irenderdevice;
	return (void*)device->CreateShaderProgramFromFile(fileName);
}

static void ReleaseShader(void* irenderdevice, void* data)
{
	IRenderDevice* device = (IRenderDevice*)irenderdevice;
	device->ReleaseShaderProgram((IShaderProgram*)data);
}

struct CreateTextureParams
{
	int filter;
	float anisotropy;
	bool clamp;
	bool compress;
};

static void* CreateTextureFromFile(void* irenderdevice, const std::string& name, void* paramsIn)
{
	IRenderDevice* device = (IRenderDevice*)irenderdevice;
	CreateTextureParams* params = (CreateTextureParams*)paramsIn;

	return (void*)device->CreateTextureFromFile(name, params->compress,
			params->filter, params->anisotropy, params->clamp);
}

static void ReleaseTexture(void* irenderdevice, void* data)
{
	IRenderDevice* device = (IRenderDevice*)irenderdevice;
	device->ReleaseTexture((ITexture*)data);
}

static void* CreateMaterialFromFile(void* allocator, const std::string& name, void* data)
{
	throw std::logic_error("Error: Material loading from file not yet implemented");
}

static void ReleaseMaterial(void* allocator, void* data)
{
	MaterialValues* values = (MaterialValues*)data;
	delete values;
}

ResourceManager::ResourceManager(IRenderDevice* device) :
	m_device(device),
	m_meshTracker(device, CreateMeshFromFile, ReleaseMesh),
	m_shaderTracker(device, CreateShaderFromFile, ReleaseShader),
	m_textureTracker(device, CreateTextureFromFile, ReleaseTexture),
	m_materialTracker(NULL, CreateMaterialFromFile, ReleaseMaterial) {}

Mesh ResourceManager::GetMesh(const std::string& name)
{
	Resource result = m_meshTracker.GetResource(name, NULL);

	// Reinterpret the result as appropriate data type.
	// This should work because it only adds a convenience method and
	// no new data.
	return *(Mesh*)(&result);
}

Mesh ResourceManager::RegisterMesh(const std::string& name, 
		const IndexedModel& model)
{
	IVertexArray* data = m_device->CreateVertexArray(model);
	Resource result = m_meshTracker.RegisterResource(name, data);
	return *(Mesh*)(&result);
}

Shader ResourceManager::GetShader(const std::string& name)
{
	Resource result = m_shaderTracker.GetResource(name, NULL);
	return *(Shader*)(&result);
}

Shader ResourceManager::RegisterShader(const std::string& name, const std::string& shaderText)
{
	IShaderProgram* data = m_device->CreateShaderProgram(shaderText);
	Resource result = m_shaderTracker.RegisterResource(name, data);
	return *(Shader*)(&result);
}

Texture ResourceManager::GetTexture(const std::string& name, bool compress, int filter, 
		float anisotropy, bool clamp)
{
	CreateTextureParams params;
	params.compress = compress,
	params.filter = filter;
	params.anisotropy = anisotropy;
	params.clamp = clamp;

	Resource result = m_textureTracker.GetResource(name, &params);
	return *(Texture*)(&result);
}

Texture ResourceManager::RegisterTexture(const std::string& name, int width, 
		int height, unsigned char* data, int format, int internalFormat,
		bool compress, int filter, float anisotropy, bool clamp)
{
	ITexture* textureData = m_device->CreateTexture(width, height, data, format,
			internalFormat, compress, filter, anisotropy, clamp);
	Resource result = m_textureTracker.RegisterResource(name, textureData);
	return *(Texture*)(&result);
}

Material ResourceManager::GetMaterial(const std::string& name)
{
	Resource result = m_materialTracker.GetResource(name, NULL);
	return *(Material*)(&result);
}

Material ResourceManager::RegisterMaterial(const std::string& name, MaterialValues* values)
{
	Resource result = m_materialTracker.RegisterResource(name, values);
	return *(Material*)(&result);
}
