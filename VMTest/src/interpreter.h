#ifndef INTERPRETER_INCLUDED_H
#define INTERPRETER_INCLUDED_H

#include "vector.h"
#include "map.h"
#include "io.h"

#ifdef __cplusplus
extern "C" {
#endif

enum interpreter_error {
	INTERPRETER_ERROR_NONE,
	INTERPRETER_ERROR_INVALID_LABEL,
	INTERPRETER_ERROR_NUMBER_PARSE_FAIL,
	INTERPRETER_ERROR_FILE_NOT_FOUND
};
#define INTERPRETER_TRY(error, line) \
	if((error = line) != INTERPRETER_ERROR_NONE) return error

typedef size_t inttype;

struct interpreter {
	/* (struct vector<struct vector<char*>>) */
	struct vector program__struct_vector__charptr;
	/* (struct map<char*, size_t>) */
	struct map labels__charptr__size_t;
	/* (struct map<char*, size_t>) */
	struct map stack_frame_sizes__charptr__size_t;
	
	/* (struct vector<struct ring_buffer>) */
	struct vector function_stacks__struct_ring_buffer;
	/* (struct vector<size_t>) */
	struct vector instruction_ptrs__size_t;
	size_t instruction_ptr;
};

void interpreter_create(struct interpreter* self);
void interpreter_release(struct interpreter* self);

void interpreter_add_line(struct interpreter* self, const char* line);
enum io_error interpreter_add_file(struct interpreter* self,
		const char* file_name);
enum interpreter_error interpreter_run(struct interpreter* self,
		inttype* result);


#ifdef __cplusplus
}
#endif

#endif
