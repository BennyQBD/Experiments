#ifndef SDL_WAV_AUDIO_DATA_INCLUDED_H
#define SDL_WAV_AUDIO_DATA_INCLUDED_H

#include "../iaudiodata.h"
#include <string>
#include <SDL2/SDL.h>

class SDLWAVAudioData : public IAudioData
{
public:
	SDLWAVAudioData(const std::string& fileName, bool streamFromFile);
	virtual ~SDLWAVAudioData();

	virtual int GenerateSamples(float* buffer, int bufferLength, int audioPos,
		const SampleInfo& sampleInfo);
	virtual void Reset();
private:
	SDL_RWops* m_src;
	Uint8* m_bufferStart;
	Uint8* m_bufferPos;
	Uint32 m_bufferLength;
	Uint32 m_filePos;
	Uint32 m_fileLength;
	std::string m_fileName;
	bool m_streamFromFile;

	bool FillBuffer();
	bool GotoAudioPos(int audioPos);
	void Init();
	void DeInit();

	inline Uint32 GetBufferLeft() 
	{
		return (m_bufferLength - (Uint32)(m_bufferPos - m_bufferStart));
	}

	inline Uint32 GetCurrentAudioPos()
	{
		return m_filePos - GetBufferLeft();
	}

	SDLWAVAudioData(SDLWAVAudioData& other) { (void)other; }
	void operator=(const SDLWAVAudioData& other) { (void)other;}
};

#endif
