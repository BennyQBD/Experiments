#include "sdlinput.h"

#include <cstring>

SDLInput::SDLInput(SDL_Window* window)
{
	memset(m_inputs, 0, NUM_KEYS * sizeof(bool));
	memset(m_downKeys, 0, NUM_KEYS * sizeof(bool));
	memset(m_upKeys, 0, NUM_KEYS * sizeof(bool));
	
	memset(m_mouseInput, 0, NUM_MOUSEBUTTONS * sizeof(bool));
	memset(m_downMouse, 0, NUM_MOUSEBUTTONS * sizeof(bool));
	memset(m_upMouse, 0, NUM_MOUSEBUTTONS * sizeof(bool));
	m_window = window;
}

void SDLInput::Update()
{
	for(int i = 0; i < NUM_MOUSEBUTTONS; i++)
	{
		m_downMouse[i] = false;
		m_upMouse[i] = false;
	}

	for(int i = 0; i < NUM_KEYS; i++)
	{
		m_downKeys[i] = false;
		m_upKeys[i] = false;
	}
}

void SDLInput::HandleEvent(const SDL_Event& e)
{
	if(e.type == SDL_MOUSEMOTION)
	{
		m_mouseX = e.motion.x;
		m_mouseY = e.motion.y;
	}

	if(e.type == SDL_KEYDOWN)
	{
		int value = e.key.keysym.scancode;

		if(!m_inputs[value])
		{
			m_inputs[value] = true;
			m_downKeys[value] = true;
		}
	}
	if(e.type == SDL_KEYUP)
	{
		int value = e.key.keysym.scancode;

		if(m_inputs[value])
		{
			m_inputs[value] = false;
			m_upKeys[value] = true;
		}
	}
	if(e.type == SDL_MOUSEBUTTONDOWN)
	{
		int value = e.button.button;

		if(!m_mouseInput[value])
		{
			m_mouseInput[value] = true;
			m_downMouse[value] = true;
		}
	}
	if(e.type == SDL_MOUSEBUTTONUP)
	{
		int value = e.button.button;

		if(m_mouseInput[value])
		{
			m_mouseInput[value] = false;
			m_upMouse[value] = true;
		}
	}
}

bool SDLInput::GetKey(int keyCode)
{
	return m_inputs[keyCode];
}

bool SDLInput::GetKeyDown(int keyCode)
{
	return m_downKeys[keyCode];
}

bool SDLInput::GetKeyUp(int keyCode)
{
	return m_upKeys[keyCode];
}

bool SDLInput::GetMouse(int keyCode)
{
	return m_mouseInput[keyCode];
}

bool SDLInput::GetMouseDown(int keyCode)
{
	return m_downMouse[keyCode];
}

bool SDLInput::GetMouseUp(int keyCode)
{
	return m_upMouse[keyCode];
}

int SDLInput::GetMouseX()
{
	return m_mouseX;
}

int SDLInput::GetMouseY()
{
	return m_mouseY;
}

void SDLInput::SetCursorVisibile(bool value)
{
	if(value)
	{
		SDL_ShowCursor(1);
	}
	else
	{
		SDL_ShowCursor(0);
	}
}

void SDLInput::SetMousePosition(int x, int y)
{
	SDL_WarpMouseInWindow(m_window, x, y);
}

