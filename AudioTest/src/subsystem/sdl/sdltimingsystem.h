#ifndef SDL_TIMING_SYSTEM_INCLUDED_H
#define SDL_TIMING_SYSTEM_INCLUDED_H

#include "../itimingsystem.h"

class SDLTimingSystem : public ITimingSystem
{
public:
	virtual double GetTime();
	virtual void Sleep(unsigned int milliseconds);
};

#endif
