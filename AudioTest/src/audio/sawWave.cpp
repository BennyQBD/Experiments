#include "sawWave.h"
#include <math.h>

SawWave::SawWave(double frequency)
{
	// frequency = period/second
	// sampleRate = samples/second
	// sampleRate/frequency = samples/period
	m_pos = 0;
	m_step = frequency/(double)GetSampleRate();
//	m_sampleRate = GetSampleRate();

	// TODO: Better method of determining buffer size
	Init(GetSampleRate(), GetSampleRate()/4);
}

void SawWave::LoadAudioData(char* bufferIn, long bufferLength)
{
	short* buffer = (short*)bufferIn;
	bufferLength /= 4;

	for(long i = 0; i < bufferLength; i++)
	{
		double val = (m_pos - (int)m_pos);
		val = val * 2.0 - 1.0;

		m_pos += m_step;
		short result = (short)(32767.0 * val);

		buffer[i * 2] = result;
		buffer[i * 2 + 1] = result;
	}
}

void SawWave::MoveAudioPos(long amt)
{
	double offsetAmt = amt * m_step / 4.0;
	m_pos += offsetAmt;
}

