#ifndef SDL_WAV_AUDIO_DATA_INCLUDED_H
#define SDL_WAV_AUDIO_DATA_INCLUDED_H

#include "../base16bitaudio.h"
#include <string>
#include <SDL2/SDL.h>

class SDLWAVAudioData : public Base16BitAudio
{
public:
	SDLWAVAudioData(const std::string& fileName, bool streamFromFile);
	virtual ~SDLWAVAudioData();
protected:
	virtual void LoadAudioData(char* buffer, long numBytes);
	virtual void MoveAudioPos(long amt);
private:
	SDL_RWops* m_src;
	std::string m_fileName;

	SDLWAVAudioData(SDLWAVAudioData& other) : 
		Base16BitAudio(0)
	{ 
		(void)other;
	}
	void operator=(const SDLWAVAudioData& other) { (void)other;}
};

#endif
