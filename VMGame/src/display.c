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

#include "display.h"

#include <GL/glew.h>
#include <stdlib.h>

//-----------------------------------------------------------------------------
// Forward declarations/Variable Initializations
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// Constructors/Destructors/Initialization/Deinitialization
//-----------------------------------------------------------------------------
void Display_Init(Display* self, unsigned int width, unsigned int height, 
                  const char* title)
{
	SDL_Init(SDL_INIT_EVERYTHING);

	SDL_GL_SetAttribute(SDL_GL_RED_SIZE,    8);
	SDL_GL_SetAttribute(SDL_GL_GREEN_SIZE,  8);
	SDL_GL_SetAttribute(SDL_GL_BLUE_SIZE,   8);
	SDL_GL_SetAttribute(SDL_GL_ALPHA_SIZE,  8);
	SDL_GL_SetAttribute(SDL_GL_BUFFER_SIZE, 32);
	SDL_GL_SetAttribute(SDL_GL_DEPTH_SIZE,  16);
	SDL_GL_SetAttribute(SDL_GL_DOUBLEBUFFER,1);

	self->m_window = SDL_CreateWindow(title, SDL_WINDOWPOS_CENTERED, 
	                 SDL_WINDOWPOS_CENTERED, (int)(width), (int)(height), 
	                 SDL_WINDOW_OPENGL);
	self->m_glContext = SDL_GL_CreateContext(self->m_window);

	SDL_GL_SetSwapInterval(1);

//	SDL_GL_SetAttribute( SDL_GL_CONTEXT_PROFILE_MASK, 
//	                     SDL_GL_CONTEXT_PROFILE_CORE );
//	SDL_GL_SetAttribute( SDL_GL_CONTEXT_MAJOR_VERSION, 3 );
//	SDL_GL_SetAttribute( SDL_GL_CONTEXT_MINOR_VERSION, 2 );

	//Apparently this is necessary to build with Xcode
	glewExperimental = GL_TRUE;
	
	GLenum res = glewInit();
	if(res != GLEW_OK)
	{
		fprintf(stderr, "Error: '%s'\n", glewGetErrorString(res));
	}

	self->m_isClosed = 0;
	self->m_width = width;
	self->m_height = height;

	memset(self->m_inputs, 0, sizeof(self->m_inputs));

	RenderContext_Init(&self->m_context, width, height);
}

void Display_DeInit(Display* self)
{
	RenderContext_DeInit(&self->m_context);
	SDL_GL_DeleteContext(self->m_glContext);
	SDL_DestroyWindow(self->m_window);
	SDL_Quit();
}

//-----------------------------------------------------------------------------
// Function Implementations
//-----------------------------------------------------------------------------
void Display_Update(Display* self)
{
	SDL_Event e;
	while(SDL_PollEvent(&e))
	{
		if(e.type == SDL_QUIT)
		{
			self->m_isClosed = 1;
		}

		if(e.type == SDL_KEYDOWN)
		{
			int value = e.key.keysym.scancode;

			self->m_inputs[value] = 1;
		}
		if(e.type == SDL_KEYUP)
		{
			int value = e.key.keysym.scancode;

			self->m_inputs[value] = 0;
		}
	}
}

void Display_SwapBuffers(Display* self)
{
	SDL_GL_SwapWindow(self->m_window);
}

int Display_IsClosed(Display* self)
{
	return self->m_isClosed;
}

unsigned int Display_GetWidth(Display* self)
{
	return self->m_width;
}

unsigned int Display_GetHeight(Display* self)
{
	return self->m_height;
}

RenderContext* Display_GetContext(Display* self)
{
	return &(self->m_context);
}

int Display_GetKey(Display* self, int keyCode)
{
	return (int)self->m_inputs[keyCode];
}

//-----------------------------------------------------------------------------
// Static Function Implementations
//-----------------------------------------------------------------------------
