#ifndef MATERIAL_INCLUDED_H
#define MATERIAL_INCLUDED_H

#include "../resourceManagement/resource.h"
#include "materialvalues.h"

class Material : public Resource
{
public:
	inline MaterialValues* GetValues() { return (MaterialValues*)GetData(); }
};

#endif

