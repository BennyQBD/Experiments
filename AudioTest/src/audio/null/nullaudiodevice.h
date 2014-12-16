#ifndef NULL_AUDIO_DEVICE_INCLUDED_H
#define NULL_AUDIO_DEVICE_INCLUDED_H

#include "../iaudiodevice.h"

class NullAudioDevice : public IAudioDevice
{
public:
	virtual IAudioData* CreateAudioFromFile(const std::string& fileName, bool streamFromFile)
	{
		return NULL;
	}
	virtual void ReleaseAudio(IAudioData* audioData) {}
};

#endif
