#ifndef SAW_WAVE_INCLUDED_H
#define SAW_WAVE_INCLUDED_H

#include "base16bitaudio.h"

class SawWave : public Base16BitAudio
{
public:
	SawWave(double frequency);
protected:
	virtual void LoadAudioData(char* buffer, long numBytes);
	virtual void MoveAudioPos(long amt);
private:
	double m_pos;
	double m_step;
};

#endif
