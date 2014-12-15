#ifndef I_AUDIO_CONTEXT_INCLUDED_H
#define I_AUDIO_CONTEXT_INCLUDED_H

#include "audioobject.h"
#include <string>

class IAudioContext
{
public:
	IAudioContext() {}
	virtual ~IAudioContext() {}

	virtual void PlayAudio(AudioObject& ao) = 0;
	virtual void PauseAudio(AudioObject& ao) = 0;
	virtual void StopAudio(AudioObject& ao) = 0;

private:
	IAudioContext(IAudioContext& other) { (void)other; }
	void operator=(const IAudioContext& other) { (void)other;}
};

#endif
