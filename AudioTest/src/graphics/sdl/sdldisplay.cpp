#include "sdldisplay.h"
#include "../opengl/opengl3renderdevice.h"
#include "../opengl/opengl3rendercontext.h"
#include "../opengl/opengl3rendertarget.h"
#include <GL/glew.h>
#include <sstream>

#define RENDERDEVICE OpenGL3RenderDevice
#define RENDERCONTEXT OpenGL3RenderContext
#define RENDERTARGET OpenGL3RenderTarget

SDLDisplay::SDLDisplay(int width, int height, const std::string& title) :
	m_width(width),
	m_height(height),
	m_isClosed(false)
{
	SDL_GL_SetAttribute(SDL_GL_RED_SIZE, 8);
	SDL_GL_SetAttribute(SDL_GL_GREEN_SIZE, 8);
	SDL_GL_SetAttribute(SDL_GL_BLUE_SIZE, 8);
	SDL_GL_SetAttribute(SDL_GL_ALPHA_SIZE, 8);
	SDL_GL_SetAttribute(SDL_GL_BUFFER_SIZE,32);
	SDL_GL_SetAttribute(SDL_GL_DEPTH_SIZE,16);
	SDL_GL_SetAttribute(SDL_GL_DOUBLEBUFFER,1);

	SDL_GL_SetAttribute( SDL_GL_CONTEXT_PROFILE_MASK, SDL_GL_CONTEXT_PROFILE_CORE );
	SDL_GL_SetAttribute( SDL_GL_CONTEXT_MAJOR_VERSION, 3 );
	SDL_GL_SetAttribute( SDL_GL_CONTEXT_MINOR_VERSION, 2 );
	
	m_window = SDL_CreateWindow(title.c_str(), SDL_WINDOWPOS_CENTERED, SDL_WINDOWPOS_CENTERED, width, height, SDL_WINDOW_OPENGL);
	m_glContext = SDL_GL_CreateContext(m_window);

	//SDL_SetHint(SDL_HINT_RENDER_VSYNC, "1");
	SDL_GL_SetSwapInterval(1);

	//Apparently this is necessary to build with Xcode
	glewExperimental = GL_TRUE;
	
	GLenum res = glewInit();
	if(res != GLEW_OK)
	{
		std::ostringstream out;
		out << "Error '" << glewGetErrorString(res) << "'";
		throw RuntimeException(out.str());
	}

	m_renderContext = new RENDERCONTEXT();
	m_renderDevice = new RENDERDEVICE();
	m_renderTarget = new RENDERTARGET(0, m_width, m_height);
	m_input = new SDLInput(m_window);
}

SDLDisplay::~SDLDisplay()
{
	if(m_input) { delete m_input; }
	if(m_renderTarget) { delete m_renderTarget; }
	if(m_renderDevice) { delete m_renderDevice; }
	if(m_renderContext) { delete m_renderContext; }
	SDL_GL_DeleteContext(m_glContext);
	SDL_DestroyWindow(m_window);
}


void SDLDisplay::Update()
{
	m_input->Update();
	SDL_Event e;
	while(SDL_PollEvent(&e))
	{
		if(e.type == SDL_QUIT)
		{
			m_isClosed = true;
		}
		else
		{
			m_input->HandleEvent(e);
		}
	}
}

void SDLDisplay::SwapBuffers()
{
	SDL_GL_SwapWindow(m_window);
}

bool SDLDisplay::IsClosed()
{
	return m_isClosed;
}

int SDLDisplay::GetWidth()
{
	return m_width;
}

int SDLDisplay::GetHeight()
{
	return m_height;
}

IInput* SDLDisplay::GetInput()
{
	return m_input;
}

IRenderContext* SDLDisplay::GetRenderContext()
{
	return m_renderContext;
}

IRenderDevice* SDLDisplay::GetRenderDevice()
{
	return m_renderDevice;
}

IRenderTarget* SDLDisplay::GetRenderTarget()
{
	return m_renderTarget;
}
