#ifndef TOKENIZER_H_INCLUDED
#define TOKENIZER_H_INCLUDED

#ifdef __cplusplus
extern "C" {
#endif

struct tokenizer {
	const char* m_input;
	const char* m_charsToIgnore;
	const char* m_tokenChars;
};

void tokenizer_create(struct tokenizer* tokenizer, const char* input,
		const char* charsToIgnore, const char* tokenChars);
void tokenizer_release(struct tokenizer* tokenizer);

int tokenizer_next_token(struct tokenizer* tokenizer, char* result,
		unsigned int maxResultLength);

void tokenizer_unit_test(void);

#ifdef __cplusplus
}
#endif

#endif // TOKENIZER_H_INCLUDED
