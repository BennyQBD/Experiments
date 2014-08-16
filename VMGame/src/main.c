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

#include "virtualMachine.h"
#include "display.h"
#include "timing.h"

#include <stdio.h>
#include <math.h>

static double PointerToDouble(void* pointer)
{
	long pointerL = (long)pointer;
	double pointerD = *(double*)(&pointerL);
	return pointerD;
}

static void* DoubleToPointer(double pointerD)
{
	long pointerL = *(long*)(&pointerD);
	return (void*)(pointerL);
}

static int L_RenderContext_Clear(lua_State* L)
{
	RenderContext* context = (RenderContext*)DoubleToPointer(lua_tonumber(L, 1));
	float r                = (float)(lua_tonumber(L, 2));
	float g                = (float)(lua_tonumber(L, 3));
	float b                = (float)(lua_tonumber(L, 4));
	float a                = (float)(lua_tonumber(L, 5));
	RenderContext_Clear(context, r, g, b, a);
	return 0;
}
//
//static int L_RenderContext_DrawPixel(lua_State* L)
//{
//	RenderContext* context = (RenderContext*)DoubleToPointer(lua_tonumber(L, 1));
//	unsigned int x         = (unsigned int)(lua_tonumber(L, 2)) - 1;
//	unsigned int y         = (unsigned int)(lua_tonumber(L, 3)) - 1;
//	int color              = (int)(lua_tonumber(L, 4));
//	RenderContext_DrawPixel(context, x, y, color);
//	return 0;
//}

static int L_RenderContext_DrawSquare(lua_State* L)
{
	RenderContext* context = (RenderContext*)DoubleToPointer(lua_tonumber(L, 1));
	float x                = (float)(lua_tonumber(L, 2)) - 1;
	float y                = (float)(lua_tonumber(L, 3)) - 1;
	float width            = (float)(lua_tonumber(L, 4));
	float height           = (float)(lua_tonumber(L, 5));
	RenderContext_DrawSquare(context, x, y, width, height);
	return 0;
}

static int L_RenderContext_GetWidth(lua_State* L)
{
	RenderContext* context = (RenderContext*)DoubleToPointer(lua_tonumber(L, 1));
	lua_pushnumber(L, RenderContext_GetWidth(context));
	return 1;
}

static int L_RenderContext_GetHeight(lua_State* L)
{
	RenderContext* context = (RenderContext*)DoubleToPointer(lua_tonumber(L, 1));
	lua_pushnumber(L, RenderContext_GetHeight(context));
	return 1;
}

static int L_Display_GetKey(lua_State* L)
{
	Display* display = (Display*)DoubleToPointer(lua_tonumber(L, 1));
	int keyCode = (int)(lua_tonumber(L, 2));
	lua_pushnumber(L, Display_GetKey(display, keyCode));
	return 1;
}

int main(int argc, char** argv)
{
	(void)argc;
	(void)argv;

	Display display;
	VirtualMachine vm;
	
	double displayWidth;
	double displayHeight;
	const char* displayTitle;

	double unprocessedTime = 0.0;
	double previousTime    = 0.0;
	double secondsPerFrame = 1.0/60.0;

	VirtualMachine_Init(&vm);
	//VirtualMachine_RegisterFunction(&vm, "RenderContext_DrawPixel", L_RenderContext_DrawPixel);
	VirtualMachine_RegisterFunction(&vm, "RenderContext_GetWidth", L_RenderContext_GetWidth);
	VirtualMachine_RegisterFunction(&vm, "RenderContext_GetHeight", L_RenderContext_GetHeight);
	VirtualMachine_RegisterFunction(&vm, "RenderContext_Clear", L_RenderContext_Clear);
	VirtualMachine_RegisterFunction(&vm, "RenderContext_DrawSquare", L_RenderContext_DrawSquare);
	VirtualMachine_RegisterFunction(&vm, "Input_GetKey", L_Display_GetKey);

	
	VirtualMachine_LoadFile(&vm, "./res/scripts/main.lua");
	VirtualMachine_Call(&vm, "GameInit", ">dds", &displayWidth, &displayHeight, &displayTitle);

	Display_Init(&display, (unsigned int)displayWidth, (unsigned int)displayHeight, displayTitle);
	previousTime = Timing_GetCurrentTime();
	while(!Display_IsClosed(&display))
	{
		int shouldRender = 0;
		double currentTime = Timing_GetCurrentTime();
		double passedTime = currentTime - previousTime;

		previousTime = currentTime;
		unprocessedTime += passedTime;

		while(unprocessedTime > secondsPerFrame)
		{
			double displayD = PointerToDouble(&display);
			shouldRender = 1;

			Display_Update(&display);
			VirtualMachine_Call(&vm, "GameUpdate", "dd>", displayD, secondsPerFrame);

			unprocessedTime -= secondsPerFrame;
		}
		
		if(shouldRender)
		{
			double contextD = PointerToDouble(Display_GetContext(&display));
			VirtualMachine_Call(&vm, "GameRender", "d>", contextD);
			Display_SwapBuffers(&display);
		}
		else
		{
			SDL_Delay(1);
		}
	}

	Display_DeInit(&display);
	VirtualMachine_DeInit(&vm);
	return 0;
}
