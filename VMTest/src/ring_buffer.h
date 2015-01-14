#ifndef RING_BUFFER_INCLUDED_H
#define RING_BUFFER_INCLUDED_H

#include <stddef.h>

#ifdef __cplusplus
extern "C" {
#endif

struct ring_buffer {
	size_t pos;
	size_t length;
	size_t* data;
};

void ring_buffer_create(struct ring_buffer* self, size_t size);
void ring_buffer_release(struct ring_buffer* self);

void ring_buffer_add(struct ring_buffer* self, size_t val);
size_t ring_buffer_get(const struct ring_buffer* self, size_t index);

void ring_buffer_unit_test();

#ifdef __cplusplus
}
#endif

#endif // TOKENIZER_H_INCLUDED
