#ifndef RESOURCE_MANAGER_INCLUDED_H
#define RESOURCE_MANAGER_INCLUDED_H

#include "../resourceManagement/resourceTracker.h"
#include "../graphics/mesh.h"
#include "../graphics/shader.h"
#include "../graphics/texture.h"
#include "../graphics/material.h"
#include "../graphics/irenderdevice.h"
#include "../audio/audioData.h"
#include "../audio/iaudiodevice.h"

class ResourceManager
{
public:
	ResourceManager(IRenderDevice* render, IAudioDevice* audio);
	
	Mesh GetMesh(const std::string& name);
	Mesh RegisterMesh(const std::string& name, const IndexedModel& model);

	Shader GetShader(const std::string& name);
	Shader RegisterShader(const std::string& name, const std::string& shaderText);

	Texture GetTexture(const std::string& name, bool compress, int filter, 
			float anisotropy, bool clamp);
	Texture RegisterTexture(const std::string& name, int width, int height, unsigned char* data, 
			int format, int internalFormat, bool compress, int filter,
			float anisotropy, bool clamp);

	Material GetMaterial(const std::string& name);
	Material RegisterMaterial(const std::string& name, MaterialValues* values);

	AudioData GetAudioData(const std::string& name, bool streamFromFile);
	AudioData RegisterAudioData(const std::string& name, IAudioData* data);
private:
	IRenderDevice*  m_render;
	IAudioDevice*   m_audio;
	ResourceTracker m_meshTracker;
	ResourceTracker m_shaderTracker;
	ResourceTracker m_textureTracker;
	ResourceTracker m_materialTracker;
	ResourceTracker m_audioTracker;
};

#endif
