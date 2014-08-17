#ifndef VIRTUAL_MACHINE_INCLUDED_H
#define VIRTUAL_MACHINE_INCLUDED_H

#include <angelscript.h>
#include <iostream>
#include <string.h>

class VirtualMachine
{
public:
	VirtualMachine();
	~VirtualMachine();
	
	void LoadFile(const std::string& fileName);
	void Call(const std::string& functionName);
private:
	asIScriptEngine*  m_engine;
	asIScriptContext* m_context;

	VirtualMachine(const VirtualMachine& other) { (void)other; }
	void operator=(const VirtualMachine& other) { (void)other; }
};

#endif
