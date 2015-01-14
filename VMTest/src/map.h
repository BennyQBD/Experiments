#ifndef MAP_INCLUDED_H
#define MAP_INCLUDED_H

#include <stddef.h>

#ifdef __cplusplus
extern "C" {
#endif

struct map_node {
	void* key;
	void* value;
	struct map_node* left;
	struct map_node* right;
};

struct map {
	size_t key_size;
	size_t value_size;
	struct map_node* root;
	int(*cmp_fn)(const void* a, const void* b);
};

void map_create(struct map* self, size_t key_size, size_t value_size,
		int(*cmp_fn)(const void* a, const void*b));
void map_release(struct map* self);

void* map_at(const struct map* self, const void* key);
void map_visit_prefix(const struct map* self,
		void* userdata,
		void(*fn)(void* userdata, void* key, void* value));
void map_visit_order(const struct map* self, 
		void* userdata,
		void(*fn)(void* userdata, void* key, void* value));

void map_insert(struct map* self, void* key, void* value);
void map_clear(struct map* self);

void map_unit_test();

#ifdef __cplusplus
}
#endif

#endif
