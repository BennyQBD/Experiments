#ifndef I_AUDIO_DATA_INCLUDED_H
#define I_AUDIO_DATA_INCLUDED_H

#include "sampleinfo.h"

class IAudioData
{
public:
	IAudioData() {}
	virtual ~IAudioData() {}

	virtual bool GenerateSamples(float* buffer, int bufferLength,
		   	const SampleInfo& sampleInfo) = 0;
	virtual void Reset() = 0;

private:
	IAudioData(IAudioData& other) { (void)other; }
	void operator=(const IAudioData& other) { (void)other;}
};

#endif
