#ifndef RESOURCE_TRACKER_INCLUDED_H
#define RESOURCE_TRACKER_INCLUDED_H

#include "resource.h"

#include <map>

class ResourceTracker
{
public:
	ResourceTracker(void* allocator,
			void* (*allocFunc)(void* allocator, const std::string& name, void* params),
			void (*deallocFunc)(void* allocator, void* data)) :
		m_allocator(allocator),
		m_allocFunc(allocFunc),
		m_deallocFunc(deallocFunc) {}

	Resource GetResource(const std::string& name, void* allocatorParams);
	Resource RegisterResource(const std::string& name, void* resourceData);
	void RemoveResource(const std::string& name, void* dataToDelete);

private:
	std::map<std::string, ResourceData*> m_resourceMap;
	void* m_allocator;
	void* (*m_allocFunc)(void* allocator, const std::string& name, void* params);
	void (*m_deallocFunc)(void* allocator, void* data);
};

#endif
