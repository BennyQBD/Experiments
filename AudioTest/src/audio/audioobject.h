#ifndef AUDIO_OBJECT_INCLUDED_H
#define AUDIO_OBJECT_INCLUDED_H

#include "iaudiodata.h"

class AudioObject
{
public:
	AudioObject(IAudioData* audioData, SampleInfo* sampleInfo) :
		m_audioData(audioData),
		m_sampleInfo(sampleInfo) {}

	inline bool GenerateSamples(float* buffer, int bufferLength)
	{
		if(m_audioData == 0)
		{
			return false;
		}
		return m_audioData->GenerateSamples(buffer, bufferLength, *m_sampleInfo);
	}

	inline void Reset()
	{
		m_audioData->Reset();
	}

	inline bool HasAudioData(IAudioData* data) { return m_audioData == data; }
private:
	IAudioData* m_audioData;
	SampleInfo* m_sampleInfo;
};

#endif
