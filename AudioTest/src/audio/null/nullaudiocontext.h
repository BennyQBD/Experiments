#ifndef NULL_AUDIO_CONTEXT_INCLUDED_H
#define NULL_AUDIO_CONTEXT_INCLUDED_H

#include "../iaudiocontext.h"

class NullAudioContext : public IAudioContext
{
public:
	virtual void PlayAudio(AudioObject& ao) {}
	virtual void PauseAudio(AudioObject& ao) {}
	virtual void StopAudio(AudioObject& ao) {}
};

#endif
