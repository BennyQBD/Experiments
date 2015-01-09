#include "base16bitaudio.h"
#include <cstdlib>

Base16BitAudio::Base16BitAudio() {}

Base16BitAudio::~Base16BitAudio()
{
	free(m_bufferStart);
}

void Base16BitAudio::Init(long audioLength, long bufferSize)
{
	m_streamFromFile = bufferSize != 0;
	
	// Round the buffer size up to the next multiple of 4.
	m_bufferLength = (bufferSize + 3) & (~3);

	if(m_bufferLength == 0)
	{
		m_bufferLength = audioLength;
	}
	m_bufferStart = (char*)malloc((size_t)m_bufferLength);
	m_bufferPos = m_bufferStart + m_bufferLength;
	m_totalBufferLength = m_bufferLength;
	m_filePos = 0;
	m_fileLength = audioLength;
	m_sampleIndexCarryOver = 0.0;

	FillBuffer(m_totalBufferLength);
}

bool Base16BitAudio::FillBuffer(long amt)
{
	if(amt > m_totalBufferLength)
	{
		amt = m_totalBufferLength;
	}

	long audioLeft = (m_fileLength - m_filePos);

	if(audioLeft <= 0)
	{
		return false;
	}
	
	long bufferLeft = GetBufferLeft();

	// If there are already enough samples loaded, no need to do any buffer
	// filling.
	if(amt <= bufferLeft)
	{
		return true;
	}

	// Move whatever is left to be played to the start of the buffer.
	for(long i = 0; i < bufferLeft; i++)
	{
		m_bufferStart[i] = *m_bufferPos;
		m_bufferPos++;
	}

	m_bufferPos = (m_bufferStart + bufferLeft);
	long readAmt = (amt - bufferLeft);

	long logicalBufferLength = amt;
	if(audioLeft < readAmt)
	{
		readAmt = audioLeft;
		logicalBufferLength = audioLeft + bufferLeft;
	}
	m_filePos += readAmt;

	LoadAudioData(m_bufferPos, readAmt);
	m_bufferPos = m_bufferStart;
	m_bufferLength = logicalBufferLength;

	return true;
}

bool Base16BitAudio::GotoAudioPos(long audioPos, unsigned int neededSamples)
{
	long currentPos = GetCurrentAudioPos();

	if(audioPos == currentPos)
	{
		return true;
	}

	m_sampleIndexCarryOver = 0.0;
	if(audioPos >= m_fileLength || audioPos < 0)
	{
		return false;
	}

	char* bufferEnd = m_bufferStart + m_bufferLength;

	long distance = audioPos - currentPos;
	char* newBufferPos = m_bufferPos + distance;
	
	bool insideBufferStart = newBufferPos >= m_bufferStart;
	bool insideBufferEnd = newBufferPos <= bufferEnd;
	if(insideBufferStart && insideBufferEnd)
	{
		m_bufferPos = newBufferPos;
		return true;
	}
	else if(!m_streamFromFile)
	{
		return false;
	}

	// At this point, stream from file code begins

	long seekDistance = (newBufferPos - bufferEnd);
	MoveAudioPos(seekDistance);
	m_bufferPos = bufferEnd;
	m_filePos += seekDistance;
	FillBuffer(neededSamples);
	return true;
}

int Base16BitAudio::GenerateSamples(float* buffer, int bufferLength, int audioPos,
		int audioEndPos, int* bufferRemaining,
		const SampleInfo& sampleInfo)
{	
	float volume = (float)(1.0 + sampleInfo.volume);
	float pitchAdjust = (float)(1.0 + sampleInfo.pitchAdjust);

	unsigned int predictedNeededSamples = 
		(unsigned int)((bufferLength * 2) * pitchAdjust * 1.1) + 8;

	// Make sure the needed samples are a multiple of 4
	predictedNeededSamples = predictedNeededSamples & (~3);
	if(predictedNeededSamples > m_totalBufferLength)
	{
		predictedNeededSamples = (unsigned int)m_totalBufferLength;
	}
	if(!GotoAudioPos(audioPos, predictedNeededSamples))
	{
		return -1;
	}

	long maxCopyDistance = (long)(((audioEndPos - audioPos)/4 - 1)/ pitchAdjust);
	long bufferStartIndex = 0;
	long neededLen = (long)bufferLength / 2;
	*bufferRemaining = 0;
	if(neededLen > maxCopyDistance)
	{
		*bufferRemaining = (int)(neededLen - maxCopyDistance) * 2;
		neededLen = maxCopyDistance;
	}
	do
	{
		// Since linear sampling is used, leave off 1 sample. This way, the
		// linear sampler will never read outside the buffer
		long bufferLeft = GetBufferLeft() / 4 - 1;
		if(bufferLeft < 0)
		{
			bufferLeft = 0;
		}
		long adjustedBufferLeft = (long)(bufferLeft / pitchAdjust);
		
		long len = adjustedBufferLeft < neededLen ? adjustedBufferLeft : neededLen;
		long bufferEndIndex = bufferStartIndex + len;
		
		int* samples = (int*)m_bufferPos;
		double sampleIndex = m_sampleIndexCarryOver;
		for(long i = bufferStartIndex; i < bufferEndIndex; i++)
		{
			int sample = samples[(size_t)sampleIndex];
			int nextSample = samples[(size_t)sampleIndex + 1];

			short sample1 = (short)(sample & 0xFFFF);
			short sample2 = (short)((sample >> 16) & 0xFFFF);

			short nextSample1 = (short)(nextSample & 0xFFFF);
			short nextSample2 = (short)((nextSample >> 16) & 0xFFFF);

			double factor2 = sampleIndex - (int)sampleIndex;
			double factor1 = 1.0f - factor2;

			sample1 = (short)(factor1 * sample1 + factor2 * nextSample1);
			sample2 = (short)(factor1 * sample2 + factor2 * nextSample2);

			buffer[i * 2] += volume * (float)(sample1);
			buffer[i * 2 + 1] += volume * (float)(sample2);
			sampleIndex += pitchAdjust;
		}
		m_bufferPos = (char*)(samples + (size_t)sampleIndex);
		neededLen -= len;
		bufferStartIndex = bufferEndIndex;
		m_sampleIndexCarryOver = sampleIndex - (int)sampleIndex;

		if(neededLen == 0)
		{
			return (int)GetCurrentAudioPos();
		}
		
		if(!FillBuffer(m_totalBufferLength))
		{
			return -1;
		}
	} while(true);
}

int Base16BitAudio::GetAudioLength()
{
	return (int)m_fileLength;
}

int Base16BitAudio::GetSampleRate()
{
	// TODO: Don't hardcode this.
	return 44100;
}
