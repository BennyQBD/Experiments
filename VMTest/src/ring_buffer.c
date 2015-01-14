#include "ring_buffer.h"

#include <string.h>
#include <stdlib.h>
#include <assert.h>

void ring_buffer_create(struct ring_buffer* self, size_t size)
{
	memset(self, 0, sizeof(*self));

	assert(size > 0);
	self->pos = 0;
	self->length = size;
	self->data = (size_t*)malloc(size * sizeof(size_t));
}

void ring_buffer_release(struct ring_buffer* self)
{
	free(self->data);
}

void ring_buffer_add(struct ring_buffer* self, size_t val)
{
	self->data[self->pos] = val;
	self->pos = (self->pos + 1) % self->length;
}

size_t ring_buffer_get(const struct ring_buffer* self, size_t index)
{
	assert(index >= 0 && index < self->length);
	index = (self->pos - index - 1);
	index = (index + self->length) % self->length;
	return self->data[index];
}

#include <stdio.h>

void ring_buffer_unit_test()
{
	struct ring_buffer test;
	size_t val;

	ring_buffer_create(&test, 3);

	ring_buffer_add(&test, 1);
	ring_buffer_add(&test, 2);
	ring_buffer_add(&test, 3);

	val = ring_buffer_get(&test, 0);
	assert(val == 3);
	val = ring_buffer_get(&test, 1);
	assert(val == 2);
	val = ring_buffer_get(&test, 2);
	assert(val == 1);

	ring_buffer_add(&test, 4);

	val = ring_buffer_get(&test, 0);
	assert(val == 4);
	val = ring_buffer_get(&test, 1);
	assert(val == 3);
	val = ring_buffer_get(&test, 2);
	assert(val == 2);

	ring_buffer_release(&test);
}

