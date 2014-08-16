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

#ifndef VIRTUAL_MACHINE_INCLUDED_H
#define VIRTUAL_MACHINE_INCLUDED_H

#include <lua5.2/lua.h>
#include <lua5.2/lauxlib.h>
#include <lua5.2/lualib.h>

/**
 * The VirtualMachine struct stores any data necessary to interpret and 
 * execute any scripts written by the user.
 *
 * Should be initialized with VirtualMachine_Init before usage, and 
 * deinitalized with VirtualMachine_DeInit after usage.
 */
typedef struct
{
	/** The current state of the Lua VM. */
	lua_State* m_state;
} VirtualMachine;

/**
 * Initialize to a usable state. Should be called as soon as the struct is 
 * created, and before any other operations using the struct.
 *
 * @param self What's being initialized.
 */
void VirtualMachine_Init(VirtualMachine* self);

/**
 * Properly frees/deinitializes any resources used. Should be called as soon as
 * the struct is no longer needed.
 *
 * @param self What's being deinitialized.
 */
void VirtualMachine_DeInit(VirtualMachine* self);

/**
 * Loads and processes a Lua script written by the user.
 *
 * @param self     The VirtualMachine being used.
 * @param fileName The file path to the script to load.
 */
void VirtualMachine_LoadFile(VirtualMachine* self, const char* fileName);

/**
 * Calls a function in the VirtualMachine.
 * 
 * Example usage:
 * VirtualMachine_Call(&vm, "myVMFunction", "ds>i", myDouble, myString,
 *                     &returnInt);
 *
 * @param self The VirtualMachine being used.
 * @param func The name of the functio being called.
 * @param sig  The parameter and return types of the functions. Formatted like
 *               "si>d" for a function that takes a string and integer typed
 *               parameters, in that order, and returns a double.
 * @param ...  The values of the function parameters, followed by pointers to
 *               variables in which the return values should be stored. The
 *               types and number of parameters should match that specified by
 *               the 'sig' parameter.
 */
void VirtualMachine_Call(VirtualMachine* self, const char* func, 
                         const char* sig, ...);

/**
 * Registers a C function for usage in the VirtualMachine.
 * 
 * @param self         The VirtualMachine being used.
 * @param functionName What the function will be called in the VirtualMachine.
 * @param func         The C function that will be registered to the VM. It 
 *                       must have the signature of:
 *
 *                       int CFunctionName(lua_State* L);
 *
 *                       and return the number of return values the function
 *                       has in the virtual machine.
 */
void VirtualMachine_RegisterFunction(VirtualMachine* self, 
                                     const char* functionName, 
                                     lua_CFunction func);

/**
 * Retrieves a global double-typed variable from the VirtualMachine.
 * 
 * @param self The VirtualMachine being used.
 * @param name The name of the global variable in the VirtualMachine.
 * @return     The value of the global variable as a double.
 */
double VirtualMachine_GetGlobalDouble(VirtualMachine* self, const char* name);

/**
 * Retrieves a double-typed variable from a global table within the 
 * VirtualMachine.
 * 
 * @param self      The VirtualMachine being used.
 * @param tableName The name of the global table in the VirtualMachine.
 * @param fieldName The name of the field in the table containing the double.
 * @return          The value of the global variable as a double.
 */
double VirtualMachine_GetGlobalTableDouble(VirtualMachine* self, 
                                           const char* tableName,
                                           const char* fieldName);

#endif
