#ifndef I_AUDIO_DATA_INCLUDED_H
#define I_AUDIO_DATA_INCLUDED_H

#include "sampleinfo.h"

class IAudioData
{
public:
	IAudioData() {}
	virtual ~IAudioData() {}

	virtual int GenerateSamples(float* buffer, int bufferLength, int audioPos,
			int audioEndPos, int* bufferLeft,
		   	const SampleInfo& sampleInfo) = 0;
	virtual int GetAudioLength() = 0;
	virtual int GetSampleRate() = 0;
private:
	IAudioData(IAudioData& other) { (void)other; }
	void operator=(const IAudioData& other) { (void)other;}
};

#endif
