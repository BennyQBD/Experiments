#ifndef CORE_ENGINE_INCLUDED_H
#define CORE_ENGINE_INCLUDED_H

#include "iscene.h"
#include "enginesystems.h"

#include "../graphics/idisplay.h"
#include "../subsystem/itimingsystem.h"

class CoreEngine
{
public:
	CoreEngine(double frameRate, IDisplay* display, IAudioContext* audioContext, 
			IAudioDevice* audioDevice, ITimingSystem* timingSystem,
			IRenderer* renderer, IScene* scene);
	
	void Start();
	void Stop();
protected:
private:
	bool           m_isRunning;
	double         m_frameTime;
	EngineSystems  m_systems;
	IDisplay*      m_display;
	ITimingSystem* m_timingSystem;
	IRenderer*     m_renderer;
	IScene*        m_scene;
	ResourceManager m_resources;
};


#endif
