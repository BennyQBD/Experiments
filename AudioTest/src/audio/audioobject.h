#ifndef AUDIO_OBJECT_INCLUDED_H
#define AUDIO_OBJECT_INCLUDED_H

#include "audioData.h"

class AudioObject
{
public:
	AudioObject(AudioData& audioData, const SampleInfo& sampleInfo) :
		m_audioData(audioData),
		m_sampleInfo(sampleInfo),
		m_audioPos(0),
		m_audioLength(audioData.GetAudioData()->GetAudioLength()) {}

	inline bool GenerateSamples(float* buffer, int bufferLength)
	{
		if(m_audioData.GetAudioData() == 0)
		{
			return false;
		}
		m_audioPos = m_audioData.GetAudioData()->GenerateSamples(buffer, bufferLength, 
				m_audioPos, m_sampleInfo);
		if(m_audioPos == -1)
		{
			m_audioPos = 0;
			return false;
		}

		return true;
	}

	inline void Reset()
	{
		SetPos(0.0);
	}

	inline double GetPos()  
	{
		return (double)m_audioPos/(double)m_audioLength;
	}
	
	inline double GetSecond()
	{
		return 4.0 * m_audioData.GetAudioData()->GetSampleRate()/(double)m_audioLength;
	}

	inline double GetLengthInSeconds()
	{
		return 1.0/GetSecond();
	}
	
	inline void SetPos(double pos)
	{
		if(pos > 1.0)
		{
			pos = 1.0;
		}
		if(pos < 0.0)
		{
			pos = 0.0;
		}
		m_audioPos = (int)(pos * (double)m_audioLength);
	}

	inline bool HasAudioData(IAudioData* data)
	{
		return m_audioData.GetAudioData() == data;
	}

	inline SampleInfo* GetSampleInfo() { return &m_sampleInfo; }
private:
	AudioData   m_audioData;
	SampleInfo  m_sampleInfo;
	int         m_audioPos;
	int         m_audioLength;
};

#endif
