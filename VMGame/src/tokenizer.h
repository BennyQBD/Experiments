#ifndef TOKENIZER_H_INCLUDED
#define TOKENIZER_H_INCLUDED

typedef struct
{
	const char* m_input;
	const char* m_charsToIgnore;
	const char* m_tokenChars;
} Tokenizer;

void Tokenizer_Init(Tokenizer* tokenizer, const char* input, const char* charsToIgnore, const char* tokenChars);
void Tokenizer_Deinit(Tokenizer* tokenizer);
void Tokenizer_Copy(Tokenizer* dest, Tokenizer* src);

int Tokenizer_NextToken(Tokenizer* tokenizer, char* result, const unsigned int maxResultLength);

void TokenizerUnitTest(void);

#endif // TOKENIZER_H_INCLUDED
