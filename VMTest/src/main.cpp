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
static size_t global_instruction_pointer = 0;

static std::vector<ring_buffer> global_function_stacks;
static std::vector<size_t> instruction_pointers;
static size_t global_instruction_pointers_index = (size_t)-1;
static size_t global_function_stacks_index = (size_t)-1;

static void enter_stack_frame(inttype size)
{
	struct ring_buffer buffer;
	ring_buffer_create(&buffer, size);

	global_function_stacks_index++;
	if(global_function_stacks_index == global_function_stacks.size()) {
		global_function_stacks.push_back(buffer);
	} else {
		global_function_stacks[global_function_stacks_index] = buffer;
	}
}

static void leave_stack_frame()
{
	ring_buffer_release(&global_function_stacks[global_function_stacks_index]);
	global_function_stacks_index--;
}

static inttype get_stack_val(inttype val)
{
	return ring_buffer_get(
			&global_function_stacks[global_function_stacks_index], val);
}

static void stack_push(inttype val)
{
	ring_buffer_add(&global_function_stacks[global_function_stacks_index], val);
}

static void add_label(const std::string& label)
{
	global_labels[label] = global_instruction_pointer -1;
}

static void goto_label(const std::string& label)
{
	std::map<std::string, size_t>::const_iterator it = global_labels.find(label);
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
	global_instruction_pointers_index++;
	if(global_instruction_pointers_index == instruction_pointers.size()) {
		instruction_pointers.push_back(global_instruction_pointer);
	} else {
		instruction_pointers[global_instruction_pointers_index]
		   	= global_instruction_pointer;
	}

	goto_label(label);
	enter_stack_frame(global_labels_stack_frame_sizes[label]);
}

static void return_function()
{
	leave_stack_frame();
	global_instruction_pointer = 
		instruction_pointers[global_instruction_pointers_index];
	global_instruction_pointers_index--;
}

static std::string remove_comments(const std::string& line)
{
	if(line.empty()) {
		return line;
	} else {
		return line.substr(0, line.find(COMMENT_CHAR, 0));
	}	
}

static bool string_to_inttype(const std::string& str, inttype* resultPtr)
{
	const char* cstr = str.c_str();
	size_t str_length = str.length();
	size_t i = str_length - 1;

	inttype result = 0;
	inttype digitplace = 1;
	for(size_t counter = 0; counter < str_length; counter++) {
		char c = cstr[i];
		if(c == '-') {
			result = -result;
			continue;
		}
		if(c < '0' || c > '9') {
			return false;
		}
		inttype newval = (inttype)(c - '0') * digitplace;

		result += newval;
		digitplace *= 10;
		i--;
	}
	
	*resultPtr = result;
	return true;
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

static std::vector<inttype> get_parameters(
		const std::vector<std::string>& tokens)
{
	std::vector<inttype> parameters;
	parameters.reserve(tokens.size() - 1);

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

		parameters.push_back(val);
	}

	return parameters;
}

static void interpret_line(const std::vector<std::string>& tokens)
{
//	for(size_t counter = 0; counter < tokens.size(); counter++) {
//		std::cout << tokens[counter] << " ";
//	}
//	std::cout << std::endl;
	std::string ins = tokens[0];

	if(ins == "branch") {
		if(get_stack_val(0) == 1) {
			goto_label(tokens[1]);
		}
		return;
	}

	std::vector<inttype> parms = get_parameters(tokens);
	if(ins == "push") {
		stack_push(parms[0]);
	} else if(ins == "add") {
		stack_push(parms[0] + parms[1]);
	} else if(ins == "sub") {
		stack_push(parms[0] - parms[1]);
	} else if(ins == "mul") {
		stack_push(parms[0] * parms[1]);
	} else if(ins == "div") {
		stack_push(parms[0] % parms[1]);
		stack_push(parms[0] / parms[1]);
	}

	else if(ins == "equals?") {
		stack_push(parms[0] == parms[1]);
	}

	// Function calling instructions
	else if(ins == "ret") {
		return_function();
		for(size_t i = 0; i < parms.size(); i++) {
			stack_push(parms[i]);
		}
	} else {
		call_function(ins);
		for(size_t i = 0; i < parms.size(); i++) {
			stack_push(parms[i]);
		}
	}
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
			std::vector<std::string> tokens = global_program[global_instruction_pointer];

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
		if(global_function_stacks_index < 1) {
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
//	interpret_line("   ");
//	interpret_line("; introductory text thingy");
//	interpret_line("     ADd      s0      S1     ; something incredibly interesting      ");
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
//	string_to_inttype("198461234", &val);
//
//	std::cout << ((signed long)val) << std::endl;
	//std::cout << "Hello World" << std::endl;
	return 0;
}
