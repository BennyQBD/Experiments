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
#include <stdlib.h>
#include <string.h>

//-----------------------------------------------------------------------------
// Forward declarations/Variable Initializations
//-----------------------------------------------------------------------------
static void Error(lua_State* L, const char* fmt, ...);
static double GetFieldDouble(lua_State* L, const char* fieldName);

//-----------------------------------------------------------------------------
// Constructors/Destructors/Initialization/Deinitialization
//-----------------------------------------------------------------------------
void VirtualMachine_Init(VirtualMachine* vm)
{
	vm->m_state = luaL_newstate();
	luaL_openlibs(vm->m_state);
}

void VirtualMachine_DeInit(VirtualMachine* vm)
{
	lua_close(vm->m_state);
}

//-----------------------------------------------------------------------------
// Function Implementations
//-----------------------------------------------------------------------------
void VirtualMachine_LoadFile(VirtualMachine* vm, const char* fileName)
{
	if(luaL_loadfile(vm->m_state, fileName) || lua_pcall(vm->m_state, 0, 0, 0))
	{
		Error(vm->m_state, "Cannot run file: %s", lua_tostring(vm->m_state, -1));
	}
}

void VirtualMachine_Call(VirtualMachine* vm, const char *func, const char *sig, ...)
{
	lua_State* L = vm->m_state;
	va_list vl;
	int narg, nres;  //number of arguments and results 
    va_start(vl, sig);
    lua_getglobal(L, func);  // get function 
    // push arguments 
	narg = 0;
	while (*sig) 
	{
		switch (*sig++) 
		{
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
				Error(L, "invalid option (%c)", *(sig - 1));
		}
		narg++;
		luaL_checkstack(L, 1, "too many arguments");
	} endwhile:
	
	// do the call
	nres = (int)strlen(sig);  // number of expected results
	if (lua_pcall(L, narg, nres, 0) != 0)  // do the call
	{
		Error(L, "error running function `%s': %s",
		      func, lua_tostring(L, -1));
	}
	
	// retrieve results
	nres = -nres;  // stack index of first result
	while (*sig) 
	{  // get results
		switch (*sig++) 
		{
			case 'd':  // double result 
				if (!lua_isnumber(L, nres))
					Error(L, "wrong result type");
				*va_arg(vl, double *) = lua_tonumber(L, nres);
			break;
			
			case 'i':  // int result
				if (!lua_isnumber(L, nres))
					Error(L, "wrong result type");
				*va_arg(vl, int *) = (int)(lua_tonumber(L, nres));
			break;
			
			case 's':  // string result 
				if (!lua_isstring(L, nres))
					Error(L, "wrong result type");
				*va_arg(vl, const char **) = lua_tostring(L, nres);
			break;
			
			default:
				Error(L, "invalid option (%c)", *(sig - 1));
		}
		nres++;
	}
	va_end(vl);
	vm->m_state = L;
}

void VirtualMachine_RegisterFunction(VirtualMachine* vm, const char* functionName, lua_CFunction func)
{
	lua_pushcfunction(vm->m_state, func);
    lua_setglobal(vm->m_state, functionName);
}

double VirtualMachine_GetGlobalDouble(VirtualMachine* vm, const char* name)
{
	lua_getglobal(vm->m_state, name);
	if(!lua_isnumber(vm->m_state, -1))
	{
		Error(vm->m_state, "'%s' is not a double.\n", name);
	}

	return lua_tonumber(vm->m_state, -1);

}

double VirtualMachine_GetGlobalTableDouble(VirtualMachine* vm, const char* tableName, const char* fieldName)
{
	lua_getglobal(vm->m_state, tableName);
	if(!lua_istable(vm->m_state, -1))
	{
		Error(vm->m_state, "'%s' is not a valid table", tableName);
	}

	return GetFieldDouble(vm->m_state, fieldName);
}


//-----------------------------------------------------------------------------
// Static Function Implementations
//-----------------------------------------------------------------------------
static void Error(lua_State* L, const char* fmt, ...)
{
	va_list argp;
	va_start(argp, fmt);
	vfprintf(stderr, fmt, argp);
	va_end(argp);
	lua_close(L);
	
	//The following code should always
	//execute the exit statement.
	//
	//It is written like this to suppress 
	//'noreturn' warnings.
	int a = 0;
	int b = 0;
	if(!(a != b && b == 0 && a == 0))
	{
		exit(EXIT_FAILURE);	
	}
}

static double GetFieldDouble(lua_State* L, const char* fieldName)
{
	double result;
	lua_pushstring(L, fieldName);
	lua_gettable(L, -2);
	if(!lua_isnumber(L, -1))
	{
		Error(L, "'%s' is not a field.", fieldName);
	}
	result = (int)(lua_tonumber(L, -1) * 255);
	lua_pop(L, 1);
	return result;

}
