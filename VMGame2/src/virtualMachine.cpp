#include "virtualMachine.h"
#include "scriptstdstring.h"
#include "scriptbuilder.h"
#include <cstdio>
#include <cassert>

static void MessageCallback(const asSMessageInfo *msg, void *param)
{
	(void)param;

	const char *type = "ERR ";
	if( msg->type == asMSGTYPE_WARNING ) 
		type = "WARN";
	else if( msg->type == asMSGTYPE_INFORMATION ) 
		type = "INFO";

	printf("%s (%d, %d) : %s : %s\n", msg->section, msg->row, msg->col, type, msg->message);
}

static void print(const std::string& in)
{
	std::cout << in;
}

VirtualMachine::VirtualMachine()
{
	m_engine = asCreateScriptEngine(ANGELSCRIPT_VERSION);
	int r = m_engine->SetMessageCallback(asFUNCTION(MessageCallback), 0, asCALL_CDECL); 
	assert( r >= 0 );

	RegisterStdString(m_engine);
	r = m_engine->RegisterGlobalFunction("void print(const string &in)", asFUNCTION(print), asCALL_CDECL); 
	assert( r >= 0 );
	m_context = m_engine->CreateContext();
}

VirtualMachine::~VirtualMachine()
{
	m_engine->Release();
	m_context->Release();
}

void VirtualMachine::Call(const std::string& functionName)
{
	asIScriptModule *mod = m_engine->GetModule("MyModule");
	asIScriptFunction *func = mod->GetFunctionByDecl(functionName.c_str());
	if( func == 0 )
	{
		// The function couldn't be found. Instruct the script writer
		// to include the expected function in the script.
		printf("The script must have the function 'void main()'. Please add it and try again.\n");
		return;
	}

	m_context->Prepare(func);
	int r = m_context->Execute();
	if( r != asEXECUTION_FINISHED )
	{
		// The execution didn't complete as expected. Determine what happened.
		if( r == asEXECUTION_EXCEPTION )
		{
			// An exception occurred, let the script writer know what happened so it can be corrected.
			printf("An exception '%s' occurred. Please correct the code and try again.\n", m_context->GetExceptionString());
		}
	}
}

void VirtualMachine::LoadFile(const std::string& fileName)
{
	CScriptBuilder builder;
	int r = builder.StartNewModule(m_engine, "MyModule");
	if( r < 0 )
	{
		// If the code fails here it is usually because there
		// is no more memory to allocate the module
		printf("Unrecoverable error while starting a new module.\n");
		return;
	}
	r = builder.AddSectionFromFile(fileName.c_str());
	if( r < 0 )
	{
		// The builder wasn't able to load the file. Maybe the file
		// has been removed, or the wrong name was given, or some
		// preprocessing commands are incorrectly written.
		printf("Please correct the errors in the script and try again.\n");
		return;
	}
	r = builder.BuildModule();
	if( r < 0 )
	{
		// An error occurred. Instruct the script writer to fix the
		// compilation errors that were listed in the output stream.
		printf("Please correct the errors in the script and try again.\n");
		return;
	}
}

