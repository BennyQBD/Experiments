#include "interpreter.h"
#include "ring_buffer.h"

#include <string.h>
#include <stdlib.h>
#include <assert.h>
#include <ctype.h>

#define STARTUP_FUNCTION "main"
#define COMMENT_CHAR ';'
#define STACK_CHAR 's'
#define LABEL_END_CHAR ':'

#define INTERPRETER_TRY(error, line) \
	if((error = line) != INTERPRETER_ERROR_NONE) return error


struct stack_frame_sizes_userdata {
	enum interpreter_error error;
	struct interpreter* self;
};

/* Data Structure Functions */
static int charptr_cmp_fn(const void* a, const void* b);
static void charptr_free_visit_fn(void* userdata, void* keyIn, void* valIn);
static void build_stack_frame_sizes_visit_fn(void* userdata, 
		void* keyIn, void* valIn);

/* Internal Stack Functions */
static void enter_stack_frame(struct interpreter* self, inttype size);
static void leave_stack_frame(struct interpreter* self);
static inttype get_stack_val(struct interpreter* self, inttype val);
static void stack_push(struct interpreter* self, inttype val);

/* Internal Labeling Functions */
static void add_label(struct interpreter* self, const char* key);
static enum interpreter_error goto_label(struct interpreter* self,
		const char* label);
static enum interpreter_error call_function(struct interpreter* self,
		const char* label);
static void return_function(struct interpreter* self);

/* Parsing Functions */
static void remove_comments(char* line);
static char string_to_inttype(const char* str, inttype* resultPtr);
static void strlower(char* str);
static void instruction_to_tokens(struct vector* tokens, char* line);

/* Interpretation Functions */
static enum interpreter_error get_parameters(struct interpreter* self,
		struct vector* result, struct vector* tokens);
static enum interpreter_error interpret_line(struct interpreter* self,
		struct vector* tokens);
static enum interpreter_error build_stack_frame_sizes(struct interpreter* self);


void interpreter_create(struct interpreter* self)
{
	vector_create(&self->instruction_ptrs__size_t, 
			sizeof(size_t), NULL);
	vector_create(&self->function_stacks__struct_ring_buffer, 
			sizeof(struct ring_buffer), NULL);
	vector_create(&self->program__struct_vector__charptr, 
			sizeof(struct vector), NULL);

	map_create(&self->labels__charptr__size_t, 
			sizeof(char*), sizeof(size_t), 
			charptr_cmp_fn);
	map_create(&self->stack_frame_sizes__charptr__size_t, 
			sizeof(char*), sizeof(size_t),
			charptr_cmp_fn);

	self->instruction_ptr = 0;
}

void interpreter_release(struct interpreter* self)
{
	size_t i, j;

	/* The keys in this map are the same as the keys in labels, so they
	   don't need to be freed */
	map_release(&self->stack_frame_sizes__charptr__size_t);

	map_visit_prefix(&self->labels__charptr__size_t, NULL,
			charptr_free_visit_fn);
	map_release(&self->labels__charptr__size_t);

	for(i = 0; i < vector_size(&self->program__struct_vector__charptr); i++) {

		for(j = 0; j < vector_size((struct vector*)
					vector_at(&self->program__struct_vector__charptr, i));
				j++) {

			free(*(char**)vector_at((struct vector*)
						vector_at(&self->program__struct_vector__charptr, 
							i), j));
		}
		vector_release((struct vector*)
				vector_at(&self->program__struct_vector__charptr, i));
	}

	vector_release(&self->program__struct_vector__charptr);

	for(i = 0; i < vector_size(&self->function_stacks__struct_ring_buffer); 
			i++) {
		ring_buffer_release(
				(struct ring_buffer*)vector_at(
					&self->function_stacks__struct_ring_buffer, i));
	}

	vector_release(&self->function_stacks__struct_ring_buffer);
	vector_release(&self->instruction_ptrs__size_t);
}

