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

#include "renderContext.h"
#include <GL/glew.h>
#include <assert.h>

//-----------------------------------------------------------------------------
// Forward declarations/Variable Initializations
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
// Constructors/Destructors/Initialization/Deinitialization
//-----------------------------------------------------------------------------
void RenderContext_Init(RenderContext* self, unsigned int width,
                        unsigned int height)
{
	self->m_width = width;
	self->m_height = height;

	glMatrixMode(GL_PROJECTION);
	glLoadIdentity();
	glMatrixMode(GL_MODELVIEW);

	glDisable(GL_DEPTH_TEST);
}

void RenderContext_DeInit(RenderContext* self)
{
	(void)self;	
}

//-----------------------------------------------------------------------------
// Function Implementations
//-----------------------------------------------------------------------------
void RenderContext_Clear(RenderContext* self, float r, float g, float b, 
                         float a)
{
	(void)self;
	glClearColor(r, g, b, a);
	glClear(GL_COLOR_BUFFER_BIT);
}

void RenderContext_DrawSquare(RenderContext* self, float x, float y, 
                              float width, float height, 
							  float r, float g, float b)
{
	(void)self;
	float xStart = x;
	float yStart = y;
	float xEnd = x + width;
	float yEnd = y + height;

	glColor3f(r, g, b);
	//glColor3f(0.25f, 0.75f, 0.5f);
	glBegin(GL_QUADS);
	{
		glVertex2f(xStart, yStart);
		glVertex2f(xStart, yEnd);
		glVertex2f(xEnd,   yEnd);
		glVertex2f(xEnd,   yStart);
	}
	glEnd();
}

unsigned int RenderContext_GetWidth(RenderContext* self)
{
	return self->m_width;
}

unsigned int RenderContext_GetHeight(RenderContext* self)
{
	return self->m_height;
}

//-----------------------------------------------------------------------------
// Static Function Implementations
//-----------------------------------------------------------------------------
