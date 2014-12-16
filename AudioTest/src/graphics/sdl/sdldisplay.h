#ifndef SDL_DISPLAY_INCLUDED_H
#define SDL_DISPLAY_INCLUDED_H

#include "../idisplay.h"
#include "../irenderdevice.h"
#include "../irendercontext.h"
#include "sdlinput.h"

#include <SDL2/SDL.h>
#include <string>
#include <stdexcept>

class SDLDisplay : public IDisplay
{
public:
	SDLDisplay(int width, int height, const std::string& title);
	virtual ~SDLDisplay();

	virtual void Update();
	virtual void SwapBuffers();
	virtual bool IsClosed();

	virtual int GetWidth();
	virtual int GetHeight();

	virtual IInput* GetInput();
	virtual IRenderContext* GetRenderContext();
	virtual IRenderDevice* GetRenderDevice();
	virtual IRenderTarget* GetRenderTarget();

	class RuntimeException : public std::runtime_error
	{
	public:
		RuntimeException(const std::string& message) :
			std::runtime_error(message) {}
	};
private:
	SDL_Window*     m_window;
	SDL_GLContext   m_glContext;
	IRenderDevice*  m_renderDevice;
	IRenderContext* m_renderContext;
	IRenderTarget*  m_renderTarget;
	SDLInput*       m_input;
	int             m_width;
	int             m_height;
	bool            m_isClosed;

	SDLDisplay(const SDLDisplay& other) {}
	void operator=(const SDLDisplay& other) {}
};

#endif
