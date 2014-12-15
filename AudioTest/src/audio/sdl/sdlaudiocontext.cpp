#include "sdlaudiocontext.h"
#include "../../subsystem/isubsystem.h"

#include <stdexcept>
#include <sstream>

static void SDLAudioContext_AudioCallback(void* userData, Uint8* streamIn, int length);

SDLAudioContext::SDLAudioContext()
{
	SDL_AudioSpec desiredSpec;

	SDL_zero(desiredSpec);
	desiredSpec.freq = 44100;
	desiredSpec.format = AUDIO_S16SYS;
	desiredSpec.channels = 2;
	desiredSpec.samples = 2048;
	desiredSpec.callback = SDLAudioContext_AudioCallback;
	desiredSpec.userdata = this;

	SDL_AudioSpec obtainedSpec;

	m_device = SDL_OpenAudioDevice(NULL, 0, &desiredSpec, &obtainedSpec,
		   SDL_AUDIO_ALLOW_FREQUENCY_CHANGE);
	if(m_device == 0)
	{
		std::ostringstream out;
		out << "SubSystem Error: Couldn't open audio: " << SDL_GetError();
		std::string result = out.str();
		SDL_ClearError();
		throw ISubSystem::SubSystemException(result);
	}
	
	// Start playing audio
	SDL_PauseAudioDevice(m_device, 0);
}

SDLAudioContext::~SDLAudioContext()
{
	SDL_CloseAudioDevice(m_device);
}

void SDLAudioContext::PlayAudio(AudioObject& ao)
{
	SDL_LockAudioDevice(m_device);

	//Remove any copies of this object first to avoid duplicates
	RemoveAudio(ao);
	
	m_playingAudio.push_back(&ao);
	SDL_UnlockAudioDevice(m_device);
}

void SDLAudioContext::PauseAudio(AudioObject& ao)
{
	SDL_LockAudioDevice(m_device);
	RemoveAudio(ao);
	SDL_UnlockAudioDevice(m_device);
}

void SDLAudioContext::StopAudio(AudioObject& ao)
{
	SDL_LockAudioDevice(m_device);
	if(RemoveAudio(ao))
	{
		ao.Reset();
	}
	SDL_UnlockAudioDevice(m_device);
}



void SDLAudioContext::GenerateSamples(Uint8* streamIn, int length)
{
	int bufferLength = length/2;

	// Make sure the float buffer is big enough, then retrieve it from
	// the vector.
	m_audioBuffer.reserve((size_t)bufferLength);
	float* buffer = *(float**)(&m_audioBuffer);
	
	// Apparently memsetting this to 0 doesn't work.
	for(int i = 0; i < bufferLength; i++)
	{
		buffer[i] = 0.0f;
	}
	
	// Collect all the samples from the playing audio sources.
	std::vector<AudioObject*>::iterator it = m_playingAudio.begin();
	std::vector<AudioObject*>::iterator end = m_playingAudio.end();
	for(; it != end; ++it)
	{
		if(!(*it)->GenerateSamples(buffer, bufferLength))
		{
			// FIXME: This seems to work right now, but I'm not confident this 
			// will work in all cases.
			RemoveAudio(*(*it));
		}
	}

	// Finally, copy the mixed audio stream into the output buffer
	Sint16* stream = (Sint16*)streamIn;
	for(int i = 0; i < bufferLength; i++)
	{
		float val = buffer[i];

		// Clamp the audio val, effectively limiting it into a valid 
		// range.
		if(val >= 32767.0f)
		{
			stream[i] = 32767;
		}
		else if(val <= -32768.0f)
		{
			stream[i] = -32768;
		}
		else
		{
			stream[i] = (Sint16)val;
		}
	}
}

bool SDLAudioContext::RemoveAudio(AudioObject& ao)
{
	std::vector<AudioObject*>::iterator it = m_playingAudio.begin();
	std::vector<AudioObject*>::iterator end = m_playingAudio.end();
	for(; it != end; ++it)
	{
		if(*it == &ao)
		{
			m_playingAudio.erase(it);
			return true;
		}
	}

	return false;
}



static void SDLAudioContext_AudioCallback(void* userData, Uint8* streamIn, int length)
{
	SDLAudioContext* context = (SDLAudioContext*)userData;
	context->GenerateSamples(streamIn, length);
}

