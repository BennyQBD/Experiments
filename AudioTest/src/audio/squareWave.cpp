#include "squareWave.h"
#include <math.h>

SquareWave::SquareWave(double frequency)
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

void SquareWave::LoadAudioData(char* bufferIn, long bufferLength)
{
	short* buffer = (short*)bufferIn;
	bufferLength /= 4;

	for(long i = 0; i < bufferLength; i++)
	{
		double val = (m_pos - (int)m_pos);
		m_pos += m_step;
		short result = val > 0.5 ? 32767 : -32768;

		buffer[i * 2] = result;
		buffer[i * 2 + 1] = result;
	}
}

void SquareWave::MoveAudioPos(long amt)
{
	double offsetAmt = amt * m_step / 4.0;
	m_pos += offsetAmt;
}

