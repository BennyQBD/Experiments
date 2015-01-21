#include <stdio.h>
#include "luavm.h"
#include "sdldisplay.h"

static const char* display_name = "Lua.Display";

/* Registering display */
static struct sdl_display* check_display2(lua_State* L, int index)
{
	void* userdata = 0;
	//userdata = luaL_checkudata(L, index, display_name);
	userdata = lua_touserdata(L, index);
	//luaL_argcheck(L, ud != 0, 0, "'lua.native.display' expected");  
	
	return *((struct sdl_display**)userdata);
}

static int lua_sdl_display_update2(lua_State* L)
{
	struct sdl_display* self;

	int expected_args = 0;
	int n = lua_gettop(L);
	if(n != expected_args) {
		return luaL_error(L, "Got %d arguments, expected %d", n, expected_args);
	}

	self = check_display2(L, lua_upvalueindex(1));
	sdl_display_update(self);
	return 0;
}

static int lua_sdl_display_is_closed2(lua_State* L)
{
	struct sdl_display* self;

	int expected_args = 0;
	int n = lua_gettop(L);
	if(n != expected_args) {
		return luaL_error(L, "Got %d arguments, expected %d", n, expected_args);
	}

	self = check_display2(L, lua_upvalueindex(1));
	lua_pushboolean(L, sdl_display_is_closed(self));
	return 1;
}

static int lua_sdl_display_clear2(lua_State* L)
{
	struct sdl_display* self;

	int expected_args = 0;
	int n = lua_gettop(L);
	if(n != expected_args) {
		return luaL_error(L, "Got %d arguments, expected %d", n, expected_args);
	}

	self = check_display2(L, lua_upvalueindex(1));
	sdl_display_clear(self);
	return 0;
}

static int lua_sdl_display_draw_pixel2(lua_State* L)
{
	struct sdl_display* self;
	unsigned int x;
	unsigned int y;
	int hex_color;

	int expected_args = 3;
	int n = lua_gettop(L);
	if(n != expected_args) {
		return luaL_error(L, "Got %d arguments, expected %d", n, expected_args);
	}

	self = check_display2(L, lua_upvalueindex(1));
	x = (unsigned int)luaL_checknumber(L, 1);
	y = (unsigned int)luaL_checknumber(L, 2);
	hex_color = (int)luaL_checknumber(L, 3);
	sdl_display_draw_pixel(self, x, y, hex_color);
	return 0;
}

static int lua_sdl_display_destroy2(lua_State* L)
{
	struct sdl_display* self;
	unsigned int x;
	unsigned int y;
	int hex_color;

	int expected_args = 1;
	int n = lua_gettop(L);
	if(n != expected_args) {
		return luaL_error(L, "Got %d arguments, expected %d", n, expected_args);
	}

	self = check_display2(L, lua_upvalueindex(1));
	free(self);
	return 0;
}


static const luaL_Reg display_funcs2[] = {
	{ "update", lua_sdl_display_update2 },
	{ "is_closed", lua_sdl_display_is_closed2 },
	{ "clear", lua_sdl_display_clear2 },
	{ "draw_pixel", lua_sdl_display_draw_pixel2 },
	{ "__gc", lua_sdl_display_destroy2 },
	{ NULL, NULL }
};


static int lua_sdl_display_create2(lua_State* L)
{
	struct sdl_display** self;
	unsigned int width;
	unsigned int height;
	const char* title;

	int expected_args = 3;
	int n = lua_gettop(L);
	if(n != expected_args) {
		return luaL_error(L, "Got %d arguments, expected %d", n, expected_args);
	}

	lua_newtable(L);

	self = (struct sdl_display**)
		lua_newuserdata(L, sizeof(struct sdl_display*));

	width = (unsigned int)luaL_checknumber(L, 1);
	height = (unsigned int)luaL_checknumber(L, 2);
	title = luaL_checkstring(L, 3);

	*self = (struct sdl_display*)malloc(sizeof(struct sdl_display));
	sdl_display_create(*self, width, height, title);

	luaL_setfuncs(L, display_funcs2, 1);

	lua_pushvalue(L, -1);
	lua_setmetatable(L, -2);

	return 1;
}


/* Main function */

static int handle_error(const struct lua_vm* vm)
{
	printf("%s", vm->error_message);
	return 1;
}

int main(int argc, char** argv)
{
	struct lua_vm vm;
	
	(void)argc;
	(void)argv;

	SDL_Init(SDL_INIT_EVERYTHING);

	lua_vm_create(&vm);
	lua_vm_register_function(&vm, "Display", lua_sdl_display_create2);

	if(lua_vm_load_file(&vm, "./res/scripts/main.lua")) {
		return handle_error(&vm);
	}

	if(lua_vm_call(&vm, "main", ">")) {
		return handle_error(&vm);
	}

	lua_vm_release(&vm);
	SDL_Quit();

	return 0;
}
