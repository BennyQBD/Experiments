#ifndef IO_INCLUDED_H
#define IO_INCLUDED_H

#include <stdio.h>

#ifdef __cplusplus
extern "C" {
#endif

enum io_error {
	IO_ERROR_NONE,
	IO_ERROR_OUT_OF_MEMORY,
	IO_ERROR_EOF,
	IO_ERROR_NULL_FILE,
	IO_ERROR_FILE_NOT_FOUND
};

enum io_error read_line(char** result, size_t* result_alloc_size, FILE* file);

#ifdef __cplusplus
}
#endif

#endif
