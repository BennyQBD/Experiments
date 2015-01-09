#include <iostream>
#include <fstream>
#include <algorithm>
#include <vector>
#include <map>
#include <stdexcept>
#include <sstream>
#include <stdlib.h>
#include <string.h>
#include <cassert>
#include "tokenizer.h"
#include "ring_buffer.h"
#include "vector.h"

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

static std::vector<std::vector<std::string> > global_program;
static std::map<std::string, size_t> global_labels;
static std::map<std::string, size_t> global_labels_stack_frame_sizes;

static struct vector global_function_stacks;
static struct vector global_instruction_pointers;
static size_t global_instruction_pointer = 0;

static void ring_buffer_release_vec(void* data)
{
	ring_buffer_release((struct ring_buffer*)data);
}

static void init_interpretter()
{
	vector_create(&global_instruction_pointers, sizeof(size_t), NULL);
	vector_create(&global_function_stacks, sizeof(ring_buffer), NULL);
}

static void deinit_interpretter()
{
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

static void add_label(const std::string& label)
{
	global_labels[label] = global_instruction_pointer -1;
}

static void goto_label(const std::string& label)
{
	std::map<std::string, size_t>::const_iterator it =global_labels.find(label);
	if(it == global_labels.end())
	{
		std::ostringstream out;
		out << "Error: " << label << " is not specified in the program!";
		throw std::runtime_error(out.str());
	}
	
	global_instruction_pointer = global_labels[label];
}

static void call_function(const std::string& label)
{
	vector_push_back(&global_instruction_pointers, &global_instruction_pointer);

	goto_label(label);
	enter_stack_frame(global_labels_stack_frame_sizes[label]);
}

static void return_function()
{
	leave_stack_frame();
	global_instruction_pointer = 
		*(size_t*)vector_back(&global_instruction_pointers);
	vector_pop_back(&global_instruction_pointers);
}

static std::string remove_comments(const std::string& line)
{
	if(line.empty()) {
		return line;
	} else {
		return line.substr(0, line.find(COMMENT_CHAR, 0));
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

static std::vector<std::string> instruction_to_tokens(
		const std::string& line)
{
	struct tokenizer tokens;
	std::string token;
	char* tokenIn = (char*)malloc(line.length() * sizeof(char));
	std::vector<std::string> result;

	tokenizer_create(&tokens, line.c_str(), " \t\r\n", "");

	while(tokenizer_next_token(&tokens, tokenIn, (unsigned int)line.length())) {
		token = std::string(tokenIn);
		if(token.empty()) {
			continue;
		}

		std::transform(token.begin(), token.end(), token.begin(), ::tolower);

		result.push_back(token);
	}

	tokenizer_release(&tokens);
	free(tokenIn);
	return result;
}

static void get_parameters(struct vector* result,
		const std::vector<std::string>& tokens)
{
	for(std::vector<std::string>::const_iterator it = ++tokens.begin();
			it != tokens.end(); ++it) {
		std::string current = *it;
		inttype val;
		bool is_stack_val = current[0] == STACK_CHAR;

		if(is_stack_val) {
			current = current.substr(1, current.length() - 1);
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

static void interpret_line(const std::vector<std::string>& tokens)
{
	const char* ins;
	struct vector parms_list;
	inttype* parms;
	size_t parms_length;
	size_t i;

	ins = tokens[0].c_str();

	if(!strcmp(ins, "branch")) {
		if(get_stack_val(0) == 1) {
			goto_label(tokens[1]);
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

static void add_line(const std::string& lineIn)
{
	std::string line = remove_comments(lineIn);
	if(line.empty()) {
		return;
	}
	
	std::vector<std::string> tokens = instruction_to_tokens(line);
	if(tokens.empty()) {
		return;
	}

	std::string ins = tokens[0];

	if(ins[ins.length() - 1] == LABEL_END_CHAR) {
		add_label(ins.substr(0, ins.length() - 1));
		return;
	}

	global_program.push_back(tokens);
	global_instruction_pointer++;
}

static void build_stack_frame_sizes()
{
	global_instruction_pointer = 0;
	for(std::map<std::string, size_t>::iterator it=global_labels.begin();
			it!=global_labels.end(); ++it) {
		inttype max_frame_size = 1;

		goto_label(it->first);
		global_instruction_pointer++;

		while(global_instruction_pointer < global_program.size()) {
			std::vector<std::string> tokens = 
				global_program[global_instruction_pointer];

			for(size_t i = 1; i < tokens.size(); i++) {
				std::string current = tokens[i];
				if(current[0] != 's') {
					continue;
				}
				
				current = current.substr(1, current.length() - 1);
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

			if(tokens[0] == "ret") {
				break;
			}

			global_instruction_pointer++;
		}

		global_labels_stack_frame_sizes[it->first] = max_frame_size;
	}
}

static inttype interpret_program()
{
	inttype result;

	build_stack_frame_sizes();

	// The create a stack frame to hold the main function's result
	enter_stack_frame(1);

	call_function(STARTUP_FUNCTION);
	global_instruction_pointer++;

	while(global_instruction_pointer < global_program.size()) {
		interpret_line(global_program[global_instruction_pointer]);
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

static inttype interpret_file(const std::string& file_name)
{
	std::string line;
	std::ifstream program_text(file_name.c_str());
	if(!program_text.is_open()) {
		throw std::runtime_error(
				"Error: Unable to open file");
	}

	while(program_text.good()) {
		getline(program_text, line);
		add_line(line);
	}
	
	return interpret_program();
}

int main(int argc, char** argv)
{
	init_interpretter();

//	interpret_line("   ");
//	interpret_line("; introductory text thingy");
//	interpret_line("add 5 3");
//	interpret_line("add s0 7");
//	interpret_line("add s0 s1");
//	interpret_line("add s0 s2");
//	interpret_line("sub s0 1");
//	interpret_line("div s0 4");
//	interpret_line("mul s0 4");
//	interpret_line("add s0 s2");

	std::cout << interpret_file(PROGRAM_FILE_NAME) << std::endl;

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
