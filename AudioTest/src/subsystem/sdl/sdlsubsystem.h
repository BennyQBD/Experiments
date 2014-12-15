#ifndef SDL_SUBSYSTEM_INCLUDED_H
#define SDL_SUBSYSTEM_INCLUDED_H

#include "../isubsystem.h"
#include "../../audio/iaudiodevice.h"
#include "../../audio/iaudiocontext.h"

class SDLSubSystem : public ISubSystem
{
public:
	SDLSubSystem();
	virtual ~SDLSubSystem();

	virtual IDisplay* CreateDisplay(int width, int height, 
			const std::string& title, bool isFullscreen);
	virtual void ReleaseDisplay(IDisplay* display);

	virtual IAudioContext* GetAudioContext();
	virtual IAudioDevice* GetAudioDevice();
private:
	IAudioContext* m_audioContext;
	IAudioDevice* m_audioDevice;
};

#endif
