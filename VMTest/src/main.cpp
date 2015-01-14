#include <iostream>
#include <fstream>
#include <algorithm>
#include <map>
#include <stdexcept>
#include <sstream>
#include <stdlib.h>
#include <string.h>
#include <cassert>
#include "tokenizer.h"
#include "ring_buffer.h"
#include "vector.h"
#include "map.h"

// TODO: C style maps
// TODO: C++ might be overkill for this. Possibly revert to C?
// TODO: Don't cram everything in one file anymore. This experiment is getting
//       large enough to warrant some structure.
// TODO: Good way of handling different data types/sizes
// TODO: Possibly variable-size integer types

#define PROGRAM_FILE_NAME "./res/test.asm"
#define STARTUP_FUNCTION "main"
#define COMMENT_CHAR ';'
#define STACK_CHAR 's'
#define LABEL_END_CHAR ':'

typedef size_t inttype;

/* (struct vector<struct vector<char*>>) */
static struct vector global_program;
/* (struct map<char*, size_t>) */
static struct map global_labels;
/* (struct map<char*, size_t>) */
static struct map global_labels_stack_frame_sizes;

/* (struct vector<struct ring_buffer>) */
static struct vector global_function_stacks;
/* (struct vector<size_t>) */
static struct vector global_instruction_pointers;
static size_t global_instruction_pointer = 0;

static int global_labels_cmp_fn(const void* a, const void* b)
{
	char* temp1 = *(char**)a;
	char* temp2 = *(char**)b;
	return strcmp(temp1, temp2);
}

static void init_interpretter()
{
	vector_create(&global_instruction_pointers, sizeof(size_t), NULL);
	vector_create(&global_function_stacks, sizeof(struct ring_buffer), NULL);
	vector_create(&global_program, sizeof(struct vector), NULL);

	map_create(&global_labels, sizeof(char*), sizeof(size_t), 
			global_labels_cmp_fn);
	map_create(&global_labels_stack_frame_sizes, sizeof(char*), sizeof(size_t),
			global_labels_cmp_fn);
}

static void global_labels_free_visit_fn(void* userdata, void* keyIn, void* valIn)
{
	char* key = *(char**)keyIn;
	(void)userdata;
	(void)valIn;

	free(key);
}

static void deinit_interpretter()
{
	size_t i, j;

	/* The keys in this map are the same as the keys in global_labels, so they
	   don't need to be freed */
	map_release(&global_labels_stack_frame_sizes);

	map_visit_prefix(&global_labels, NULL, global_labels_free_visit_fn);
	map_release(&global_labels);

	for(i = 0; i < vector_size(&global_program); i++) {

		for(j = 0; j < vector_size((struct vector*)
					vector_at(&global_program, i)); j++) {

			free(*(char**)vector_at((struct vector*)
						vector_at(&global_program, i), j));
		}
		vector_release((struct vector*)vector_at(&global_program, i));
	}

	vector_release(&global_program);

	for(i = 0; i < vector_size(&global_function_stacks); i++) {
		ring_buffer_release(
				(struct ring_buffer*)vector_at(&global_function_stacks, i));
	}

	vector_release(&global_function_stacks);
	vector_release(&global_instruction_pointers);
}

static void enter_stack_frame(inttype size)
{
	struct ring_buffer buffer;
	ring_buffer_create(&buffer, size);

	vector_push_back(&global_function_stacks, &buffer);
}

static void leave_stack_frame()
{
	ring_buffer_release(
			(struct ring_buffer*)vector_back(&global_function_stacks));
	vector_pop_back(&global_function_stacks);
}

static inttype get_stack_val(inttype val)
{
	struct ring_buffer* buffer;
	buffer = (struct ring_buffer*)vector_back(&global_function_stacks);
	
	return ring_buffer_get(buffer, val);
}

static void stack_push(inttype val)
{
	struct ring_buffer* buffer;
	buffer = (struct ring_buffer*)vector_back(&global_function_stacks);

	ring_buffer_add(buffer, val);
}

static void add_label(const char* key)
{
	assert(key != NULL);
	size_t value = global_instruction_pointer - 1;
	map_insert(&global_labels, &key, &value);
}

static void goto_label(const char* label)
{
	size_t* new_ip = (size_t*)map_at(&global_labels, &label);

	if(new_ip == NULL) {
		std::ostringstream out;
		out << "Error: " << label << " is not specified in the program!";
		throw std::runtime_error(out.str());
	}

	global_instruction_pointer = *new_ip;
}

