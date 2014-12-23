#ifndef AUDIO_DATA_INCLUDED_H
#define AUDIO_DATA_INCLUDED_H

#include "../resourceManagement/resource.h"
#include "iaudiodata.h"

class AudioData : public Resource
{
public:
	inline IAudioData* GetAudioData() { return (IAudioData*)GetData(); }
};

#endif



