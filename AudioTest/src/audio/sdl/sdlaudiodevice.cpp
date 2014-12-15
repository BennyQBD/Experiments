#include "sdlaudiodevice.h"
#include "sdlwavaudiodata.h"

// TODO: There is no reason this needs to be SDL specific.

IAudioData* SDLAudioDevice::CreateAudioFromFile(const std::string& fileName, bool streamFromFile)
{
	return new SDLWAVAudioData(fileName, streamFromFile);
}

void SDLAudioDevice::ReleaseAudio(IAudioData* audioData)
{
	if(!audioData)
	{
		return;
	}
	
	delete audioData;
}

