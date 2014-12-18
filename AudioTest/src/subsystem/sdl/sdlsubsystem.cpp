#include "sdlsubsystem.h"
#include "sdltimingsystem.h"
#include "../../audio/sdl/sdlaudiocontext.h"
#include "../../audio/sdl/sdlaudiodevice.h"
#include "../../audio/null/nullaudiodevice.h"
#include "../../audio/null/nullaudiocontext.h"
#include "../../graphics/sdl/sdldisplay.h"
#include <sstream>

//#define AUDIO_CONTEXT SDLAudioContext
//#define AUDIO_DEVICE SDLAudioDevice

#define AUDIO_CONTEXT NullAudioContext
#define AUDIO_DEVICE NullAudioDevice

#define TIMING_SYSTEM SDLTimingSystem

SDLSubSystem::SDLSubSystem()
{
	SDL_Init(SDL_INIT_EVERYTHING);
	if(SDL_Init(SDL_INIT_EVERYTHING) != 0)
	{
		std::ostringstream out;
		out << "SubSystem Error: SDL failed to initialize. " << SDL_GetError();
		std::string result = out.str();
		SDL_ClearError();
		throw SubSystemException(result);
	}

	m_audioContext = new AUDIO_CONTEXT();
	m_audioDevice = new AUDIO_DEVICE();
	m_timingSystem = new TIMING_SYSTEM();
}

SDLSubSystem::~SDLSubSystem()
{
	if(m_timingSystem) { delete m_timingSystem; }
	if(m_audioDevice) { delete m_audioDevice; }
	if(m_audioContext) { delete m_audioContext; }
	SDL_Quit();
}

IAudioContext* SDLSubSystem::GetAudioContext()
{
	return m_audioContext;
}

IAudioDevice* SDLSubSystem::GetAudioDevice()
{
	return m_audioDevice;
}

ITimingSystem* SDLSubSystem::GetTimingSystem()
{
	return m_timingSystem;
}

IDisplay* SDLSubSystem::CreateDisplay(int width, int height, 
		const std::string& title, bool isFullscreen)
{
	return new SDLDisplay(width, height, title, isFullscreen);
}

void SDLSubSystem::ReleaseDisplay(IDisplay* display)
{
	if(display) { delete display; };
}
