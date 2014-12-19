#ifndef MESH_INCLUDED_H
#define MESH_INCLUDED_H

#include "../resourceManagement/resource.h"
#include "ivertexarray.h"

class Mesh : public Resource
{
public:
	inline IVertexArray* GetVertexArray() { return (IVertexArray*)GetData(); }
};

#endif
