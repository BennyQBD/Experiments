#include "resource.h"
#include "resourceTracker.h"

ResourceData::~ResourceData()
{
	m_resources->RemoveResource(m_name, m_data);
}

Resource::Resource(ResourceData* data) :
	m_data(data) 
{
	m_data->AddReference();
}

Resource::Resource(const Resource& other) :
	m_data(other.m_data)
{
	m_data->AddReference();
}

Resource::~Resource()
{
	if(m_data->RemoveReference())
	{
		delete m_data;
	}
}

Resource& Resource::operator=(const Resource& other)
{
//	ResourceData* temp = m_data;
//	m_data = other.m_data;
//	other.m_data = temp;
	return *this;
}