static void call_function(const char* label)
{
	size_t* frame_size;
	vector_push_back(&global_instruction_pointers, &global_instruction_pointer);

	goto_label(label);
	frame_size = (size_t*)map_at(&global_labels_stack_frame_sizes, &label);
	if(frame_size == NULL) {
		std::ostringstream out;
		out << "Error: " << label << " is not specified in the program!";
		throw std::runtime_error(out.str());
	}
	enter_stack_frame(*frame_size);
}

static void return_function()
{
	leave_stack_frame();
	global_instruction_pointer = 
		*(size_t*)vector_back(&global_instruction_pointers);
	vector_pop_back(&global_instruction_pointers);
}

static char* remove_comments(char* line)
{
	char* comment_loc;
	if(!line[0]) {
		return line;
	} else {
		comment_loc = strchr(line, COMMENT_CHAR);

		if(comment_loc != NULL) {
			*comment_loc = 0;
		}
		
		return line;
	}	
}

static bool string_to_inttype(const std::string& strIn, inttype* resultPtr)
{
	const char* str = strIn.c_str();
	size_t str_length = strIn.length();
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

static void get_parameters(struct vector* result, struct vector* tokens)
{
	size_t i;

	for(i = 1; i < vector_size(tokens); i++) {
		char* current = *(char**)vector_at(tokens, i);
		inttype val;
		bool is_stack_val = current[0] == STACK_CHAR;

		if(is_stack_val) {
			// Ignore the first character.
			current++;
		}

		if(!string_to_inttype(current, &val)) {
			std::ostringstream out;
			out << "Error: " << current << " is not a number!";
			throw std::runtime_error(out.str());
		}

		if(is_stack_val) {
			val = get_stack_val(val);
		}

		vector_push_back(result, &val);
	}
}

static void interpret_line(struct vector* tokens)
{
	const char* ins;
	struct vector parms_list;
	inttype* parms;
	size_t parms_length;
	size_t i;

	ins = *(const char**)vector_at(tokens, 0);

	if(!strcmp(ins, "branch")) {
		if(get_stack_val(0) == 1) {
			goto_label(*(char**)vector_at(tokens, 1));
		}
		return;
	}
	
	vector_create(&parms_list, sizeof(inttype), NULL);
	get_parameters(&parms_list, tokens);
	
	parms = (inttype*)vector_to_array(&parms_list);
	parms_length = vector_size(&parms_list);

	if(!strcmp(ins, "push")) {
		stack_push(parms[0]);
	} else if(!strcmp(ins, "add")) {
		stack_push(parms[0] + parms[1]);
	} else if(!strcmp(ins, "sub")) {
		stack_push(parms[0] - parms[1]);
	} else if(!strcmp(ins, "mul")) {
		stack_push(parms[0] * parms[1]);
	} else if(!strcmp(ins, "div")) {
		stack_push(parms[0] % parms[1]);
		stack_push(parms[0] / parms[1]);
	}

	else if(!strcmp(ins, "equals?")) {
		stack_push(parms[0] == parms[1]);
	}

	// Function calling instructions
	else if(!strcmp(ins, "ret")) {
		return_function();
		for(i = 0; i < parms_length; i++) {
			stack_push(parms[i]);
		}
	} else {
		call_function(ins);
		for(i = 0; i < parms_length; i++) {
			stack_push(parms[i]);
		}
	}

	vector_release(&parms_list);
}

static void add_line(const char* lineIn)
{
	struct vector tokens;
	size_t lineLength = strlen(lineIn) + 1;
	char* line = (char*)malloc(lineLength);
	char* ins;
	size_t insEndPos;

	memcpy(line, lineIn, lineLength);
	line = remove_comments(line);

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
		vector_push_back(&global_program, &tokens);
		global_instruction_pointer++;
		goto add_line_cleanup_1;
	} 
	
	/* Line is a label. Register it, and delete extra token data */
	ins[insEndPos] = 0; /* Remove end char; it's just a marker*/
	add_label(ins);
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

static void build_stack_frame_sizes_visit_fn(void* userdata, 
		void* keyIn, void* valIn)
{
	char* key = *(char**)keyIn;
	inttype max_frame_size = 1;

	(void)userdata;
	(void)valIn;

	goto_label(key);
	global_instruction_pointer++;

	while(global_instruction_pointer < vector_size(&global_program)) {
		struct vector* tokens = (struct vector*)vector_at(&global_program,
				global_instruction_pointer);

		for(size_t i = 1; i < vector_size(tokens); i++) {
			char* current = *(char**)vector_at(tokens, i);
			if(current[0] != 's') {
				continue;
			}
			
			// Ignore first char
			current++;
			inttype val = 0;

			if(!string_to_inttype(current, &val)) {
				std::ostringstream out;
				out << "Error: " << current << " is not a number!";
				throw std::runtime_error(out.str());
			}

			// Increment by 1, since index is 0 based.
			val++;
			if(val > max_frame_size) {
				max_frame_size = val;
			}
		}

		if(!strcmp(*(char**)vector_at(tokens, 0), "ret")) {
			break;
		}

		global_instruction_pointer++;
	}

	map_insert(&global_labels_stack_frame_sizes, &key, &max_frame_size);
}

static void build_stack_frame_sizes()
{
	global_instruction_pointer = 0;
	map_visit_prefix(&global_labels, NULL, build_stack_frame_sizes_visit_fn);
}

static inttype interpret_program()
{
	inttype result;

	build_stack_frame_sizes();

	// The create a stack frame to hold the main function's result
	enter_stack_frame(1);

	call_function(STARTUP_FUNCTION);
	global_instruction_pointer++;

	while(global_instruction_pointer < vector_size(&global_program)) {
		interpret_line((struct vector*)vector_at(&global_program,
					global_instruction_pointer));
		global_instruction_pointer++;

		// If this is true, then the main function has returned
		if(vector_size(&global_function_stacks) < 2) {
			break;
		}
	}
	
	result = get_stack_val(0);
	leave_stack_frame();
	
	return result;
}

enum io_error {
	IO_ERROR_NONE,
	IO_ERROR_OUT_OF_MEMORY,
	IO_ERROR_EOF,
	IO_ERROR_FILE_NOT_FOUND
};
static enum io_error read_line(char** result, size_t* line_alloc_size, FILE* file)
{
	char* line = *result;
	size_t result_alloc_size = *line_alloc_size;
	size_t char_read_count = 0;
	char ch;

	assert(file != NULL);

	ch = (char)getc(file);
	if(ch == EOF) {
		return IO_ERROR_EOF;
	}

	if(result_alloc_size == 0) {
		result_alloc_size = 128;
		line = (char*)malloc(sizeof(char) * result_alloc_size);
		if(line == NULL) {
			return IO_ERROR_OUT_OF_MEMORY;
		}
	}

	while((ch != '\n') && (ch != EOF)) {
		if(char_read_count == result_alloc_size) {
			result_alloc_size *= 2;
			line = (char*)realloc(result, result_alloc_size);
			if(line == NULL) {
				return IO_ERROR_OUT_OF_MEMORY;
			}
		}
		line[char_read_count] = ch;
		char_read_count++;

		ch = (char)getc(file);
	}

	if(ch == EOF) {
		ungetc(ch, file);
	}

	line[char_read_count] = 0;
	*result = line;
	*line_alloc_size = result_alloc_size;
	return IO_ERROR_NONE;
}

static enum io_error interpret_file(inttype* result, const std::string& file_name)
{
	enum io_error error;
	char* line;
	size_t line_alloc_size;
	FILE* file;

	file = fopen(file_name.c_str(), "r");
	if(file == NULL) {
		return IO_ERROR_FILE_NOT_FOUND;
	}

	// TODO: Handle errors!
	line_alloc_size = 128;
	line = (char*)malloc(sizeof(char) * line_alloc_size);
	error = read_line(&line, &line_alloc_size, file);
	while(error == IO_ERROR_NONE)
	{
		add_line(line);
		error = read_line(&line, &line_alloc_size, file); 
	}

	free(line);
	fclose(file);
	
	*result = interpret_program();
	return IO_ERROR_NONE;
}

int main(int argc, char** argv)
{
	size_t val;

	(void)argc;
	(void)argv;
	init_interpretter();

//	add_line("main:");
//	add_line("push 5");
//	add_line("push 3");
//	add_line("add s0 s1");
//	std::cout << interpret_program() << std::endl;

	if(interpret_file(&val, PROGRAM_FILE_NAME) != IO_ERROR_NONE) {
		fprintf(stderr, "Error: File could not be interpretted");
		exit(1);
	}

	std::cout << val << std::endl;

//	for(int i = 0; i < global_stack.size(); i++) {
//		std::cout << get_stack_val(i) << std::endl;
//	}

//	inttype val = 0;
//	if(!string_to_inttype("981264109264", &val)) {
//		std::cout << "Value did not convert!" << std::endl;
//	}
//
//	std::cout << ((signed long)val) << std::endl;
	//std::cout << "Hello World" << std::endl;

	deinit_interpretter();
	return 0;
}
