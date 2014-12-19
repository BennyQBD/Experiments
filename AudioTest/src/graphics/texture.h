#ifndef TEXTURE_INCLUDED_H
#define TEXTURE_INCLUDED_H

#include "../resourceManagement/resource.h"
#include "itexture.h"

class Texture : public Resource
{
public:
	inline ITexture* GetTexture() { return (ITexture*)GetData(); }
};

#endif