void interpreter_add_line(struct interpreter* self, const char* lineIn)
{
	struct vector tokens;
	size_t lineLength = strlen(lineIn) + 1;
	char* line = (char*)malloc(lineLength);
	char* ins;
	size_t insEndPos;

	memcpy(line, lineIn, lineLength);
	remove_comments(line);

	/* Nothing in this line; safe to ignore */
	if(!line[0]) {
		goto add_line_cleanup_1;
	}
	
	vector_create(&tokens, sizeof(char*), NULL);
	instruction_to_tokens(&tokens, line);

	/* No code in this line; safe to ignore */
	if(vector_size(&tokens) == 0) {
		goto add_line_cleanup_2;
	}

	ins = *(char**)vector_at(&tokens, 0);
	insEndPos = strlen(ins) - 1;

	if(ins[insEndPos] != LABEL_END_CHAR) {
		/* Line is valid code. Register it, delete string, but keep token data
		   for later interprettation */
		vector_push_back(&self->program__struct_vector__charptr, &tokens);
		self->instruction_ptr++;
		goto add_line_cleanup_1;
	} 
	
	
	/* Line is a label. Register it, and delete extra token data */
	ins[insEndPos] = 0; /* Remove end char; it's just a marker */
	add_label(self, ins);
	assert(vector_size(&tokens) == 1);

	/* Don't delete the first element; it's kept around as the label */
	for(size_t i = 1; i < vector_size(&tokens); i++) {
		free(*(char**)vector_at(&tokens, i));
	}

add_line_cleanup_2:
	vector_release(&tokens);
add_line_cleanup_1:
	free(line);
}

enum io_error interpreter_add_file(struct interpreter* self,
		const char* file_name)
{
	enum io_error error;
	char* line;
	size_t line_alloc_size;
	FILE* file;

	file = fopen(file_name, "r");
	if(file == NULL) {
		return IO_ERROR_FILE_NOT_FOUND;
	}

	line_alloc_size = 128;
	line = (char*)malloc(sizeof(char) * line_alloc_size);
	error = read_line(&line, &line_alloc_size, file);
	while(error == IO_ERROR_NONE)
	{
		interpreter_add_line(self, line);
		error = read_line(&line, &line_alloc_size, file); 
	}

	free(line);
	fclose(file);
	
	if(error == IO_ERROR_EOF) {
		return IO_ERROR_NONE;
	} else {
		return error;
	}
}

enum interpreter_error interpreter_run(struct interpreter* self,
		inttype* result)
{
	enum interpreter_error error;

	INTERPRETER_TRY(error, build_stack_frame_sizes(self));

	/* Create a stack frame to hold the main function's result */
	enter_stack_frame(self, 1);

	
	INTERPRETER_TRY(error, call_function(self, STARTUP_FUNCTION));
	self->instruction_ptr++;

	while(self->instruction_ptr < 
			vector_size(&self->program__struct_vector__charptr)) {
		INTERPRETER_TRY(error, 
				interpret_line(self, (struct vector*)vector_at(
						&self->program__struct_vector__charptr,
					self->instruction_ptr)));
		self->instruction_ptr++;

		/* If this is true, then the main function has returned */
		if(vector_size(&self->function_stacks__struct_ring_buffer) < 2) {
			break;
		}
	}
	
	*result = get_stack_val(self, 0);
	leave_stack_frame(self);
	
	return INTERPRETER_ERROR_NONE;
}




static int charptr_cmp_fn(const void* a, const void* b)
{
	char* temp1 = *(char**)a;
	char* temp2 = *(char**)b;

	if(temp1 != NULL && temp2 != NULL) {
		return strcmp(temp1, temp2);
	} else if(temp1 == NULL && temp2 == NULL) {
		return 0;
	} else if(temp1 == NULL) {
		return -1;
	} else {
		return 1;
	}
}

static void charptr_free_visit_fn(void* userdata, void* keyIn, void* valIn)
{
	char* key = *(char**)keyIn;
	(void)userdata;
	(void)valIn;

	if(key) {
		free(key);
	}
}

