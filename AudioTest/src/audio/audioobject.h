#ifndef AUDIO_OBJECT_INCLUDED_H
#define AUDIO_OBJECT_INCLUDED_H

#include "audioData.h"

#include <iostream>
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
		bool shouldLoop = m_sampleInfo.loopStart != m_sampleInfo.loopEnd;

		if(shouldLoop && m_sampleInfo.loopStart > m_sampleInfo.loopEnd)
		{
			double temp = m_sampleInfo.loopStart;
			m_sampleInfo.loopStart = m_sampleInfo.loopEnd;
			m_sampleInfo.loopEnd = temp;
		}

		if(m_audioData.GetAudioData() == 0)
		{
			return false;
		}

		int bufferRemaining = 0;
		int audioEndPos = m_audioLength;
		if(shouldLoop)
		{
			audioEndPos = ToAbsolutePos(m_sampleInfo.loopEnd);
		}

		m_audioPos = m_audioData.GetAudioData()->GenerateSamples(buffer,
				bufferLength, m_audioPos, audioEndPos, &bufferRemaining,
				m_sampleInfo);

		if(bufferRemaining != 0)
		{
			SetPos(m_sampleInfo.loopStart);
			m_audioPos = m_audioData.GetAudioData()->GenerateSamples(
					buffer + (bufferLength - bufferRemaining),
					bufferRemaining, m_audioPos, audioEndPos, &bufferRemaining,
					m_sampleInfo);	
		}
			
		if(m_audioPos == -1)
		{
			if(shouldLoop)
			{
				SetPos(m_sampleInfo.loopStart);
				return true;
			}
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
		m_audioPos = ToAbsolutePos(pos);

		// Keep aligned to a 4-byte position.
		m_audioPos = m_audioPos & (~3);
	}

	inline bool HasAudioData(IAudioData* data)
	{
		return m_audioData.GetAudioData() == data;
	}

	inline SampleInfo* GetSampleInfo() { return &m_sampleInfo; }
private:
	inline int ToAbsolutePos(double pos)
	{
		return (int)(pos * (double)m_audioLength);
	}
	AudioData   m_audioData;
	SampleInfo  m_sampleInfo;
	int         m_audioPos;
	int         m_audioLength;
};

#endif
