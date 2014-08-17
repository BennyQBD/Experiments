#include "virtualMachine.h"

int main(int argc, char** argv)
{
	(void)argc;
	(void)argv;

	VirtualMachine vm;
	vm.LoadFile("script.as");
	vm.Call("void main()");

	std::cout << "Hello World! (C++)" << std::endl;
}
