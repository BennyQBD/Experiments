#ifndef AUDIO_OBJECT_INCLUDED_H
#define AUDIO_OBJECT_INCLUDED_H

#include "iaudiodata.h"

class AudioObject
{
public:
	AudioObject(IAudioData* audioData, SampleInfo* sampleInfo) :
		m_audioData(audioData),
		m_sampleInfo(sampleInfo),
		m_audioPos(0) {}

	inline bool GenerateSamples(float* buffer, int bufferLength)
	{
		if(m_audioData == 0)
		{
			return false;
		}
		m_audioPos = m_audioData->GenerateSamples(buffer, bufferLength, 
				m_audioPos, *m_sampleInfo);
		if(m_audioPos == -1)
		{
			m_audioPos = 0;
			return false;
		}

		return true;
	}

	inline void Reset()
	{
		m_audioPos = 0;
	}

	inline bool HasAudioData(IAudioData* data) { return m_audioData == data; }
private:
	IAudioData* m_audioData;
	SampleInfo* m_sampleInfo;
	int         m_audioPos;
};

#endif