static void build_stack_frame_sizes_visit_fn(void* userdata, 
		void* keyIn, void* valIn)
{
	struct stack_frame_sizes_userdata* data = 
		(struct stack_frame_sizes_userdata*)(userdata);
	struct interpreter* self = data->self;
	char* key = *(char**)keyIn;
	inttype max_frame_size = 1;

	if(data->error != INTERPRETER_ERROR_NONE) {
		return;
	}

	(void)userdata;
	(void)valIn;

	data->error = goto_label(self, key);
	if(data->error != INTERPRETER_ERROR_NONE) {
		return;
	}
	self->instruction_ptr++;

	while(self->instruction_ptr < 
			vector_size(&self->program__struct_vector__charptr)) {
		struct vector* tokens = 
			(struct vector*)vector_at(&self->program__struct_vector__charptr,
				self->instruction_ptr);

		for(size_t i = 1; i < vector_size(tokens); i++) {
			char* current = *(char**)vector_at(tokens, i);
			if(current[0] != 's') {
				continue;
			}
			
			/* Ignore first char */
			current++;
			inttype val = 0;

			if(!string_to_inttype(current, &val)) {
				data->error = INTERPRETER_ERROR_NUMBER_PARSE_FAIL;
				return;
			}

			/* Increment by 1, since index is 0 based. */
			val++;
			if(val > max_frame_size) {
				max_frame_size = val;
			}
		}

		if(!strcmp(*(char**)vector_at(tokens, 0), "ret")) {
			break;
		}

		self->instruction_ptr++;
	}

	map_insert(&self->stack_frame_sizes__charptr__size_t,
			&key, &max_frame_size);
}


static void enter_stack_frame(struct interpreter* self, inttype size)
{
	struct ring_buffer buffer;
	ring_buffer_create(&buffer, size);

	vector_push_back(&self->function_stacks__struct_ring_buffer, &buffer);
}

static void leave_stack_frame(struct interpreter* self)
{
	ring_buffer_release(
			(struct ring_buffer*)vector_back(
				&self->function_stacks__struct_ring_buffer));
	vector_pop_back(&self->function_stacks__struct_ring_buffer);
}

static inttype get_stack_val(struct interpreter* self, inttype val)
{
	struct ring_buffer* buffer;
	buffer = (struct ring_buffer*)vector_back(
			&self->function_stacks__struct_ring_buffer);
	
	return ring_buffer_get(buffer, val);
}

static void stack_push(struct interpreter* self, inttype val)
{
	struct ring_buffer* buffer;

	buffer = (struct ring_buffer*)vector_back(
			&self->function_stacks__struct_ring_buffer);

	ring_buffer_add(buffer, val);
}

static void add_label(struct interpreter* self, const char* key)
{
	assert(key != NULL);
	size_t value = self->instruction_ptr - 1;
	map_insert(&self->labels__charptr__size_t, 
			&key, &value);
}

static enum interpreter_error goto_label(struct interpreter* self,
		const char* label)

{
	size_t* new_ip = (size_t*)map_at(&self->labels__charptr__size_t, &label);

	assert(self != NULL);

	if(new_ip == NULL) {
		return INTERPRETER_ERROR_INVALID_LABEL;
	}

	self->instruction_ptr = *new_ip;
	return INTERPRETER_ERROR_NONE;
}

static enum interpreter_error call_function(struct interpreter* self,
		const char* label)
{
	enum interpreter_error error;
	size_t* frame_size;
	vector_push_back(&self->instruction_ptrs__size_t, &self->instruction_ptr);

	INTERPRETER_TRY(error, goto_label(self, label));

	frame_size = (size_t*)map_at(&self->stack_frame_sizes__charptr__size_t,
			&label);
	if(frame_size == NULL) {
		return INTERPRETER_ERROR_INVALID_LABEL;
	}

	enter_stack_frame(self, *frame_size);
	return INTERPRETER_ERROR_NONE;
}

static void return_function(struct interpreter* self)
{
	leave_stack_frame(self);
	self->instruction_ptr = 
		*(size_t*)vector_back(&self->instruction_ptrs__size_t);
	vector_pop_back(&self->instruction_ptrs__size_t);
}

static void remove_comments(char* line)
{
	if(!line[0]) {
		return;
	}
	line = strchr(line, COMMENT_CHAR);

	if(line != NULL) {
		*line = 0;
	}
}

