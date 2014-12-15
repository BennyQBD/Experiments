#ifndef SDL_AUDIO_DEVICE_INCLUDED_H
#define SDL_AUDIO_DEVICE_INCLUDED_H

#include "../iaudiodevice.h"

class SDLAudioDevice : public IAudioDevice
{
public:
	virtual IAudioData* CreateAudioFromFile(const std::string& fileName, bool streamFromFile);
	virtual void ReleaseAudio(IAudioData* audioData);
};

#endif
