#ifndef SDL_DISPLAY_INCLUDED_H
#define SDL_DISPLAY_INCLUDED_H

#include <SDL2/SDL.h>

struct sdl_display {
	SDL_Window* window;
	SDL_Texture* image;
	SDL_Renderer* renderer;
	int* pixels;

	unsigned int width;
	unsigned int height;
	char is_closed;
};

void sdl_display_create(struct sdl_display* self, 
		unsigned int width, unsigned int height, const char* title);
void sdl_display_release(struct sdl_display* self);

void sdl_display_update(struct sdl_display* self);
char sdl_display_is_closed(struct sdl_display* self);

void sdl_display_clear(struct sdl_display* self);
void sdl_display_draw_pixel(struct sdl_display* self,
		unsigned int x, unsigned int y, int hex_color);

#endif
