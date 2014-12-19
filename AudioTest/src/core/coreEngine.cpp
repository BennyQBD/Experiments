#include "coreEngine.h"

#include <stdio.h>

CoreEngine::CoreEngine(double frameRate, IDisplay* display, ITimingSystem* timingSystem,
	   IRenderer* renderer, IScene* scene) :
	m_isRunning(false),
	m_frameTime(1.0/frameRate),
	m_display(display),
	m_timingSystem(timingSystem),
	m_renderer(renderer),
	m_scene(scene),
	m_resources(display->GetRenderDevice())
{
	//Scene is initialized here because this is the point where all rendering systems
	//are initialized, and so creating meshes/textures/etc. will not fail due
	//to missing context.
	m_scene->Init(&m_resources,
		(float)m_display->GetWidth()/(float)m_display->GetHeight());
}

void CoreEngine::Start()
{
	if(m_isRunning)
	{
		return;
	}
		
	m_isRunning = true;

	double lastTime = m_timingSystem->GetTime();
	double frameCounter = 0;
	double unprocessedTime = 0;
	int frames = 0;

	while(m_isRunning)
	{
		bool render = false;

		double startTime = m_timingSystem->GetTime();
		double passedTime = startTime - lastTime;
		lastTime = startTime;

		unprocessedTime += passedTime;
		frameCounter += passedTime;

		//The engine displays profiling statistics after every second because it needs to display them at some point.
		//The choice of once per second is arbitrary, and can be changed as needed.
		if(frameCounter >= 1.0)
		{
			double totalTime = ((1000.0 * frameCounter)/((double)frames));
			printf("%f ms\n", totalTime);
			frames = 0;
			frameCounter = 0;
		}

		//The engine works on a fixed update system, where each update is 1/frameRate seconds of time.
		//Because of this, there can be a situation where there is, for instance, a fixed update of 16ms, 
		//but 20ms of actual time has passed. To ensure all time is accounted for, all passed time is
		//stored in unprocessedTime, and then the engine processes as much time as it can. Any
		//unaccounted time can then be processed later, since it will remain stored in unprocessedTime.
		while(unprocessedTime > m_frameTime)
		{
			m_display->Update();
			
			if(m_display->IsClosed())
			{
				Stop();
			}
			
			m_scene->Update(m_display->GetInput(), (float)m_frameTime);
			render = true;
			unprocessedTime -= m_frameTime;
		}

		if(render)
		{
			m_scene->Render(m_renderer);
			
			//The newly rendered image will be in the window's backbuffer,
			//so the buffers must be swapped to display the new image.
			m_display->SwapBuffers();
			frames++;
		}
		else
		{
			//If no rendering is needed, sleep for some time so the OS
			//can use the processor for other tasks.
			m_timingSystem->Sleep(1);
		}
	}
}

void CoreEngine::Stop()
{
	m_isRunning = false;
}

