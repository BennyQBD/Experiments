#include "ring_buffer.h"

#include <string.h>
#include <stdlib.h>
#include <assert.h>

void ring_buffer_create(struct ring_buffer* self, size_t size)
{
	memset(self, 0, sizeof(*self));
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

size_t ring_buffer_get(struct ring_buffer* self, size_t index)
{
	assert(index < self->length);
	index = (self->pos - index - 1);
	index = (index + self->length) % self->length;
	return self->data[index];
}

