#ifndef BASE_16_BIT_AUDIO_INCLUDED_H
#define BASE_16_BIT_AUDIO_INCLUDED_H

#include "iaudiodata.h"
#include <string>

class Base16BitAudio : public IAudioData
{
public:
	Base16BitAudio(long bufferSize);
	virtual ~Base16BitAudio();

	virtual int GenerateSamples(float* buffer, int bufferLength, int audioPos,
		const SampleInfo& sampleInfo);
	virtual int GetAudioLength();
	virtual int GetSampleRate();
protected:
	void Init(long audioLength);
	virtual void LoadAudioData(char* buffer, long numBytes) = 0;
	virtual void MoveAudioPos(long amt) = 0;
private:
	char*  m_bufferStart;
	char*  m_bufferPos;
	long   m_bufferLength;
	long   m_totalBufferLength;
	long   m_filePos;
	long   m_fileLength;
	double m_sampleIndexCarryOver;
	bool   m_streamFromFile;

	bool FillBuffer(long amt);
	bool GotoAudioPos(long audioPos, unsigned int neededSamples);

	inline long GetBufferLeft() 
	{
		return (m_bufferLength - (long)(m_bufferPos - m_bufferStart));
	}

	inline long GetCurrentAudioPos()
	{
		return m_filePos - GetBufferLeft();
	}

	Base16BitAudio(Base16BitAudio& other) { (void)other; }
	void operator=(const Base16BitAudio& other) { (void)other;}
};


#endif
