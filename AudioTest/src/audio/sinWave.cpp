#include "sinWave.h"
#include "../core/math3d.h"

SinWave::SinWave(double frequency)
{
	// frequency = period/second
	// sampleRate = samples/second
	// sampleRate/frequency = samples/period
	m_pos = 0;
	m_step = frequency/(double)GetSampleRate() * 2.0 * MATH_PI;

	// TODO: Better method of determining buffer size
	Init(GetSampleRate(), GetSampleRate()/4);
}

void SinWave::LoadAudioData(char* bufferIn, long bufferLength)
{
	short* buffer = (short*)bufferIn;
	bufferLength /= 4;

	for(long i = 0; i < bufferLength; i++)
	{
		double val = sin(m_pos);
		short result = (short)(val * 32767.0);
		buffer[i * 2]     = result;
		buffer[i * 2 + 1] = result;
		m_pos += m_step;
	}
}

void SinWave::MoveAudioPos(long amt)
{
	double offsetAmt = amt * m_step / 4.0;
	m_pos += offsetAmt;
}

