#ifndef I_SUBSYSTEM_INCLUDED_H
#define I_SUBSYSTEM_INCLUDED_H

#include <stdexcept>
#include "../audio/iaudiocontext.h"
#include "../audio/iaudiodevice.h"
#include "../graphics/idisplay.h"
#include "itimingsystem.h"

class ISubSystem
{
public:
	ISubSystem() {}
	virtual ~ISubSystem() {}

	virtual IDisplay* CreateDisplay(int width, int height, 
			const std::string& title, bool isFullscreen) = 0;
	virtual void ReleaseDisplay(IDisplay* display) = 0;

	virtual IAudioContext* GetAudioContext() = 0;
	virtual IAudioDevice* GetAudioDevice() = 0;
	virtual ITimingSystem* GetTimingSystem() = 0;

	class SubSystemException : public std::runtime_error
	{
	public:
		SubSystemException(const std::string& what_arg);
	};
private:
	ISubSystem(ISubSystem& other) { (void)other; }
	void operator=(const ISubSystem& other) { (void)other;}
};


#endif
