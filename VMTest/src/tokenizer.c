#include "tokenizer.h"

#include <string.h>
#include <assert.h>

void tokenizer_create(struct tokenizer* tokenizer, const char* input,
		const char* charsToIgnore, const char* tokenChars)
{
	tokenizer->m_input = input;
	tokenizer->m_charsToIgnore = charsToIgnore;
	tokenizer->m_tokenChars = tokenChars;
}

void tokenizer_release(struct tokenizer* tokenizer)
{
	(void)(tokenizer);
}

int tokenizer_next_token(struct tokenizer* tokenizer, char* result,
		unsigned int maxResultLength)
{
	const char* tokenStart;
	unsigned long tokenLength;

	while(*tokenizer->m_input != 0) {
		if(strchr(tokenizer->m_tokenChars, *tokenizer->m_input) != NULL) {
			assert(maxResultLength >= 1);
			result[0] = *tokenizer->m_input;
			result[1] = 0;
			
			tokenizer->m_input++;
			return 1;
		}
		
		if(strchr(tokenizer->m_charsToIgnore, *tokenizer->m_input) == NULL) {
			break;
		}
		
		tokenizer->m_input++;
	}

	if(*tokenizer->m_input == 0) {
		return 0;
	}
	
	tokenStart = tokenizer->m_input;
	
	while(*tokenizer->m_input != 0) {
		if(strchr(tokenizer->m_charsToIgnore, *tokenizer->m_input) != NULL ||
			   	strchr(tokenizer->m_tokenChars, *tokenizer->m_input) != NULL) {
			break;
		}
	
		tokenizer->m_input++;
	}
	
	tokenLength = (unsigned long)(tokenizer->m_input - tokenStart);
	assert(maxResultLength >= tokenLength);

	strncpy(result, tokenStart, tokenLength);
	result[tokenLength] = 0;

	return 1;
}

void tokenizer_unit_test()
{
	struct tokenizer test;
	char token[6];
	
	tokenizer_create(&test, "Hello, World!", ", ", "!");
    
    assert(tokenizer_next_token(&test, token, sizeof(token)));
    assert(strcmp(token, "Hello\n"));
    assert(tokenizer_next_token(&test, token, sizeof(token)));
	assert(strcmp(token, "World\n"));
	assert(tokenizer_next_token(&test, token, sizeof(token)));
	assert(strcmp(token, "!\n"));
	assert(!tokenizer_next_token(&test, token, sizeof(token)));
	
	tokenizer_release(&test);
}
