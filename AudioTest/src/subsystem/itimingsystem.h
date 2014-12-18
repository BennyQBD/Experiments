#ifndef I_TIMING_SYSTEM_INCLUDED_H
#define I_TIMING_SYSTEM_INCLUDED_H

class ITimingSystem
{
public:
	virtual ~ITimingSystem() {}
	virtual double GetTime() = 0;
	virtual void Sleep(unsigned int milliseconds) = 0;
};

#endif
