#include "sdlwavaudiodata.h"
#include <SDL2/SDL.h>
#include <stdexcept>
#include <sstream>

#define BUFFER_SIZE (1024 * 32)

//Structure definitions blatantly taken from SDL2 source code.
typedef struct WavFormat
{
/* Not saved in the chunk we read:
    Uint32  FMTchunk;
    Uint32  fmtlen;
*/
    Uint16 encoding;
    Uint16 channels;            /* 1 = mono, 2 = stereo */
    Uint32 frequency;           /* One of 11025, 22050, or 44100 Hz */
    Uint32 byterate;            /* Average bytes per second */
    Uint16 blockalign;          /* Bytes per sample block */
    Uint16 bitspersample;       /* One of 8, 12, 16, or 4 for ADPCM */
} WavFormat;

/* The general chunk found in the WAVE file */
typedef struct WavChunk
{
    Uint32 header;
    Uint32 length;
    Uint8 *data;
} WavChunk;

static int ReadWavChunkData(SDL_RWops* src, Uint8* data, Uint32 length);
static SDL_AudioSpec* OpenWavFile(SDL_RWops* src,
                                  SDL_AudioSpec* spec,
                                  Uint32* length);
static void SkipWavChunkData(SDL_RWops* src, Uint32 length);
static Uint32 ReadLE32(SDL_RWops* src);
static void ReadWavChunkHeader(SDL_RWops* src, Uint32* header, Uint32* length);

SDLWAVAudioData::SDLWAVAudioData(const std::string& fileName, bool streamFromFile)
{
	m_fileName = fileName;
	m_streamFromFile = streamFromFile;
	Init();
}

SDLWAVAudioData::~SDLWAVAudioData()
{
	DeInit();
}

void SDLWAVAudioData::Init()
{
	Uint32 wavLength;
	Uint8* wavBuffer;
	SDL_AudioSpec wavSpec;

	m_src = SDL_RWFromFile(m_fileName.c_str(), "rb");
	if(OpenWavFile(m_src, &wavSpec, &wavLength) == NULL)
	{
		std::ostringstream stream;
		stream << "Error: " << m_fileName << " could not be loaded as a WAV audio file.";
		throw std::runtime_error(stream.str());
	}
	
	if(m_streamFromFile)
	{
		m_bufferLength = BUFFER_SIZE;
	}
	else
	{
		m_bufferLength = wavLength;
	}
	wavBuffer = (Uint8*)malloc(m_bufferLength);
	m_bufferStart = wavBuffer;
	m_bufferPos = wavBuffer + m_bufferLength;
	m_totalBufferLength = m_bufferLength;
	m_filePos = 0;
	m_fileLength = wavLength;
	m_fileName = m_fileName;

	FillBuffer();
}

void SDLWAVAudioData::DeInit()
{
	free(m_bufferStart);
	SDL_RWclose(m_src);
}

bool SDLWAVAudioData::FillBuffer()
{
	Uint32 audioLeft = (m_fileLength - m_filePos);

	if(audioLeft <= 0)
	{
		return false;
	}
	
	Uint32 bufferLeft = GetBufferLeft();

	// Move whatever is left to be played to the start of the buffer.
	for(Uint32 i = 0; i < bufferLeft; i++)
	{
		m_bufferStart[i] = *m_bufferPos;
		m_bufferPos++;
	}

	m_bufferPos = (m_bufferStart + bufferLeft);
	Uint32 readAmt = (m_totalBufferLength - bufferLeft);

	Uint32 logicalBufferLength = m_totalBufferLength;
	if(audioLeft < readAmt)
	{
		readAmt = audioLeft;
		logicalBufferLength = audioLeft + bufferLeft;
	}
	m_filePos += readAmt;

	ReadWavChunkData(m_src, m_bufferPos, readAmt);
	m_bufferPos = m_bufferStart;

	m_bufferLength = logicalBufferLength;
	// TODO: If data is encoded, this is where it would be decoded.

	return true;
}

