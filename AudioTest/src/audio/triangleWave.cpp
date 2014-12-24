#include "triangleWave.h"
#include <math.h>

TriangleWave::TriangleWave(double frequency)
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

void TriangleWave::LoadAudioData(char* bufferIn, long bufferLength)
{
	short* buffer = (short*)bufferIn;
	bufferLength /= 4;

	for(long i = 0; i < bufferLength; i++)
	{
		// Correct equations:
		// SawWave(v, f, r, c)      = (v*((c % r) * 2) - v*r)/r
		// TriangleWave(v, f, r, c) = (v*abs(c % r - r/2) * 2) - v*(r/2))/(r/2)
		// TriangleWave(v, f, r, c) = 2*(v*abs(c % r - r/2) * 2) - v*(r/2))/r
		// TriangleWave(v, f, r, c) = (v*abs(c % r - r/2) * 4) - v*r))/r

		// Note: To get convex/concave curves rather than lines, use pow(absEquation, power),
		// and in final equation divide by pow(sampleRate, power)

		double val = 2.0 * fabs((m_pos - (int)m_pos) - 0.5);
		val = val * 2 - 1.0;
		m_pos += m_step;

		short result = (short)(32767.0 * val);

		buffer[i * 2] = result;
		buffer[i * 2 + 1] = result;
	}
}

void TriangleWave::MoveAudioPos(long amt)
{
	double offsetAmt = amt * m_step / 4.0;
	m_pos += offsetAmt;
}

