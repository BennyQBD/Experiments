#include "isubsystem.h"

ISubSystem::SubSystemException::SubSystemException(const std::string& what_arg) :
	std::runtime_error(what_arg) {}

	
