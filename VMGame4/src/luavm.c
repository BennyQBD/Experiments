#include "luavm.h"

#include <string.h>
#include <stdlib.h>

void lua_vm_create(struct lua_vm* self)
{
	self->state = luaL_newstate();
	luaL_openlibs(self->state);
	self->error = LUA_VM_ERROR_NONE;
	self->error_message = NULL;
}

void lua_vm_release(struct lua_vm* self)
{
	lua_close(self->state);
	self->state = NULL;
}

char lua_vm_load_file(struct lua_vm* self, const char* fileName)
{
	if(luaL_loadfile(self->state, fileName) || 
			lua_pcall(self->state, 0, 0, 0)) {
		self->error_message = lua_tostring(self->state, -1);
		self->error = LUA_VM_ERROR_FILE_CANNOT_BE_RUN;
		return 1;
	}

	return 0;
}

char lua_vm_call(struct lua_vm* self, const char *func, 
		const char *sig, ...)
{
	lua_State* L = self->state;
	va_list vl;
	int narg, nres;  //number of arguments and results 
    va_start(vl, sig);
    lua_getglobal(L, func);  // get function 
    // push arguments 
	narg = 0;
	while (*sig) {
		switch (*sig++) {
			case 'd':  // double argument
				lua_pushnumber(L, va_arg(vl, double));
			break;
			
			case 'i':  // int argument
				lua_pushnumber(L, va_arg(vl, int));
			break;
			
			case 's':  // string argument
				lua_pushstring(L, va_arg(vl, char *));
			break;
			
			case '>':
				goto endwhile;
			
			default:
				self->error_message = (sig - 1);
				self->error = LUA_VM_ERROR_INVALID_FUNCTION_SIG;
				return 1;
		}
		narg++;
		luaL_checkstack(L, 1, "too many arguments");
	} endwhile:
	
	// do the call
	nres = (int)strlen(sig);  // number of expected results
	if (lua_pcall(L, narg, nres, 0) != 0)  {
		self->error_message = lua_tostring(L, -1);
		self->error = LUA_VM_ERROR_FUNCTION_CALL_FAILED;
		return 1;
	}
	
	// retrieve results
	nres = -nres;  // stack index of first result
	while (*sig) {  // get results
		switch (*sig++) {
			case 'd':  // double result 
				if (!lua_isnumber(L, nres)) {
					self->error_message = "wrong result type";
					self->error = LUA_VM_ERROR_TYPE_MISMATCH;
					return 1;
				}
				*va_arg(vl, double *) = lua_tonumber(L, nres);
			break;
			
			case 'i':  // int result
				if (!lua_isnumber(L, nres)) {
					self->error_message = "wrong result type";
					self->error = LUA_VM_ERROR_TYPE_MISMATCH;
					return 1;
				}
				*va_arg(vl, int *) = (int)(lua_tonumber(L, nres));
			break;
			
			case 's':  // string result 
				if (!lua_isstring(L, nres)) {
					self->error_message = "wrong result type";
					self->error = LUA_VM_ERROR_TYPE_MISMATCH;
					return 1;
				}
				*va_arg(vl, const char **) = lua_tostring(L, nres);
			break;
			
			default:
				self->error_message = (sig - 1);
				self->error = LUA_VM_ERROR_INVALID_FUNCTION_SIG;
				return 1;
		}
		nres++;
	}
	va_end(vl);
	self->state = L;
	return 0;
}

void lua_vm_register_function(struct lua_vm* self, 
		const char* name, lua_CFunction f)
{
	lua_pushcfunction(self->state, f);
	lua_setglobal(self->state, name);
}

