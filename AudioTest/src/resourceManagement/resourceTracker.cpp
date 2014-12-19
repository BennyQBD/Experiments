#include "resourceTracker.h"

Resource ResourceTracker::GetResource(const std::string& name, void* params)
{
	std::map<std::string, ResourceData*>::const_iterator it =
		m_resourceMap.find(name);
	if(it != m_resourceMap.end())
	{
		return Resource(it->second);
	}
	else
	{
		void* data = m_allocFunc(m_allocator, name, params);
		return RegisterResource(name, data);
	}
}

Resource ResourceTracker::RegisterResource(const std::string& name,
		void* resourceData)
{
	ResourceData* data =
		new ResourceData(resourceData, this, name);
	m_resourceMap[name] = data;

	// Remove the reference from our pointer
	data->RemoveReference();
	return Resource(data);
}

void ResourceTracker::RemoveResource(const std::string& name, void* dataToDelete)
{
	m_deallocFunc(m_allocator, dataToDelete);
	std::map<std::string, ResourceData*>::const_iterator it =
		m_resourceMap.find(name);
	if(it != m_resourceMap.end())
	{
		// Warning: Do not access the ResourceData* at this point; it has been
		// freed.
		m_resourceMap.erase(name);
	}
}

