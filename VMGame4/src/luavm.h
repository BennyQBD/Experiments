#ifndef LUA_VIRTUAL_MACHINE_INCLUDED_H
#define LUA_VIRTUAL_MACHINE_INCLUDED_H

#include <lua5.2/lua.h>
#include <lua5.2/lauxlib.h>
#include <lua5.2/lualib.h>

enum lua_vm_error {
	LUA_VM_ERROR_NONE,
	LUA_VM_ERROR_FILE_CANNOT_BE_RUN,
	LUA_VM_ERROR_INVALID_FUNCTION_SIG,
	LUA_VM_ERROR_FUNCTION_CALL_FAILED,
	LUA_VM_ERROR_TYPE_MISMATCH
};

struct lua_vm {
	lua_State* state;
	enum lua_vm_error error;
	const char* error_message;
};


void lua_vm_create(struct lua_vm* self);
void lua_vm_release(struct lua_vm* self);

char lua_vm_load_file(struct lua_vm* self, const char* fileName);
char lua_vm_call(struct lua_vm* self, const char *func, 
		const char *sig, ...);

void lua_vm_register_function(struct lua_vm* self, 
		const char* name, lua_CFunction f);

#endif
