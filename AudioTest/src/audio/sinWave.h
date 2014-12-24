#ifndef SIN_WAVE_INCLUDED_H
#define SIN_WAVE_INCLUDED_H

#include "base16bitaudio.h"

class SinWave : public Base16BitAudio
{
public:
	SinWave(double frequency);
protected:
	virtual void LoadAudioData(char* buffer, long numBytes);
	virtual void MoveAudioPos(long amt);
private:
	double m_pos;
	double m_step;
};

#endif