static char string_to_inttype(const char* str, inttype* resultPtr)
{
	size_t str_length = strlen(str);
	size_t i;
	int negate;
	inttype result = 0;

	negate = str[0] == '-';
	result = 0;
	for(i = (size_t)negate; str[i] >= '0' && str[i] <= '9'; i++) {
		result = result * 10 + (inttype)(str[i] - '0');
	}

	if(negate) {
		result = -result;
	}
	
	*resultPtr = result;
	return i >= str_length;
}

static void strlower(char* str)
{
	for(; *str; ++str) {
		*str = (char)tolower(*str);
	}
}

static void instruction_to_tokens(struct vector* tokens, char* line)
{
	const char* delim = " \t\r\n";
	char* token_out;

	line = strtok(line, delim);
	while(line != NULL) {
		if(!line[0]) {
			continue;
		}

		strlower(line);

		token_out = (char*)malloc((strlen(line) + 1) * sizeof(char));
		strcpy(token_out, line);

		vector_push_back(tokens, &token_out);
		line = strtok(NULL, delim);
	}
}

static enum interpreter_error get_parameters(struct interpreter* self,
		struct vector* result, struct vector* tokens)
{
	size_t i;

	for(i = 1; i < vector_size(tokens); i++) {
		char* current = *(char**)vector_at(tokens, i);
		inttype val;
		char is_stack_val = current[0] == STACK_CHAR;

		if(is_stack_val) {
			/* Ignore the first character. */
			current++;
		}

		if(!string_to_inttype(current, &val)) {
			return INTERPRETER_ERROR_NUMBER_PARSE_FAIL;
		}

		if(is_stack_val) {
			val = get_stack_val(self, val);
		}

		vector_push_back(result, &val);
	}

	return INTERPRETER_ERROR_NONE;
}

static enum interpreter_error interpret_line(struct interpreter* self,
		struct vector* tokens)
{
	enum interpreter_error error;
	const char* ins;
	struct vector parms_list;
	inttype* parms;
	size_t parms_length;
	size_t i;

	ins = *(const char**)vector_at(tokens, 0);

	if(!strcmp(ins, "branch")) {
		if(get_stack_val(self, 0) == 1) {
			return goto_label(self, *(char**)vector_at(tokens, 1));
		}
		return INTERPRETER_ERROR_NONE;
	}
	
	vector_create(&parms_list, sizeof(inttype), NULL);
	INTERPRETER_TRY(error, get_parameters(self, &parms_list, tokens));
	
	parms = (inttype*)vector_to_array(&parms_list);
	parms_length = vector_size(&parms_list);

	if(!strcmp(ins, "push")) {
		stack_push(self, parms[0]);
	} else if(!strcmp(ins, "add")) {
		stack_push(self, parms[0] + parms[1]);
	} else if(!strcmp(ins, "sub")) {
		stack_push(self, parms[0] - parms[1]);
	} else if(!strcmp(ins, "mul")) {
		stack_push(self, parms[0] * parms[1]);
	} else if(!strcmp(ins, "div")) {
		stack_push(self, parms[0] % parms[1]);
		stack_push(self, parms[0] / parms[1]);
	}

	else if(!strcmp(ins, "equals?")) {
		stack_push(self, parms[0] == parms[1]);
	}

	/* Function calling instructions */
	else if(!strcmp(ins, "ret")) {
		return_function(self);
		for(i = 0; i < parms_length; i++) {
			stack_push(self, parms[i]);
		}
	} else {
		INTERPRETER_TRY(error, call_function(self, ins));
		for(i = 0; i < parms_length; i++) {
			stack_push(self, parms[i]);
		}
	}

	vector_release(&parms_list);
	return INTERPRETER_ERROR_NONE;
}

static enum interpreter_error build_stack_frame_sizes(struct interpreter* self)
{
	enum interpreter_error result = INTERPRETER_ERROR_NONE;

	struct stack_frame_sizes_userdata data;
	data.self = self;
	data.error = result;

	self->instruction_ptr = 0;
	map_visit_prefix(&self->labels__charptr__size_t, 
			&data, build_stack_frame_sizes_visit_fn);

	return result;
}