bool SDLWAVAudioData::GotoAudioPos(int audioPosIn)
{
	Uint32 audioPos = (Uint32)audioPosIn;
	Uint32 currentPos = GetCurrentAudioPos();

	if(audioPos == currentPos)
	{
		return true;
	}
	if(audioPos >= m_fileLength || audioPosIn < 0)
	{
		return false;
	}

	Uint8* bufferEnd = m_bufferStart + m_bufferLength;

	Sint32 distance = (Sint32)audioPos - (Sint32)currentPos;
	Uint8* newBufferPos = m_bufferPos + distance;
	
	bool insideBufferStart = newBufferPos >= m_bufferStart;
	bool insideBufferEnd = newBufferPos <= bufferEnd;
	if(insideBufferStart && insideBufferEnd && !m_streamFromFile)
	{
		m_bufferPos = newBufferPos;
		return true;
	}
	else if(!m_streamFromFile)
	{
		return false;
	}

	// At this point, stream from file code begins

	Sint64 seekDistance = (newBufferPos - bufferEnd);
	m_bufferPos = bufferEnd;
	m_filePos += seekDistance;
	SDL_RWseek(m_src, seekDistance, RW_SEEK_CUR);
	FillBuffer();
	return true;
}

int SDLWAVAudioData::GenerateSamples(float* buffer, int bufferLength, int audioPos,
		const SampleInfo& sampleInfo)
{
	if(!GotoAudioPos(audioPos))
	{
		return -1;
	}
	
	float volume = 1.0f + (float)sampleInfo.volume;
	float pitchAdjust = 1.0f + (float)sampleInfo.pitchAdjust;
	if((float)sampleInfo.pitchAdjust < 0.0f)
	{
		pitchAdjust = 1.0f/(((float)sampleInfo.pitchAdjust * -1.0f) + 1.0f);
	}

	Uint32 bufferStartIndex = 0;
	Uint32 neededLen = (Uint32)bufferLength / 2;
	float sampleIndexCarryOver = 0.0f;
	do
	{
		Uint32 bufferLeft = GetBufferLeft() / 4;
		Uint32 adjustedBufferLeft = (Uint32)(bufferLeft / pitchAdjust);
		
		Uint32 len = adjustedBufferLeft < neededLen ? adjustedBufferLeft : neededLen;
		Uint32 bufferEndIndex = bufferStartIndex + len;
		
		Sint32* samples = (Sint32*)m_bufferPos;
		float sampleIndex = sampleIndexCarryOver;
		for(Uint32 i = bufferStartIndex; i < bufferEndIndex; i++)
		{
			Sint32 sample = samples[(Uint32)sampleIndex];

			Sint16 sample1 = (Sint16)(sample & 0xFFFF);
			Sint16 sample2 = (Sint16)((sample >> 16) & 0xFFFF);

			buffer[i * 2] += volume * (float)(sample1);
			buffer[i * 2 + 1] += volume * (float)(sample2);
			sampleIndex += pitchAdjust;
		}
		m_bufferPos = (Uint8*)(samples + (Uint32)sampleIndex);
		neededLen -= len;
		bufferStartIndex = bufferEndIndex;
		sampleIndexCarryOver = sampleIndex - (int)sampleIndex;

		if(neededLen == 0)
		{
			return (int)GetCurrentAudioPos();
		}

		if(!FillBuffer())
		{
			return -1;
		}
	} while(true);
}

static Uint32 ReadLE32(SDL_RWops* src)
{
	Uint32 result = 0;

	SDL_RWread(src, &result, sizeof(result), 1);
	return SDL_SwapLE32(result);
}

static void ReadWavChunkHeader(SDL_RWops* src, Uint32* header, Uint32* length)
{
	*header = ReadLE32(src);
	*length = ReadLE32(src);
}

static int ReadWavChunkData(SDL_RWops* src, Uint8* data, Uint32 length)
{
	if(SDL_RWread(src, data, length, 1) != 1)
	{
		// ERROR: Read failed!
		return -1;
	}

	return (int)length;
}

static void SkipWavChunkData(SDL_RWops* src, Uint32 length)
{
	if(length == 0)
	{
		return;
	}
	SDL_RWseek(src, length, RW_SEEK_CUR);
}

