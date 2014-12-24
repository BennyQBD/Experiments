#ifndef SQUARE_WAVE_INCLUDED_H
#define SQUARE_WAVE_INCLUDED_H

#include "base16bitaudio.h"

class SquareWave : public Base16BitAudio
{
public:
	SquareWave(double frequency);
protected:
	virtual void LoadAudioData(char* buffer, long numBytes);
	virtual void MoveAudioPos(long amt);
private:
	double m_pos;
	double m_step;
};

#endif
