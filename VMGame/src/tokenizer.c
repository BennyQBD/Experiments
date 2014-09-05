#include "tokenizer.h"
#include "util.h"

#include <string.h>
#include <assert.h>

void Tokenizer_Init(Tokenizer* tokenizer, const char* input, const char* charsToIgnore, const char* tokenChars)
{
	tokenizer->m_input = input;
	tokenizer->m_charsToIgnore = charsToIgnore;
	tokenizer->m_tokenChars = tokenChars;
}

void Tokenizer_Deinit(Tokenizer* tokenizer)          { UNUSED_PARAMETER(tokenizer); }
void Tokenizer_Copy(Tokenizer* dest, Tokenizer* src) { DEFAULT_COPY(dest, src, Tokenizer); }

int Tokenizer_NextToken(Tokenizer* tokenizer, char* result, const unsigned int maxResultLength)
{
	const char* tokenStart;
	unsigned long tokenLength;

	while(*tokenizer->m_input != 0)
	{
		if(strchr(tokenizer->m_tokenChars, *tokenizer->m_input) != NULL)
		{
			assert(maxResultLength >= 1);
			result[0] = *tokenizer->m_input;
			result[1] = 0;
			
			tokenizer->m_input++;
			return 1;
		}
		
		if(strchr(tokenizer->m_charsToIgnore, *tokenizer->m_input) == NULL)
		{
			break;
		}
		
		tokenizer->m_input++;
	}

	if(*tokenizer->m_input == 0)
	{
		return 0;
	}
	
	tokenStart = tokenizer->m_input;
	
	while(*tokenizer->m_input != 0)
	{
		if(strchr(tokenizer->m_charsToIgnore, *tokenizer->m_input) != NULL || strchr(tokenizer->m_tokenChars, *tokenizer->m_input) != NULL)
		{
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

void TokenizerUnitTest()
{
	Tokenizer test;
	char token[6];
	
	Tokenizer_Init(&test, "Hello, World!", ", ", "!");
    
    assert(Tokenizer_NextToken(&test, token, sizeof(token)));
    assert(strcmp(token, "Hello\n"));
    assert(Tokenizer_NextToken(&test, token, sizeof(token)));
	assert(strcmp(token, "World\n"));
	assert(Tokenizer_NextToken(&test, token, sizeof(token)));
	assert(strcmp(token, "!\n"));
	assert(!Tokenizer_NextToken(&test, token, sizeof(token)));
	
	Tokenizer_Deinit(&test);
}
