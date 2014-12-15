#ifndef I_AUDIO_DEVICE_INCLUDED_H
#define I_AUDIO_DEVICE_INCLUDED_H

#include "iaudiodata.h"
#include <string>

class IAudioDevice
{
public:
	IAudioDevice() {}
	virtual ~IAudioDevice() {}

	virtual IAudioData* CreateAudioFromFile(const std::string& fileName, bool streamFromFile) = 0;
	virtual void ReleaseAudio(IAudioData* audioData) = 0;
private:
	IAudioDevice(IAudioDevice& other) { (void)other; }
	void operator=(const IAudioDevice& other) { (void)other;}
};

#endif
