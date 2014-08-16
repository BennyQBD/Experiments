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

#ifndef RENDER_CONTEXT_INCLUDED_H
#define RENDER_CONTEXT_INCLUDED_H

/**
 * The RenderContext stores all the information necessary to render into
 * a defined area.
 *
 * Should be initialized with RenderContext_Init before usage, and 
 * deinitalized with RenderContext_DeInit after usage.
 */
typedef struct
{
	/** Width, in pixels, of the renderable area. */
	unsigned int m_width; 
	/** Height, in pixels, of the renderable area. */
	unsigned int m_height; 
} RenderContext;

/**
 * Initialize to a usable state. Should be called as soon as the struct is 
 * created, and before any other operations using the struct.
 *
 * @param self   What's being initialized.
 * @param width  How wide, in pixels, the renderable area should be.
 * @param height How tall, in pixels, the renderable area should be.
 */
void RenderContext_Init(RenderContext* self, unsigned int width, 
                        unsigned int height);

/**
 * Properly frees/deinitializes any resources used. Should be called as soon as
 * the struct is no longer needed.
 *
 * @param self What's being deinitialized.
 */
void RenderContext_DeInit(RenderContext* self);

/**
 * Sets every pixel in the rendering area to a specific RGBA color.
 *
 * @param self The RenderContext being used.
 * @param r    Amount of red in the desired color
 * @param g    Amount of green in the desired color
 * @param b    Amount of blue in the desired color
 * @param a    Alpha value of the desired color
 */
void RenderContext_Clear(RenderContext* self, float r, float g, float b, 
                         float a);

//TODO: Remove this function, and replace it with a more generally
//useful rendering function.
void RenderContext_DrawSquare(RenderContext* self, float x, float y, 
                         float width, float height,
						 float r, float g, float b);

/**
 * Gets the width of the rendering area.
 *
 * @param self The RenderContext being used.
 * @return     The width of the rendering area.
 */
unsigned int RenderContext_GetWidth(RenderContext* self);

/**
 * Gets the height of the rendering area.
 *
 * @param self The RenderContext being used.
 * @return     The height of the rendering area.
 */
unsigned int RenderContext_GetHeight(RenderContext* self);

#endif

