/**
@file
@author Benny Bobaganoosh <thebennybox@gmail.com>
@section LICENSE

Copyright (c) 2014, Benny Bobaganoosh
All rights reserved.

Redistribution and use in source and binary forms, with or without
modification, are permitted provided that the following conditions are met:

1. Redistributions of source code must retain the above copyright notice, this
   list of conditions and the following disclaimer. 
2. Redistributions in binary form must reproduce the above copyright notice,
  this list of conditions and the following disclaimer in the documentation
   and/or other materials provided with the distribution.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
(INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
(INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
*/

#include "timing.h"
#include <time.h>

//-----------------------------------------------------------------------------
// Forward declarations/Variable Initializations
//-----------------------------------------------------------------------------
#if defined(WIN32) || defined(_WIN32) || defined(__WIN32__) || defined(_WIN64) || defined(WIN64)
	#define OS_WINDOWS
#elif defined(__linux__)
	#define OS_LINUX
#elif __cplusplus >= 201103L
	#define OS_OTHER_CPP11
#else
	#define OS_OTHER
#endif

#ifdef OS_WINDOWS
	#include <Windows.h>
	#include <iostream>
	static double g_freq;
	static bool g_timerInitialized = false;
#endif

#ifdef OS_LINUX
	#include <sys/time.h>
	static const long NANOSECONDS_PER_SECOND = 1000000000L;
#endif

#ifdef OS_OTHER
	#include <SDL2/SDL.h>
#endif

#ifdef OS_OTHER_CPP11
	#include <chrono>
	static std::chrono::system_clock::time_point m_epoch = std::chrono::high_resolution_clock::now();
#endif

//-----------------------------------------------------------------------------
// Constructors/Destructors/Initialization/Deinitialization
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// Function Implementations
//-----------------------------------------------------------------------------
double Timing_GetCurrentTime(void)
{
	#ifdef OS_WINDOWS
		if(!g_timerInitialized)
		{
			LARGE_INTEGER li;
			if(!QueryPerformanceFrequency(&li))
				std::cerr << "QueryPerformanceFrequency failed in timer initialization"  << std::endl;
			
			g_freq = double(li.QuadPart);
			g_timerInitialized = true;
		}
	
		LARGE_INTEGER li;
		if(!QueryPerformanceCounter(&li))
			std::cerr << "QueryPerformanceCounter failed in get time!" << std::endl;
		
		return double(li.QuadPart)/g_freq;
	#endif

	#ifdef OS_LINUX
		struct timespec ts;
		clock_gettime(CLOCK_REALTIME, &ts);
		return (double)(((long) ts.tv_sec * NANOSECONDS_PER_SECOND) + ts.tv_nsec)/((double)(NANOSECONDS_PER_SECOND));
	#endif

	#ifdef OS_OTHER_CPP11
		return std::chrono::duration_cast<std::chrono::nanoseconds>(std::chrono::high_resolution_clock::now() - m_epoch).count() / 1000000000.0;
	#endif

	#ifdef OS_OTHER
		return (double)SDL_GetTicks()/1000.0;
	#endif
}

//-----------------------------------------------------------------------------
// Static Function Implementations
//-----------------------------------------------------------------------------
