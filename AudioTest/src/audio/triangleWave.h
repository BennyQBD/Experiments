#ifndef TRIANGLE_WAVE_INCLUDED_H
#define TRIANGLE_WAVE_INCLUDED_H

#include "base16bitaudio.h"

class TriangleWave : public Base16BitAudio
{
public:
	TriangleWave(double frequency);
protected:
	virtual void LoadAudioData(char* buffer, long numBytes);
	virtual void MoveAudioPos(long amt);
private:
	double m_pos;
	double m_step;
};

#endif
