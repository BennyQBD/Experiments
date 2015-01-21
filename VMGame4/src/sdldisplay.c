#include "sdldisplay.h"

void sdl_display_create(struct sdl_display* self, 
		unsigned int width, unsigned int height, const char* title)
{
	self->window = SDL_CreateWindow(title, SDL_WINDOWPOS_CENTERED, 
			SDL_WINDOWPOS_CENTERED, (int)(width), (int)(height), 0);
	self->renderer = SDL_CreateRenderer(self->window, -1,
			SDL_RENDERER_SOFTWARE);
	self->image = SDL_CreateTexture(self->renderer, 
			SDL_PIXELFORMAT_ARGB8888, SDL_TEXTUREACCESS_STREAMING,
			(int)width, (int)height);
	self->pixels = (int*)malloc(width * height * sizeof(int));
	
	self->width = width;
	self->height = height;
	self->is_closed = 0;
}

void sdl_display_release(struct sdl_display* self)
{
	free(self->pixels);	
	self->pixels = NULL;

	SDL_DestroyTexture(self->image);
	self->image = NULL;

	SDL_DestroyRenderer(self->renderer);
	self->renderer = NULL;

	SDL_DestroyWindow(self->window);
	self->window = NULL;
}

void sdl_display_update(struct sdl_display* self)
{
	SDL_Event e;
	
	while(SDL_PollEvent(&e)){
		if(e.type == SDL_QUIT) {
			self->is_closed = 1;
		}
	}
	
	SDL_UpdateTexture(self->image, NULL, self->pixels, 
			(int)(self->width * sizeof(int)));
	SDL_RenderCopy(self->renderer, self->image, NULL, NULL);
	SDL_RenderPresent(self->renderer);
}

char sdl_display_is_closed(struct sdl_display* self)
{
	return self->is_closed;
}

void sdl_display_clear(struct sdl_display* self)
{
	memset(self->pixels, 0, self->width * self->height * sizeof(int));
}

void sdl_display_draw_pixel(struct sdl_display* self,
		unsigned int x, unsigned int y, int hex_color)
{
	self->pixels[x + y * self->width] = hex_color;
}

