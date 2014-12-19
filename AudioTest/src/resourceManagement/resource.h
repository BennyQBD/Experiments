#ifndef RESOURCE_INCLUDED_H
#define RESOURCE_INCLUDED_H

#include "referenceCounter.h"
#include <string>

class ResourceTracker;
class ResourceData : public ReferenceCounter
{
public:
	ResourceData(void* data, ResourceTracker* resources,
		   const std::string& name) :
		m_data(data),
		m_resources(resources),
		m_name(name) {}
	
	~ResourceData();	
	inline void* GetData() { return m_data; }
private:
	void*  m_data;
	ResourceTracker* m_resources;
	std::string      m_name;

	ResourceData(ResourceData& other) { (void)other; }
	void operator=(ResourceData& other) { (void)other; }
};

class Resource
{
public:
	Resource(ResourceData* data);
	Resource(const Resource& other);
	~Resource();
	inline void* GetData() 
	{ 
		return m_data->GetData(); 
	}
	Resource& operator=(const Resource& other);
private:
	ResourceData* m_data;	
};

#endif

