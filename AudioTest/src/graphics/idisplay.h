#ifndef I_DISPLAY_INCLUDED_H
#define I_DISPLAY_INCLUDED_H

#include "irenderdevice.h"
#include "irendercontext.h"
#include "../core/iinput.h"

class IDisplay
{
public:
	virtual ~IDisplay() {}
	
	virtual void Update() = 0;
	virtual void SwapBuffers() = 0;
	virtual bool IsClosed() = 0;

	virtual int GetWidth() = 0;
	virtual int GetHeight() = 0;

	virtual IInput* GetInput() = 0;
	virtual IRenderContext* GetRenderContext() = 0;
	virtual IRenderDevice* GetRenderDevice() = 0;
	virtual IRenderTarget* GetRenderTarget() = 0;
};

#endif
