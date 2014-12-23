#ifndef BASIC_SOUND_INCLUDED_H
#define BASIC_SOUND_INCLUDED_H

#include "entityComponent.h"
#include "../audio/audioobject.h"

class BasicSound : public EntityComponent
{
public:
	BasicSound(const AudioObject& audio) :
		m_audio(audio),
		m_playCount(0) {}

	virtual void Update(EngineSystems& systems, float delta)
	{
		// intervalX = 2^(X/12).
		// Precalculated for convenience.	
		TestInput(systems, IInput::KEY_Q, 1.0);
		TestInput(systems, IInput::KEY_W, 1.0594630943593);
		TestInput(systems, IInput::KEY_E, 1.12246204830937);
		TestInput(systems, IInput::KEY_R, 1.18920711500272);
		TestInput(systems, IInput::KEY_T, 1.25992104989487);
		TestInput(systems, IInput::KEY_Y, 1.33483985417003);
		TestInput(systems, IInput::KEY_U, 1.4142135623731);
		TestInput(systems, IInput::KEY_I, 1.49830707687668);
		TestInput(systems, IInput::KEY_O, 1.5874010519682);
		TestInput(systems, IInput::KEY_P, 1.68179283050743);
		TestInput(systems, IInput::KEY_LEFTBRACKET, 1.78179743628068);
		TestInput(systems, IInput::KEY_RIGHTBRACKET, 1.88774862536339);

		TestInput(systems, IInput::KEY_BACKSLASH, 1.0);
		TestInput(systems, IInput::KEY_EQUALS, 1.0/1.0594630943593);
		TestInput(systems, IInput::KEY_MINUS, 1.0/1.12246204830937);
		TestInput(systems, IInput::KEY_0, 1.0/1.18920711500272);
		TestInput(systems, IInput::KEY_9, 1.0/1.25992104989487);
		TestInput(systems, IInput::KEY_8, 1.0/1.33483985417003);
		TestInput(systems, IInput::KEY_7, 1.0/1.4142135623731);
		TestInput(systems, IInput::KEY_6, 1.0/1.49830707687668);
		TestInput(systems, IInput::KEY_5, 1.0/1.5874010519682);
		TestInput(systems, IInput::KEY_4, 1.0/1.68179283050743);
		TestInput(systems, IInput::KEY_3, 1.0/1.78179743628068);
		TestInput(systems, IInput::KEY_2, 1.0/1.88774862536339);
		TestInput(systems, IInput::KEY_1, 1.0/2.0);
	}
private:
	AudioObject m_audio;
	int m_playCount;

	void TestInput(EngineSystems& systems, int key, double pitch)
	{
		if(m_playCount < 0)
		{
			m_playCount = 0;
		}

		if(systems.input->GetKeyUp(key))
		{
			m_playCount--;
			if(m_playCount == 0)
			{
				systems.audio->StopAudio(m_audio);
			}
		}
		if(systems.input->GetKeyDown(key))
		{
			//systems.audio->StopAudio(m_audio);
			m_audio.GetSampleInfo()->pitchAdjust = pitch - 1.0;
			systems.audio->PlayAudio(m_audio);
			m_playCount++;
		}
	}
};

#endif
