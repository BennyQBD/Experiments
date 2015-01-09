#ifndef VECTOR_INCLUDED_H
#define VECTOR_INCLUDED_H

#include <stddef.h>

#ifdef __cplusplus
extern "C" {
#endif

struct vector {
	void* data;
	size_t data_size;
	size_t logical_length;
	size_t allocated_length;
	void(*freefn)(void*);
};

void vector_create(struct vector* self, size_t data_size, void(*freefn)(void*));
void vector_release(struct vector* self);

size_t vector_size(const struct vector* self);
size_t vector_capacity(const struct vector* self);
int vector_empty(const struct vector* self);

void* vector_at(const struct vector* self, size_t index);
void* vector_front(const struct vector* self);
void* vector_back(const struct vector* self);
void* vector_to_array(const struct vector* self);


void vector_push_back(struct vector* self, void* data);
void vector_pop_back(struct vector* self);
void vector_set(struct vector* self, size_t index, void* data);
void vector_insert(struct vector* self, size_t index, void* data);
void vector_erase(struct vector* self, size_t index);
void vector_clear(struct vector* self);

void vector_unit_test();

#ifdef __cplusplus
}
#endif

#endif // TOKENIZER_H_INCLUDED