static SDL_AudioSpec* OpenWavFile(SDL_RWops* src,
                                  SDL_AudioSpec* spec,
                                  Uint32* length)
{
	// Constants for various headers and other WAV file data.
	static const Uint32 RIFF_HEX = 0x46464952; /* "RIFF" */
	static const Uint32 WAVE_HEX = 0x45564157; /* "WAVE" */
	static const Uint32 FACT_HEX = 0x74636166; /* "fact" */
	static const Uint32 LIST_HEX = 0x5453494c; /* "LIST" */
	static const Uint32 FMT_HEX  = 0x20746D66; /* "fmt " */
	static const Uint32 DATA_HEX = 0x61746164; /* "data" */

	static const Uint32 PCM_CODE = 0x0001;
//	static const Uint32 MS_ADPCM = 0x0002;
//	static const Uint32 IEEE_FLOAT_CODE = 0x0003;
//	static const Uint32 IMA_ADPCM_CODE = 0x0011;
//	static const Uint32 MP3_CODE = 0x0055;
//	
//	static const Uint32 WAVE_MONO = 1;
//	static const Uint32 WAVE_STEREO = 2;

	Uint32 RIFFchunk = 0;
	Uint32 wavelen = 0;
	Uint32 WAVEchunk = 0;

	Uint32 encoding = 0;
	int samplesize = 0;

	WavFormat* format = NULL;

	WavChunk chunk;
	SDL_zero(chunk);

	bool error = false;
	if(src == NULL)
	{
		error = true;
		goto done;
	}

	// Load the file header
	RIFFchunk = ReadLE32(src);
	wavelen = ReadLE32(src);

	// Some WAV files are missing the "RIFF" at the start, but are otherwise
	// valid. If that's the case, the entire file will be offset by 4 bytes.
	//
	// As a result, RIFFchunk will end up storing what should be in wavelen,
	// wavelen will end up storing what should be in WAVEchunk, and so on
	// unless it is corrected.
	//
	// This code will detect that, and swap around the existing variables so they
	// hold what they should. Additionally, it'll skip a 4 byte load so the rest 
	// of the file is loaded properly.
	if(wavelen == WAVE_HEX)
	{
		WAVEchunk = wavelen;
		wavelen = RIFFchunk;
		RIFFchunk = RIFF_HEX;
	}
	else
	{
		WAVEchunk = ReadLE32(src);
	}

	// Make sure file is actually a valid WAV file
	if((RIFFchunk != RIFF_HEX) || (WAVEchunk != WAVE_HEX))
	{
		// ERROR: Not a valid WAV file!
		error = true;
		goto done;
	}


	
	// Find the format chunk	
	chunk.data = NULL;
	chunk.length = 0;
	
	do
	{
		SkipWavChunkData(src, chunk.length);
		ReadWavChunkHeader(src, &chunk.header, &chunk.length);
	} while((chunk.header == FACT_HEX) || (chunk.header == LIST_HEX));

	chunk.data = (Uint8*)malloc(chunk.length);
	if(chunk.data == NULL)
	{
		// ERROR: Out of Memory
		error = true;
		goto done;
	}
	
	if(ReadWavChunkData(src, chunk.data, chunk.length) == -1)
	{
		free(chunk.data);
		error = true;
		goto done;
	}	


	
	// Determine encoding
	format = (WavFormat*)chunk.data;
	if(chunk.header != FMT_HEX)
	{
		// ERROR: Complex WAV Files not supported!
		error = true;
		goto done;
	}

	encoding = SDL_SwapLE16(format->encoding);
	if(encoding != PCM_CODE)
	{
		// ERROR: Only PCM WAV encoding is supported at the moment.
		error = true;
		goto done;
	}



	// TODO: This should be used to adjust the WAV data as appropriate
	// Set up audio spec
	SDL_memset(spec, 0, (sizeof *spec));
    spec->freq = (int)SDL_SwapLE32(format->frequency);

	switch(SDL_SwapLE16(format->bitspersample))
	{
		case 8:
            spec->format = AUDIO_U8;
            break;
        case 16:
            spec->format = AUDIO_S16;
            break;
        case 32:
            spec->format = AUDIO_S32;
            break;
        default:
			// ERROR: Invalid number of bits per sample
            error = true;
            goto done;
	}
	spec->channels = (Uint8)SDL_SwapLE16(format->channels);
	spec->samples = 4096;

	

	chunk.length = 0;
	chunk.data = NULL;
	do
	{
		SkipWavChunkData(src, chunk.length);
		ReadWavChunkHeader(src, &chunk.header, &chunk.length);
	} while(chunk.header != DATA_HEX);

	*length = chunk.length;
	
	//Ensure final length is a multiple of sample size
	samplesize = ((SDL_AUDIO_BITSIZE(spec->format)) / 8) * spec->channels;
    *length &= ~(samplesize - 1);

done:
	free(format);

	if(error)
	{
		spec = NULL;
	}
	
	return spec;
}

