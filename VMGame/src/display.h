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

#ifndef DISPLAY_INCLUDED_H
#define DISPLAY_INCLUDED_H

#include <SDL2/SDL.h>
#include "renderContext.h"

#define NUM_KEYS 512

/**
 * The Display struct is used to represent a renderable area in a display 
 * output device (such as a window on a screen) and all it's associated data.
 *
 * Should be initialized with Display_Init before usage, and deinitalized
 * with Display_DeInit after usage.
 */
typedef struct
{
	/** Holds the display. */
	SDL_Window*   m_window;
	/** For rendering in the display with OpenGL. */
	SDL_GLContext m_glContext; 
	/** For rendering offscreen by other code. */
	RenderContext m_context;

	/** How wide the display is, in pixels */
	unsigned int  m_width;
	/** How tall the display is, in pixels */
	unsigned int  m_height;
	/** Whether or not the display has been closed by the operating system. */
	int           m_isClosed;
	
	char          m_inputs[NUM_KEYS];

	/** 
	  * 4 bytes of extra space so that Display is aligned to an 8-byte 
	  * boundary.
	  */
	char unusedSpace[4];
} Display;

/**
 * Initialize to a usable state. Should be called as soon as the struct is 
 * created, and before any other operations using the struct.
 *
 * @param self   What's being initialized.
 * @param width  How wide, in pixels, the display area should be.
 * @param height How tall, in pixels, the display area should be.
 * @param title  The string of text that should appear on any title bar.
 */
void Display_Init(Display* self, unsigned int width, unsigned int height, 
                  const char* title);

/**
 * Properly frees/deinitializes any resources used. Should be called as soon as
 * the struct is no longer needed.
 *
 * @param self What's being deinitialized.
 */
void Display_DeInit(Display* self);

/**
 * Handles any operating system events or other factors that might affect the
 * Display's state.
 *
 * @param self The display being used.
 */
void Display_Update(Display* self);

/**
 * Copies the offscreen image into the visible display.
 *
 * @param self The display being used.
 */
void Display_SwapBuffers(Display* self);

/**
 * Whether or not the operating system closed the display
 *
 * @param self The display being used.
 * @return     0 if the display is open, 1 if the display is closed.
 */
int Display_IsClosed(Display* self);

/**
 * Returns how wide the display area is. 
 *
 * @param self The display being used.
 * @return     The width of the display
 */
unsigned int Display_GetWidth(Display* self);

/**
 * Returns how tall the display area is.
 *
 * @param self The display being used.
 * @return     The height of the display
 */
unsigned int Display_GetHeight(Display* self);

/**
 * Returns the context which is used to render images into the offscreen
 * display area.
 *
 * @param self The display being used.
 * @return     A RenderContext that can render into the offscreen display area.
 */
RenderContext* Display_GetContext(Display* self);

int Display_GetKey(Display* self, int keyCode);

#endif
