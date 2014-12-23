#ifndef ENGINE_SYSTEMS_INCLUDED_H
#define ENGINE_SYSTEMS_INCLUDED_H

#include "iinput.h"
#include "../audio/iaudiocontext.h"

struct EngineSystems
{
	IInput* input;
	IAudioContext* audio;
};

#endif
