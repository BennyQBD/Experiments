#include <stdio.h>
#include "interpreter.h"

#define PROGRAM_FILE_NAME "./res/test.asm"

int main(int argc, char** argv)
{
	struct interpreter interp;
	enum interpreter_error error;
	enum io_error ioerror;
	inttype result = 0;

	(void)argc;
	(void)argv;

	interpreter_create(&interp);

	ioerror = interpreter_add_file(&interp, PROGRAM_FILE_NAME);
	if(ioerror != IO_ERROR_NONE) {
		fprintf(stderr, 
				"Error: File %s could not be loaded, due to error code: %d\n",
				PROGRAM_FILE_NAME, ioerror);
		return 1;
	}

	error = interpreter_run(&interp, &result);
	if(error != INTERPRETER_ERROR_NONE) {
		fprintf(stderr, "Error: Interpretation failed with error code: %d\n", error);
		return 1;
	}

	printf("Hello, World: %lu\n", result);

	interpreter_release(&interp);
	return 0;
}
