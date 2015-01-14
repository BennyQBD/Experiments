#include "io.h"

#include <stdlib.h>

enum io_error read_line(char** result, size_t* result_alloc_size, FILE* file)
{
	char* line = *result;
	size_t line_alloc_size = *result_alloc_size;
	size_t char_read_count = 0;
	char ch;

	if(file == NULL) {
		return IO_ERROR_NULL_FILE;
	}

	if(feof(file)) {
		return IO_ERROR_EOF;
	}

	ch = (char)getc(file);
	
	if(line_alloc_size == 0) {
		line_alloc_size = 128;
		line = (char*)malloc(sizeof(char) * line_alloc_size);
		if(line == NULL) {
			return IO_ERROR_OUT_OF_MEMORY;
		}
	}

	while((ch != '\n') && (ch != EOF)) {
		if(char_read_count == line_alloc_size) {
			line_alloc_size *= 2;
			line = (char*)realloc(result, line_alloc_size);
			if(line == NULL) {
				return IO_ERROR_OUT_OF_MEMORY;
			}
		}
		line[char_read_count] = ch;
		char_read_count++;

		ch = (char)getc(file);
	}

	line[char_read_count] = 0;
	*result = line;
	*result_alloc_size = line_alloc_size;
	return IO_ERROR_NONE;
}

