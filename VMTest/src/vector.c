#include "vector.h"

#include <assert.h>
#include <stdlib.h>
#include <string.h>

#define DEFAULT_LENGTH 4
#define GROWTH_FACTOR 1.5

static void vector_grow(struct vector* self)
{
	self->allocated_length = (size_t)(self->allocated_length * GROWTH_FACTOR);
	self->data = realloc(self->data, self->allocated_length * self->data_size);
}

void vector_create(struct vector* self, size_t data_size, void(*freefn)(void*)) 
{
	assert(data_size > 0);
	self->logical_length = 0;
	self->allocated_length = DEFAULT_LENGTH;
	self->data_size = data_size;
	self->data = malloc(self->allocated_length * self->data_size);

	assert(self->data != NULL);
	self->freefn = freefn;	
}

void vector_release(struct vector* self)
{
	size_t i;

	if(self->freefn != NULL) {
		for(i = 0; i < self->logical_length; i++) {
			self->freefn((char*)self->data + i * self->data_size);
		}
	}

	free(self->data);
}

size_t vector_size(const struct vector* self)
{
	return self->logical_length;
}

size_t vector_capacity(const struct vector* self)
{
	return self->allocated_length;
}

int vector_empty(const struct vector* self)
{
	return self->logical_length == 0;
}

void* vector_at(const struct vector* self, size_t index)
{
	void* src;

	assert(index >= 0 && index < self->logical_length);
	src = (char*)self->data + index*self->data_size;

	return src;
}

void* vector_front(const struct vector* self)
{
	return vector_at(self, 0);
}

void* vector_back(const struct vector* self)
{
	return vector_at(self, self->logical_length - 1);
}

void* vector_to_array(const struct vector* self)
{
	return self->data;
}

void vector_push_back(struct vector* self, void* data)
{
	void* dest;

	if(self->allocated_length == self->logical_length) {
		vector_grow(self);
	}

	dest = (char*)self->data + (self->logical_length)*self->data_size;
	memcpy(dest, data, self->data_size);
	self->logical_length++;
}

void vector_pop_back(struct vector* self)
{
	void* loc;

	if(self->freefn != NULL) {
		loc = (char*)self->data + self->logical_length*self->data_size;
		self->freefn(loc);
	}
	self->logical_length--;
}

void vector_insert(struct vector* self, size_t index, void* data)
{
	void* dest;
	void* dest_end;
	void* end;

	assert(index >= 0 && index <= self->logical_length);
	if(self->allocated_length == self->logical_length) {
		vector_grow(self);
	}

	dest = (char*)self->data + index*self->data_size;
	dest_end = (char*)dest + self->data_size;
	end = (char*)self->data + (self->logical_length)*self->data_size;

	while(end >= dest_end) {
		void* begin = (char*)end - self->data_size;
		memcpy(end, begin, self->data_size);
		end = begin;
	}

	memcpy(dest, data, self->data_size);
	self->logical_length++;
}

void vector_set(struct vector* self, size_t index, void* data)
{
	void* dest;

	dest = (char*)self->data + index*self->data_size;
	if(self->freefn != NULL) {
		self->freefn(dest);
	}

	memcpy(dest, data, self->data_size);
}

void vector_erase(struct vector* self, size_t index)
{
	void* dest;
	void* end;
	void* dest_end;

	assert(index >= 0 && index < self->logical_length);

	dest = (char*)self->data + index*self->data_size;
	end = (char*)self->data + (self->logical_length)*self->data_size;

	if(self->freefn != NULL) {
		self->freefn(dest);
	}

	while(dest < end) {
		dest_end = (char*)dest + self->data_size;
		memcpy(dest, dest_end, self->data_size);
		dest = dest_end;
	}

	self->logical_length--;
}


void vector_clear(struct vector* self)
{
	size_t i;

	if(self->freefn != NULL) {
		for(i = 0; i < self->logical_length; i++) {
			self->freefn((char*)self->data + i * self->data_size);
		}
	}

	self->logical_length = 0;
}

void vector_unit_test()
{
	struct vector v;
	int val;
	int val2;
	size_t i;
	int val_array[] = { 5, 3, 8, 4, 7, 11, 12};

	vector_create(&v, sizeof(int), NULL);

	for(i = 0; i < sizeof(val_array)/sizeof(val_array[0]); i++) {
		vector_push_back(&v, &val_array[i]);
	}

	for(i = 0; i < vector_size(&v); i++) {
		val = *(int*)vector_at(&v, i);
		assert(val == val_array[i]);
	}
	
	val = *(int*)vector_front(&v);
	val2 = *(int*)vector_at(&v, 0);
	assert(val == val2);

	val = *(int*)vector_back(&v);
	val2 = *(int*)vector_at(&v, vector_size(&v) - 1);
	assert(val == val2);

//	for(i = 0; i < vector_size(&v); i++) {
//		vector_at(&v, i, &val);
//		printf("%d ", val);
//	}
//	printf("\n");
//
//	val = 1337;
//	vector_insert(&v, vector_size(&v) - 3, &val);
//
//	for(i = 0; i < vector_size(&v); i++) {
//		vector_at(&v, i, &val);
//		printf("%d ", val);
//	}
//	printf("\n");
//
//
//	vector_erase(&v, 7);
//
//	for(i = 0; i < vector_size(&v); i++) {
//		vector_at(&v, i, &val);
//		printf("%d ", val);
//	}
//	printf("\n");

	//printf("Hello, World: %d\n", vector_capacity(&v));

	vector_release(&v);
}
