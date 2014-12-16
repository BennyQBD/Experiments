#ifndef SDL_INPUT_INCLUDED_H
#define SDL_INPUT_INCLUDED_H

#include "../../core/iinput.h"
#include <SDL2/SDL.h>

class SDLInput : public IInput
{
public:
	SDLInput(SDL_Window* window);

	void Update();
	void HandleEvent(const SDL_Event& e);

	virtual bool GetKey(int keyCode);
	virtual bool GetKeyDown(int keyCode);
	virtual bool GetKeyUp(int keyCode);
	
	virtual bool GetMouse(int keyCode);
	virtual bool GetMouseDown(int keyCode);
	virtual bool GetMouseUp(int keyCode);

	virtual int GetMouseX();
	virtual int GetMouseY();

	virtual void SetCursorVisibile(bool value);
	virtual void SetMousePosition(int x, int y);
private:
	bool m_inputs[NUM_KEYS];
	bool m_downKeys[NUM_KEYS];
	bool m_upKeys[NUM_KEYS];
	bool m_mouseInput[NUM_MOUSEBUTTONS];
	bool m_downMouse[NUM_MOUSEBUTTONS];
	bool m_upMouse[NUM_MOUSEBUTTONS];
	int  m_mouseX;
	int  m_mouseY;
	SDL_Window* m_window;
};

#endif
